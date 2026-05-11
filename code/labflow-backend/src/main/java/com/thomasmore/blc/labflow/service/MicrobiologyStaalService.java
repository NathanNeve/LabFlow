package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.dto.MicrobiologyStaalCreateRequest;
import com.thomasmore.blc.labflow.dto.MicrobiologyStaalTestsRequest;
import com.thomasmore.blc.labflow.dto.MicrobiologyStaalUpdateRequest;
import com.thomasmore.blc.labflow.dto.MicrobiologyTestResponse;
import com.thomasmore.blc.labflow.dto.MicrobiologyVoedingsbodemsConfirmRequest;
import com.thomasmore.blc.labflow.entity.microbiology.Staal;
import com.thomasmore.blc.labflow.entity.microbiology.StaalTest;
import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.entity.microbiology.Test;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;
import com.thomasmore.blc.labflow.repository.microbiology.StaalCodeCounterRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestVoedingsbodemRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTypeRepository;
import com.thomasmore.blc.labflow.repository.microbiology.TestRepository;
import com.thomasmore.blc.labflow.repository.microbiology.TestVoedingsbodemRepository;
import com.thomasmore.blc.labflow.repository.microbiology.VoedingsbodemRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Path;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional("microbiologyTransactionManager")
public class MicrobiologyStaalService {

    @Autowired
    private StaalRepository staalRepository;

    @Autowired
    private StaalTypeRepository staalTypeRepository;

    @Autowired
    private StaalCodeCounterRepository staalCodeCounterRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private StaalTestRepository staalTestRepository;

    @Autowired
    private StaalTestVoedingsbodemRepository staalTestVoedingsbodemRepository;

    @Autowired
    private TestVoedingsbodemRepository testVoedingsbodemRepository;

    @Autowired
    private VoedingsbodemRepository voedingsbodemRepository;

    public Page<Staal> findStalen(int page, int size, String search, String dateStr) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("staalCode").descending());

        Specification<Staal> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Path<?> staalCodePath = root.get("staalCode");
                if (staalCodePath.getJavaType().equals(Long.class)) {
                    try {
                        Long searchAsLong = Long.parseLong(search.trim());
                        return cb.equal(root.get("staalCode"), searchAsLong);
                    } catch (NumberFormatException e) {
                        return cb.disjunction();
                    }
                }
                return cb.disjunction();
            });
        }

        if (dateStr != null && !dateStr.isBlank()) {
            try {
                LocalDate date = LocalDate.parse(dateStr);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59, 999_999_999);
                spec = spec.and((root, query, cb) ->
                        cb.between(root.get("patientGeboorteDatum"), startOfDay, endOfDay));
            } catch (DateTimeParseException e) {
                // ignore invalid date filter
            }
        }

        Page<Staal> staalPage = staalRepository.findAll(spec, pageable);
        staalPage.getContent().forEach(this::initializeForJson);
        return staalPage;
    }

    private void initializeForJson(Staal staal) {
        if (staal == null) {
            return;
        }
        Hibernate.initialize(staal.getStaalType());
    }

    public ResponseEntity<Staal> create(MicrobiologyStaalCreateRequest body) {
        if (body == null || body.getStaalTypeId() == null) {
            return ResponseEntity.badRequest().build();
        }
        final Long staalTypeId = body.getStaalTypeId();
        StaalType type = staalTypeRepository.findById(staalTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid staalTypeId"));

        Staal staal = new Staal();
        staal.setLaborantNaam(body.getLaborantNaam());
        staal.setLaborantRnummer(body.getLaborantRnummer());
        staal.setStaalType(type);
        // patient data will be completed in the next step of the flow
        staal.setPatientGeslacht('X');

        final int maxAttempts = 5;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            staal.setStaalCode(allocateNextStaalCode());
            try {
                Staal saved = staalRepository.save(staal);
                initializeForJson(saved);
                return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            } catch (DataIntegrityViolationException e) {
                if (attempt == maxAttempts) {
                    throw new UniqueConstraintViolationException("Could not allocate unique staalcode after retries");
                }
            }
        }
        throw new UniqueConstraintViolationException("Could not create staal");
    }

    private Long allocateNextStaalCode() {
        int year = Year.now().getValue();
        Long next = staalCodeCounterRepository.incrementAndGet(year);
        if (next == null || next <= 0) {
            throw new UniqueConstraintViolationException("Could not allocate staalcode counter value");
        }
        String code = year + String.format("%06d", next);
        return Long.parseLong(code);
    }

    public Optional<Staal> getById(Long id) {
        return staalRepository.findById(id).map(staal -> {
            initializeForJson(staal);
            return staal;
        });
    }

    public List<Test> findTestsByStaalTypeId(Long staalTypeId) {
        if (staalTypeId == null) {
            return Collections.emptyList();
        }
        List<Test> list = testRepository.findByStaalType_Id(staalTypeId);
        list.forEach(t -> Hibernate.initialize(t.getStaalType()));
        return list;
    }

    public List<MicrobiologyTestResponse> findTestsWithVoedingsbodemsByStaalTypeId(Long staalTypeId) {
        if (staalTypeId == null) {
            return Collections.emptyList();
        }
        List<Test> tests = testRepository.findByStaalType_Id(staalTypeId);
        return tests.stream()
                .map(t -> new MicrobiologyTestResponse(
                        t.getId(),
                        t.getTestCode(),
                        t.getNaam(),
                        t.isExtraTest(),
                        t.getId() == null ? Collections.emptyList() : testVoedingsbodemRepository.findVoedingsbodemNamesByTestId(t.getId())
                ))
                .collect(Collectors.toList());
    }

    public ResponseEntity<Staal> saveStaalTests(Long staalId, MicrobiologyStaalTestsRequest body) {
        if (body == null) {
            return ResponseEntity.badRequest().build();
        }
        if (body.getPatientVoornaam() == null || body.getPatientVoornaam().isBlank()
                || body.getPatientAchternaam() == null || body.getPatientAchternaam().isBlank()
                || body.getPatientGeboorteDatum() == null
                || body.getPatientGeslacht() == null || body.getPatientGeslacht().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Staal staal = staalRepository.findById(staalId)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + staalId));
        Hibernate.initialize(staal.getStaalType());
        final Long expectedStaalTypeId = staal.getStaalType().getId();

        staalTestVoedingsbodemRepository.deleteByStaalId(staalId);
        staalTestRepository.deleteByStaalId(staalId);

        staal.setPatientVoornaam(body.getPatientVoornaam());
        staal.setPatientAchternaam(body.getPatientAchternaam());
        staal.setPatientGeboorteDatum(body.getPatientGeboorteDatum());
        staal.setPatientGeslacht(body.getPatientGeslacht().charAt(0));
        staalRepository.save(staal);

        List<Long> testIds = body.getTestIds() != null ? body.getTestIds() : Collections.emptyList();
        for (Long testId : testIds) {
            if (testId == null) {
                continue;
            }
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid test id: " + testId));
            Hibernate.initialize(test.getStaalType());
            if (!Objects.equals(test.getStaalType().getId(), expectedStaalTypeId)) {
                throw new IllegalArgumentException("Test does not belong to staal type");
            }
            StaalTest row = new StaalTest();
            row.setStaal(staal);
            row.setTest(test);
            staalTestRepository.save(row);
        }

        Staal saved = staalRepository.findById(staalId).orElse(staal);
        initializeForJson(saved);
        return ResponseEntity.ok(saved);
    }

    public void clearStaalTests(Long staalId) {
        if (!staalRepository.existsById(staalId)) {
            throw new EntityNotFoundException("Staal not found with id: " + staalId);
        }
        staalTestVoedingsbodemRepository.deleteByStaalId(staalId);
        staalTestRepository.deleteByStaalId(staalId);
    }

    public List<Voedingsbodem> findPossibleVoedingsbodems(Long staalId) {
        List<Long> testIds = staalTestRepository.findTestIdsByStaalId(staalId);
        if (testIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Voedingsbodem> raw = testVoedingsbodemRepository.findDistinctVoedingsbodemsByTestIds(testIds);
        Set<Long> seen = new LinkedHashSet<>();
        List<Voedingsbodem> out = new ArrayList<>();
        for (Voedingsbodem vb : raw) {
            if (vb == null || vb.getId() == null || seen.contains(vb.getId())) {
                continue;
            }
            seen.add(vb.getId());
            out.add(vb);
        }
        out.sort(Comparator.comparing(Voedingsbodem::getNaam, Comparator.nullsLast(String::compareToIgnoreCase)));
        return out;
    }

    public void confirmVoedingsbodems(Long staalId, MicrobiologyVoedingsbodemsConfirmRequest body) {
        if (body == null || body.getVoedingsbodemIds() == null) {
            throw new IllegalArgumentException("voedingsbodemIds required");
        }
        if (!staalRepository.existsById(staalId)) {
            throw new EntityNotFoundException("Staal not found with id: " + staalId);
        }

        List<Voedingsbodem> allowed = findPossibleVoedingsbodems(staalId);
        Set<Long> allowedIds = allowed.stream().map(Voedingsbodem::getId).collect(Collectors.toSet());

        List<StaalTest> staalTests = staalTestRepository.findByStaalId(staalId);
        staalTestVoedingsbodemRepository.deleteByStaalId(staalId);
        for (Long vbId : body.getVoedingsbodemIds()) {
            if (vbId == null || !allowedIds.contains(vbId)) {
                throw new IllegalArgumentException("Invalid voedingsbodem id: " + vbId);
            }
            Voedingsbodem vb = voedingsbodemRepository.findById(vbId)
                    .orElseThrow(() -> new IllegalArgumentException("Voedingsbodem not found: " + vbId));
            for (StaalTest st : staalTests) {
                Hibernate.initialize(st.getTest());
                Long tid = st.getTest().getId();
                if (testVoedingsbodemRepository.existsByTest_IdAndVoedingsbodem_Id(tid, vbId)) {
                    StaalTestVoedingsbodem link = new StaalTestVoedingsbodem();
                    link.setStaalTest(st);
                    link.setVoedingsbodem(vb);
                    staalTestVoedingsbodemRepository.save(link);
                }
            }
        }
    }

    public ResponseEntity<Staal> update(Long id, MicrobiologyStaalUpdateRequest body) {
        if (body == null || body.getStaalTypeId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Staal existing = staalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + id));

        if (body.getStaalCode() != null
                && !body.getStaalCode().equals(existing.getStaalCode())
                && staalRepository.existsByStaalCodeAndIdNot(body.getStaalCode(), id)) {
            throw new UniqueConstraintViolationException("Staalcode already exists");
        }

        final Long staalTypeId = body.getStaalTypeId();
        StaalType type = staalTypeRepository.findById(staalTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid staalTypeId"));

        existing.setStaalCode(body.getStaalCode());
        existing.setPatientVoornaam(body.getPatientVoornaam());
        existing.setPatientAchternaam(body.getPatientAchternaam());
        existing.setPatientGeboorteDatum(body.getPatientGeboorteDatum());
        if (body.getPatientGeslacht() != null && !body.getPatientGeslacht().isEmpty()) {
            existing.setPatientGeslacht(body.getPatientGeslacht().charAt(0));
        }
        existing.setLaborantNaam(body.getLaborantNaam());
        existing.setLaborantRnummer(body.getLaborantRnummer());
        existing.setStaalType(type);

        Staal saved = staalRepository.save(existing);
        initializeForJson(saved);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    public ResponseEntity<Void> delete(Long id) {
        Staal existing = staalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + id));
        try {
            staalTestVoedingsbodemRepository.deleteByStaalId(id);
            staalTestRepository.deleteByStaalId(id);
            staalRepository.delete(existing);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
