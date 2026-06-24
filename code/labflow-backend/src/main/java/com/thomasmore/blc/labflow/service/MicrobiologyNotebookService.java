package com.thomasmore.blc.labflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyAntibiogramEntryDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyAntibiogramUpdateRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyGramkleuringDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyGramkleuringRowDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyNotebookResponse;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyStaalTestDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyStaalTestUpdateRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyVoedingsbodemLogEntry;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyVoedingsbodemNotebookDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyVoedingsbodemLogsRequest;
import com.thomasmore.blc.labflow.entity.microbiology.Antibiogram;
import com.thomasmore.blc.labflow.entity.microbiology.Antibiotica;
import com.thomasmore.blc.labflow.entity.microbiology.Bepaling;
import com.thomasmore.blc.labflow.entity.microbiology.Staal;
import com.thomasmore.blc.labflow.entity.microbiology.StaalStatus;
import com.thomasmore.blc.labflow.entity.microbiology.StaalTest;
import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodemLog;
import com.thomasmore.blc.labflow.entity.microbiology.Test;
import com.thomasmore.blc.labflow.entity.microbiology.TestType;
import com.thomasmore.blc.labflow.repository.microbiology.AntibiogramRepository;
import com.thomasmore.blc.labflow.repository.microbiology.AntibioticaRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestVoedingsbodemLogRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestVoedingsbodemRepository;

import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional("microbiologyTransactionManager")
public class MicrobiologyNotebookService {

    private static final List<String> GRAM_BEPALINGEN = List.of("WBC", "RBC", "EPC", "Kiemen");
    private static final List<String> SECTION_ORDER = List.of(
            "algemene-testen", "voedingsbodems", "gramkleuring", "antibiogram");
    private static final Set<String> VALID_SCORES = Set.of("0", "+", "++", "+++", "++++");
    private static final Pattern BEOORDELING_PATTERN = Pattern.compile("^\\+{1,4}$");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Qualifier("microbiologyStaalRepository")
    private StaalRepository staalRepository;

    @Autowired
    @Qualifier("microbiologyStaalTestRepository")
    private StaalTestRepository staalTestRepository;

    @Autowired
    @Qualifier("microbiologyStaalTestVoedingsbodemRepository")
    private StaalTestVoedingsbodemRepository staalTestVoedingsbodemRepository;

    @Autowired
    @Qualifier("microbiologyStaalTestVoedingsbodemLogRepository")
    private StaalTestVoedingsbodemLogRepository logRepository;

    @Autowired
    @Qualifier("microbiologyAntibioticaRepository")
    private AntibioticaRepository antibioticaRepository;

    @Autowired
    @Qualifier("microbiologyAntibiogramRepository")
    private AntibiogramRepository antibiogramRepository;

    public MicrobiologyNotebookResponse getNotebook(Long staalId) {
        Staal staal = requireStaal(staalId);
        List<StaalTest> staalTests = staalTestRepository.findByStaalId(staalId);
        staalTests.forEach(st -> Hibernate.initialize(st.getTest()));

        MicrobiologyNotebookResponse response = new MicrobiologyNotebookResponse();
        response.setId(staal.getId());
        response.setStaalCode(staal.getStaalCode());
        response.setPatientVoornaam(staal.getPatientVoornaam());
        response.setPatientAchternaam(staal.getPatientAchternaam());
        if (staal.getPatientGeboorteDatum() != null) {
            response.setPatientGeboorteDatum(
                    staal.getPatientGeboorteDatum().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        response.setPatientGeslacht(String.valueOf(staal.getPatientGeslacht()));
        response.setCommentaar(staal.getCommentaar());
        response.setVoltooidAlgemeneTesten(staal.isVoltooidAlgemeneTesten());
        response.setVoltooidVoedingsbodems(staal.isVoltooidVoedingsbodems());
        response.setVoltooidGramkleuring(staal.isVoltooidGramkleuring());
        response.setVoltooidAntibiogram(staal.isVoltooidAntibiogram());
        response.setStatus(staal.getStatus() != null ? staal.getStatus().name() : StaalStatus.AANGEMAAKT.name());

        List<String> activeSections = buildActiveSections(staalTests);
        response.setActiveSections(activeSections);

        response.setAlgemeneTesten(staalTests.stream()
                .filter(st -> st.getTest().getTestType() == TestType.EXTRA_TEST)
                .map(this::toStaalTestDto)
                .sorted(Comparator.comparing(MicrobiologyStaalTestDto::getTestCode))
                .collect(Collectors.toList()));

        response.setVoedingsbodems(activeSections.contains("voedingsbodems")
                ? buildVoedingsbodems(staalId) : List.of());
        response.setGramkleuring(buildGramkleuring(staalTests));
        response.setAntibiogram(hasTestType(staalTests, TestType.ANTIBIOGRAM)
                ? buildAntibiogram(staalId) : List.of());

        return response;
    }

    public void updateCommentaar(Long staalId, String commentaar) {
        Staal staal = requireStaal(staalId);
        assertStaalEditable(staal);
        staal.setCommentaar(commentaar);
        staalRepository.save(staal);
    }

    public void updateVoltooid(Long staalId, String section, boolean voltooid) {
        Staal staal = requireStaal(staalId);
        assertStaalEditable(staal);
        List<StaalTest> staalTests = staalTestRepository.findByStaalId(staalId);
        staalTests.forEach(st -> Hibernate.initialize(st.getTest()));
        List<String> activeSections = buildActiveSections(staalTests);
        if (!activeSections.contains(section)) {
            throw new IllegalArgumentException("Section is not active for this staal: " + section);
        }
        switch (section) {
            case "algemene-testen" -> staal.setVoltooidAlgemeneTesten(voltooid);
            case "voedingsbodems" -> staal.setVoltooidVoedingsbodems(voltooid);
            case "gramkleuring" -> staal.setVoltooidGramkleuring(voltooid);
            case "antibiogram" -> staal.setVoltooidAntibiogram(voltooid);
            default -> throw new IllegalArgumentException("Unknown section: " + section);
        }
        staalRepository.save(staal);
    }

    public void updateStaalTest(Long staalId, Long staalTestId, MicrobiologyStaalTestUpdateRequest body) {
        Staal staal = requireStaal(staalId);
        assertStaalEditable(staal);
        if (staal.isVoltooidAlgemeneTesten()) {
            throw new IllegalStateException("Section algemene testen is voltooid");
        }
        StaalTest st = requireStaalTest(staalId, staalTestId);
        if (st.getTest().getTestType() != TestType.EXTRA_TEST) {
            throw new IllegalArgumentException("Test does not belong to algemene testen section");
        }
        if (body.getWaarde() != null) {
            st.setWaarde(body.getWaarde());
        }
        if (body.getCommentaar() != null) {
            st.setCommentaar(body.getCommentaar());
        }
        if (body.getFailed() != null) {
            st.setFailed(body.getFailed());
            if (body.getFailed()) {
                st.setWaarde("");
            }
        }
        staalTestRepository.save(st);
    }

    public void updateVoedingsbodemCommentaar(Long staalId, Long linkId, String commentaar) {
        Staal staal = requireStaal(staalId);
        assertStaalEditable(staal);
        if (staal.isVoltooidVoedingsbodems()) {
            throw new IllegalStateException("Section voedingsbodems is voltooid");
        }
        StaalTestVoedingsbodem link = requireVoedingsbodemLink(staalId, linkId);
        link.setCommentaar(commentaar);
        staalTestVoedingsbodemRepository.save(link);
    }

    public void syncVoedingsbodemLogs(Long staalId, Long linkId, MicrobiologyVoedingsbodemLogsRequest body) {
        Staal staal = requireStaal(staalId);
        assertStaalEditable(staal);
        if (staal.isVoltooidVoedingsbodems()) {
            throw new IllegalStateException("Section voedingsbodems is voltooid");
        }
        StaalTestVoedingsbodem link = requireVoedingsbodemLink(staalId, linkId);
        List<MicrobiologyVoedingsbodemLogEntry> entries =
                body.getLogs() != null ? body.getLogs() : List.of();

        List<MicrobiologyVoedingsbodemLogEntry> validated = new ArrayList<>();
        for (MicrobiologyVoedingsbodemLogEntry entry : entries) {
            if (isEmptyLogEntry(entry)) {
                continue;
            }
            if (entry.getOrganisme() == null || entry.getOrganisme().isBlank()) {
                throw new IllegalArgumentException("Organisme is verplicht voor ingevulde rijen");
            }
            if (entry.getBeoordeling() == null || !BEOORDELING_PATTERN.matcher(entry.getBeoordeling()).matches()) {
                throw new IllegalArgumentException("Beoordeling moet 1 tot 4 '+' tekens zijn");
            }
            validated.add(entry);
        }

        logRepository.deleteByStaalTestVoedingsbodem_Id(linkId);
        for (MicrobiologyVoedingsbodemLogEntry entry : validated) {
            StaalTestVoedingsbodemLog log = new StaalTestVoedingsbodemLog();
            log.setStaalTestVoedingsbodem(link);
            log.setOrganisme(entry.getOrganisme().trim());
            log.setBeoordeling(entry.getBeoordeling().trim());
            log.setSts(entry.getSts() != null ? entry.getSts().trim() : null);
            log.setCommentaar(entry.getCommentaar() != null ? entry.getCommentaar().trim() : null);
            LocalDateTime now = LocalDateTime.now();
            log.setCreatedAt(now);
            log.setUpdatedAt(now);
            logRepository.save(log);
        }
    }

    public void updateGramkleuring(Long staalId, MicrobiologyGramkleuringDto body) {
        Staal staal = requireStaal(staalId);
        assertStaalEditable(staal);
        if (staal.isVoltooidGramkleuring()) {
            throw new IllegalStateException("Section gramkleuring is voltooid");
        }
        StaalTest gramTest = findGramkleuringTest(staalId)
                .orElseThrow(() -> new IllegalArgumentException("Geen gramkleuring test voor dit staal"));

        if (body.getCommentaar() != null) {
            gramTest.setCommentaar(body.getCommentaar());
        }
        if (body.getRows() != null) {
            for (MicrobiologyGramkleuringRowDto row : body.getRows()) {
                if (row.getScore() != null && !row.getScore().isBlank()
                        && !VALID_SCORES.contains(row.getScore())) {
                    throw new IllegalArgumentException("Ongeldige score: " + row.getScore());
                }
            }
            gramTest.setWaarde(serializeGramRows(body.getRows()));
        }
        staalTestRepository.save(gramTest);
    }

    public void updateAntibiogram(Long staalId, MicrobiologyAntibiogramUpdateRequest body) {
        Staal staal = requireStaal(staalId);
        assertStaalEditable(staal);
        if (staal.isVoltooidAntibiogram()) {
            throw new IllegalStateException("Section antibiogram is voltooid");
        }
        List<StaalTest> staalTests = staalTestRepository.findByStaalId(staalId);
        staalTests.forEach(st -> Hibernate.initialize(st.getTest()));
        if (!hasTestType(staalTests, TestType.ANTIBIOGRAM)) {
            throw new IllegalArgumentException("Geen antibiogram test voor dit staal");
        }
        if (body.getEntries() == null) {
            return;
        }
        for (MicrobiologyAntibiogramUpdateRequest.MicrobiologyAntibiogramEntryUpdate entry : body.getEntries()) {
            if (entry.getAntibioticaId() == null || entry.getBeoordeling() == null) {
                continue;
            }
            String beoordeling = entry.getBeoordeling().trim().toUpperCase();
            Antibiotica ab = antibioticaRepository.findById(entry.getAntibioticaId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid antibiotica id"));

            if ("R".equals(beoordeling)) {
                antibiogramRepository.deleteByStaal_IdAndAntibiotica_Id(staalId, ab.getId());
            } else if ("S".equals(beoordeling)) {
                upsertAntibiogram(staal, ab, Bepaling.SENSITIEF);
            } else if ("I".equals(beoordeling)) {
                upsertAntibiogram(staal, ab, Bepaling.INTERMEDIARE);
            } else {
                throw new IllegalArgumentException("Beoordeling moet R, S of I zijn");
            }
        }
    }

    public void markStaalKlaar(Long staalId) {
        Staal staal = requireStaal(staalId);
        if (staal.getStatus() == StaalStatus.KLAAR) {
            return;
        }
        List<StaalTest> staalTests = staalTestRepository.findByStaalId(staalId);
        staalTests.forEach(st -> Hibernate.initialize(st.getTest()));
        List<String> activeSections = buildActiveSections(staalTests);
        for (String section : activeSections) {
            if (!isSectionVoltooid(staal, section)) {
                throw new IllegalStateException("Not all notebook sections are voltooid");
            }
        }
        staal.setStatus(StaalStatus.KLAAR);
        staalRepository.save(staal);
    }

    private boolean isSectionVoltooid(Staal staal, String section) {
        return switch (section) {
            case "algemene-testen" -> staal.isVoltooidAlgemeneTesten();
            case "voedingsbodems" -> staal.isVoltooidVoedingsbodems();
            case "gramkleuring" -> staal.isVoltooidGramkleuring();
            case "antibiogram" -> staal.isVoltooidAntibiogram();
            default -> false;
        };
    }

    private void upsertAntibiogram(Staal staal, Antibiotica ab, Bepaling bepaling) {
        Optional<Antibiogram> existing =
                antibiogramRepository.findByStaal_IdAndAntibiotica_Id(staal.getId(), ab.getId());
        Antibiogram row = existing.orElseGet(Antibiogram::new);
        row.setStaal(staal);
        row.setAntibiotica(ab);
        row.setBepaling(bepaling);
        antibiogramRepository.save(row);
    }

    private Staal requireStaal(Long staalId) {
        return staalRepository.findById(staalId)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + staalId));
    }

    private void assertStaalEditable(Staal staal) {
        if (staal.getStatus() == StaalStatus.KLAAR) {
            throw new IllegalStateException("Staal is afgerond en kan niet meer worden gewijzigd");
        }
    }

    private StaalTest requireStaalTest(Long staalId, Long staalTestId) {
        StaalTest st = staalTestRepository.findById(staalTestId)
                .orElseThrow(() -> new EntityNotFoundException("StaalTest not found"));
        if (!Objects.equals(st.getStaal().getId(), staalId)) {
            throw new EntityNotFoundException("StaalTest does not belong to staal");
        }
        Hibernate.initialize(st.getTest());
        return st;
    }

    private StaalTestVoedingsbodem requireVoedingsbodemLink(Long staalId, Long linkId) {
        StaalTestVoedingsbodem link = staalTestVoedingsbodemRepository.findById(linkId)
                .orElseThrow(() -> new EntityNotFoundException("Voedingsbodem link not found"));
        if (!Objects.equals(link.getStaalTest().getStaal().getId(), staalId)) {
            throw new EntityNotFoundException("Voedingsbodem link does not belong to staal");
        }
        return link;
    }

    private List<String> buildActiveSections(List<StaalTest> staalTests) {
        LinkedHashSet<String> sections = new LinkedHashSet<>();
        for (StaalTest st : staalTests) {
            Test test = st.getTest();
            if (test == null || test.getTestType() == null) {
                continue;
            }
            String section = sectionForTestType(test.getTestType());
            if (section != null) {
                sections.add(section);
            }
        }
        return SECTION_ORDER.stream().filter(sections::contains).collect(Collectors.toList());
    }

    private String sectionForTestType(TestType testType) {
        return switch (testType) {
            case EXTRA_TEST -> "algemene-testen";
            case CULTUUR -> "voedingsbodems";
            case GRAMKLEURING -> "gramkleuring";
            case ANTIBIOGRAM -> "antibiogram";
        };
    }

    private boolean hasTestType(List<StaalTest> staalTests, TestType testType) {
        return staalTests.stream()
                .anyMatch(st -> st.getTest() != null && st.getTest().getTestType() == testType);
    }

    private Optional<StaalTest> findGramkleuringTest(Long staalId) {
        return staalTestRepository.findByStaalId(staalId).stream()
                .filter(st -> {
                    Hibernate.initialize(st.getTest());
                    return st.getTest() != null && st.getTest().getTestType() == TestType.GRAMKLEURING;
                })
                .findFirst();
    }

    private MicrobiologyStaalTestDto toStaalTestDto(StaalTest st) {
        MicrobiologyStaalTestDto dto = new MicrobiologyStaalTestDto();
        dto.setId(st.getId());
        dto.setTestId(st.getTest().getId());
        dto.setTestCode(st.getTest().getTestCode());
        dto.setTestNaam(st.getTest().getNaam());
        dto.setWaarde(st.getWaarde());
        dto.setCommentaar(st.getCommentaar());
        dto.setFailed(st.isFailed());
        return dto;
    }

    private List<MicrobiologyVoedingsbodemNotebookDto> buildVoedingsbodems(Long staalId) {
        List<StaalTestVoedingsbodem> links = staalTestVoedingsbodemRepository.findByStaalId(staalId);
        Map<Long, StaalTestVoedingsbodem> byVbId = new LinkedHashMap<>();
        for (StaalTestVoedingsbodem link : links) {
            Hibernate.initialize(link.getVoedingsbodem());
            Long vbId = link.getVoedingsbodem().getId();
            byVbId.putIfAbsent(vbId, link);
        }

        return byVbId.values().stream()
                .sorted(Comparator.comparing(l -> l.getVoedingsbodem().getNaam(),
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(link -> {
                    MicrobiologyVoedingsbodemNotebookDto dto = new MicrobiologyVoedingsbodemNotebookDto();
                    dto.setLinkId(link.getId());
                    dto.setVoedingsbodemId(link.getVoedingsbodem().getId());
                    dto.setVoedingsbodemNaam(link.getVoedingsbodem().getNaam());
                    dto.setCommentaar(link.getCommentaar());
                    dto.setLogs(logRepository.findByStaalTestVoedingsbodem_IdOrderByIdAsc(link.getId()).stream()
                            .map(log -> {
                                MicrobiologyVoedingsbodemLogEntry entry = new MicrobiologyVoedingsbodemLogEntry();
                                entry.setId(log.getId());
                                entry.setOrganisme(log.getOrganisme());
                                entry.setBeoordeling(log.getBeoordeling());
                                entry.setSts(log.getSts());
                                entry.setCommentaar(log.getCommentaar());
                                if (log.getCreatedAt() != null) {
                                    entry.setCreatedAt(log.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                                }
                                if (log.getUpdatedAt() != null) {
                                    entry.setUpdatedAt(log.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                                } else if (log.getCreatedAt() != null) {
                                    entry.setUpdatedAt(log.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                                }
                                return entry;
                            })
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private MicrobiologyGramkleuringDto buildGramkleuring(List<StaalTest> staalTests) {
        MicrobiologyGramkleuringDto dto = new MicrobiologyGramkleuringDto();
        Optional<StaalTest> gramOpt = staalTests.stream()
                .filter(st -> st.getTest() != null && st.getTest().getTestType() == TestType.GRAMKLEURING)
                .findFirst();

        if (gramOpt.isEmpty()) {
            dto.setRows(List.of());
            return dto;
        }

        StaalTest gram = gramOpt.get();
        dto.setStaalTestId(gram.getId());
        dto.setCommentaar(gram.getCommentaar());
        dto.setRows(parseGramRows(gram.getWaarde()));
        return dto;
    }

    private List<MicrobiologyGramkleuringRowDto> parseGramRows(String waarde) {
        Map<String, MicrobiologyGramkleuringRowDto> parsed = new LinkedHashMap<>();
        if (waarde != null && !waarde.isBlank()) {
            try {
                Map<String, Map<String, String>> raw = objectMapper.readValue(waarde, new TypeReference<>() {});
                for (Map.Entry<String, Map<String, String>> e : raw.entrySet()) {
                    Map<String, String> row = e.getValue();
                    parsed.put(e.getKey(), new MicrobiologyGramkleuringRowDto(
                            e.getKey(),
                            row.getOrDefault("score", ""),
                            row.getOrDefault("commentaar", "")));
                }
            } catch (JsonProcessingException ignored) {
                // fall back to empty rows
            }
        }
        List<MicrobiologyGramkleuringRowDto> rows = new ArrayList<>();
        for (String bepaling : GRAM_BEPALINGEN) {
            rows.add(parsed.getOrDefault(bepaling, new MicrobiologyGramkleuringRowDto(bepaling, "", "")));
        }
        return rows;
    }

    private String serializeGramRows(List<MicrobiologyGramkleuringRowDto> rows) {
        Map<String, Map<String, String>> out = new LinkedHashMap<>();
        for (MicrobiologyGramkleuringRowDto row : rows) {
            String key = row.getBepaling() != null ? row.getBepaling() : "";
            Map<String, String> val = new LinkedHashMap<>();
            val.put("score", row.getScore() != null ? row.getScore() : "");
            val.put("commentaar", row.getCommentaar() != null ? row.getCommentaar() : "");
            out.put(key, val);
        }
        try {
            return objectMapper.writeValueAsString(out);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize gramkleuring data", e);
        }
    }

    private List<MicrobiologyAntibiogramEntryDto> buildAntibiogram(Long staalId) {
        Map<Long, Bepaling> saved = antibiogramRepository.findByStaal_Id(staalId).stream()
                .collect(Collectors.toMap(a -> a.getAntibiotica().getId(), Antibiogram::getBepaling));

        return antibioticaRepository.findAllByOrderByNaamAsc().stream()
                .map(ab -> {
                    MicrobiologyAntibiogramEntryDto dto = new MicrobiologyAntibiogramEntryDto();
                    dto.setAntibioticaId(ab.getId());
                    dto.setAntibioticaNaam(ab.getNaam());
                    Bepaling b = saved.get(ab.getId());
                    if (b == Bepaling.SENSITIEF) {
                        dto.setBeoordeling("S");
                    } else if (b == Bepaling.INTERMEDIARE) {
                        dto.setBeoordeling("I");
                    } else {
                        dto.setBeoordeling("R");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private boolean isEmptyLogEntry(MicrobiologyVoedingsbodemLogEntry entry) {
        return (entry.getOrganisme() == null || entry.getOrganisme().isBlank())
                && (entry.getBeoordeling() == null || entry.getBeoordeling().isBlank())
                && (entry.getSts() == null || entry.getSts().isBlank())
                && (entry.getCommentaar() == null || entry.getCommentaar().isBlank());
    }
}
