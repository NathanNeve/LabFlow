package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyCatalogNameRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyCatalogTestRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyCatalogTestResponse;
import com.thomasmore.blc.labflow.entity.microbiology.Antibiotica;
import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.entity.microbiology.Test;
import com.thomasmore.blc.labflow.entity.microbiology.TestType;
import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;
import com.thomasmore.blc.labflow.repository.microbiology.AntibiogramRepository;
import com.thomasmore.blc.labflow.repository.microbiology.AntibioticaRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestVoedingsbodemRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTypeRepository;
import com.thomasmore.blc.labflow.repository.microbiology.TestRepository;
import com.thomasmore.blc.labflow.repository.microbiology.TestVoedingsbodemRepository;
import com.thomasmore.blc.labflow.repository.microbiology.VoedingsbodemRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional("microbiologyTransactionManager")
public class MicrobiologyCatalogService {

    @Autowired
    @Qualifier("microbiologyAntibioticaRepository")
    private AntibioticaRepository antibioticaRepository;

    @Autowired
    @Qualifier("microbiologyAntibiogramRepository")
    private AntibiogramRepository antibiogramRepository;

    @Autowired
    @Qualifier("microbiologyVoedingsbodemRepository")
    private VoedingsbodemRepository voedingsbodemRepository;

    @Autowired
    @Qualifier("microbiologyTestVoedingsbodemRepository")
    private TestVoedingsbodemRepository testVoedingsbodemRepository;

    @Autowired
    @Qualifier("microbiologyStaalTestVoedingsbodemRepository")
    private StaalTestVoedingsbodemRepository staalTestVoedingsbodemRepository;

    @Autowired
    @Qualifier("microbiologyStaalTypeRepository")
    private StaalTypeRepository staalTypeRepository;

    @Autowired
    @Qualifier("microbiologyTestRepository")
    private TestRepository testRepository;

    @Autowired
    @Qualifier("microbiologyStaalRepository")
    private StaalRepository staalRepository;

    @Autowired
    @Qualifier("microbiologyStaalTestRepository")
    private StaalTestRepository staalTestRepository;

    public List<Antibiotica> listAntibiotica() {
        return antibioticaRepository.findAllByOrderByNaamAsc();
    }

    public Antibiotica createAntibiotica(MicrobiologyCatalogNameRequest body) {
        String naam = requireNaam(body);
        if (antibioticaRepository.findByNaam(naam).isPresent()) {
            throw new UniqueConstraintViolationException("Antibiotica naam already exists");
        }
        return antibioticaRepository.save(new Antibiotica(naam));
    }

    public Antibiotica updateAntibiotica(Long id, MicrobiologyCatalogNameRequest body) {
        String naam = requireNaam(body);
        Antibiotica existing = antibioticaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Antibiotica not found with id: " + id));
        Optional<Antibiotica> duplicate = antibioticaRepository.findByNaam(naam);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new UniqueConstraintViolationException("Antibiotica naam already exists");
        }
        existing.setNaam(naam);
        return antibioticaRepository.save(existing);
    }

    public void deleteAntibiotica(Long id) {
        Antibiotica existing = antibioticaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Antibiotica not found with id: " + id));
        if (antibiogramRepository.existsByAntibiotica_Id(id)) {
            throw new IllegalStateException("Kan antibiotica niet verwijderen want deze is gelinked aan één of meerdere antibiogrammen");
        }
        antibioticaRepository.delete(existing);
    }

    public List<Voedingsbodem> listVoedingsbodems() {
        return voedingsbodemRepository.findAllByOrderByNaamAsc();
    }

    public Voedingsbodem createVoedingsbodem(MicrobiologyCatalogNameRequest body) {
        String naam = requireNaam(body);
        if (voedingsbodemRepository.findByNaam(naam).isPresent()) {
            throw new UniqueConstraintViolationException("Voedingsbodem naam already exists");
        }
        return voedingsbodemRepository.save(new Voedingsbodem(naam));
    }

    public Voedingsbodem updateVoedingsbodem(Long id, MicrobiologyCatalogNameRequest body) {
        String naam = requireNaam(body);
        Voedingsbodem existing = voedingsbodemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voedingsbodem not found with id: " + id));
        Optional<Voedingsbodem> duplicate = voedingsbodemRepository.findByNaam(naam);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new UniqueConstraintViolationException("Voedingsbodem naam already exists");
        }
        existing.setNaam(naam);
        return voedingsbodemRepository.save(existing);
    }

    public void deleteVoedingsbodem(Long id) {
        Voedingsbodem existing = voedingsbodemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voedingsbodem not found with id: " + id));
        if (testVoedingsbodemRepository.existsByVoedingsbodem_Id(id)
                || staalTestVoedingsbodemRepository.existsByVoedingsbodem_Id(id)) {
            throw new IllegalStateException("Kan voedingsbodem niet verwijderen want deze is gelinked aan één of meerdere tests of stalen");
        }
        voedingsbodemRepository.delete(existing);
    }

    public StaalType createStaalType(MicrobiologyCatalogNameRequest body) {
        String naam = requireNaam(body);
        if (staalTypeRepository.findByNaam(naam).isPresent()) {
            throw new UniqueConstraintViolationException("Staaltype naam already exists");
        }
        return staalTypeRepository.save(new StaalType(naam));
    }

    public StaalType updateStaalType(Long id, MicrobiologyCatalogNameRequest body) {
        String naam = requireNaam(body);
        StaalType existing = staalTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staaltype not found with id: " + id));
        Optional<StaalType> duplicate = staalTypeRepository.findByNaam(naam);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new UniqueConstraintViolationException("Staaltype naam already exists");
        }
        existing.setNaam(naam);
        return staalTypeRepository.save(existing);
    }

    public void deleteStaalType(Long id) {
        StaalType existing = staalTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staaltype not found with id: " + id));
        if (testRepository.existsByStaalType_Id(id) || staalRepository.existsByStaalType_Id(id)) {
            throw new IllegalStateException("Kan staaltype niet verwijderen want deze is gelinked aan één of meerdere tests of stalen");
        }
        staalTypeRepository.delete(existing);
    }

    public List<MicrobiologyCatalogTestResponse> listTests() {
        return testRepository.findAllByOrderByTestCodeAsc().stream()
                .map(this::toCatalogTestResponse)
                .toList();
    }

    public MicrobiologyCatalogTestResponse createTest(MicrobiologyCatalogTestRequest body) {
        String testCode = requireText(body != null ? body.getTestCode() : null, "testCode");
        String naam = requireText(body != null ? body.getNaam() : null, "naam");
        if (testRepository.findByTestCode(testCode).isPresent()) {
            throw new UniqueConstraintViolationException("Test code already exists");
        }
        TestType testType = parseTestType(body.getTestType());
        List<Long> voedingsbodemIds = voedingsbodemIdsForType(testType, body.getVoedingsbodemIds());
        requireCultuurHasVoedingsbodem(testType, voedingsbodemIds);
        StaalType staalType = requireStaalType(body.getStaalTypeId());
        Test test = testRepository.save(new Test(testCode, naam, staalType, body.isExtraTest(), testType));
        replaceVoedingsbodems(test, voedingsbodemIds);
        return toCatalogTestResponse(test);
    }

    public MicrobiologyCatalogTestResponse updateTest(Long id, MicrobiologyCatalogTestRequest body) {
        Test existing = testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + id));
        String testCode = requireText(body != null ? body.getTestCode() : null, "testCode");
        String naam = requireText(body != null ? body.getNaam() : null, "naam");
        Optional<Test> duplicate = testRepository.findByTestCode(testCode);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new UniqueConstraintViolationException("Test code already exists");
        }
        existing.setTestCode(testCode);
        existing.setNaam(naam);
        existing.setExtraTest(body.isExtraTest());
        TestType testType = parseTestType(body.getTestType());
        List<Long> voedingsbodemIds = voedingsbodemIdsForType(testType, body.getVoedingsbodemIds());
        requireCultuurHasVoedingsbodem(testType, voedingsbodemIds);
        existing.setTestType(testType);
        existing.setStaalType(requireStaalType(body.getStaalTypeId()));
        testRepository.save(existing);
        replaceVoedingsbodems(existing, voedingsbodemIds);
        return toCatalogTestResponse(existing);
    }

    public void deleteTest(Long id) {
        Test existing = testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + id));
        if (staalTestRepository.existsByTest_Id(id)) {
            throw new IllegalStateException("Kan test niet verwijderen want deze is gelinked aan één of meerdere stalen");
        }
        testVoedingsbodemRepository.deleteByTestId(id);
        testRepository.delete(existing);
    }

    private List<Long> voedingsbodemIdsForType(TestType testType, List<Long> voedingsbodemIds) {
        if (testType != TestType.CULTUUR) {
            return Collections.emptyList();
        }
        return voedingsbodemIds;
    }

    private void requireCultuurHasVoedingsbodem(TestType testType, List<Long> voedingsbodemIds) {
        if (testType != TestType.CULTUUR) {
            return;
        }
        boolean hasOne = voedingsbodemIds != null && voedingsbodemIds.stream().anyMatch(id -> id != null);
        if (!hasOne) {
            throw new IllegalArgumentException("At least one voedingsbodem is required for CULTUUR tests");
        }
    }

    private void replaceVoedingsbodems(Test test, List<Long> voedingsbodemIds) {
        testVoedingsbodemRepository.deleteByTestId(test.getId());
        if (voedingsbodemIds == null || voedingsbodemIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(voedingsbodemIds);
        for (Long voedingsbodemId : uniqueIds) {
            if (voedingsbodemId == null) {
                continue;
            }
            Voedingsbodem voedingsbodem = voedingsbodemRepository.findById(voedingsbodemId)
                    .orElseThrow(() -> new EntityNotFoundException("Voedingsbodem not found with id: " + voedingsbodemId));
            testVoedingsbodemRepository.save(new TestVoedingsbodem(test, voedingsbodem));
        }
    }

    private MicrobiologyCatalogTestResponse toCatalogTestResponse(Test test) {
        MicrobiologyCatalogTestResponse response = new MicrobiologyCatalogTestResponse();
        response.setId(test.getId());
        response.setTestCode(test.getTestCode());
        response.setNaam(test.getNaam());
        response.setExtraTest(test.isExtraTest());
        response.setTestType(test.getTestType() != null ? test.getTestType().name() : null);
        response.setStaalType(test.getStaalType());
        List<Voedingsbodem> voedingsbodems = test.getId() == null
                ? Collections.emptyList()
                : testVoedingsbodemRepository.findVoedingsbodemsByTestId(test.getId());
        response.setVoedingsbodems(voedingsbodems != null ? voedingsbodems : new ArrayList<>());
        return response;
    }

    private StaalType requireStaalType(Long staalTypeId) {
        if (staalTypeId == null) {
            throw new IllegalArgumentException("staalTypeId is required");
        }
        return staalTypeRepository.findById(staalTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Staaltype not found with id: " + staalTypeId));
    }

    private TestType parseTestType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("testType is required");
        }
        try {
            return TestType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid testType: " + raw);
        }
    }

    private String requireNaam(MicrobiologyCatalogNameRequest body) {
        return requireText(body != null ? body.getNaam() : null, "naam");
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
