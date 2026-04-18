package com.thomasmore.blc.labflow.config;

import com.thomasmore.blc.labflow.entity.microbiology.Antibiotica;
import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.entity.microbiology.Test;
import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodemId;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;
import com.thomasmore.blc.labflow.repository.microbiology.AntibioticaRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTypeRepository;
import com.thomasmore.blc.labflow.repository.microbiology.TestRepository;
import com.thomasmore.blc.labflow.repository.microbiology.TestVoedingsbodemRepository;
import com.thomasmore.blc.labflow.repository.microbiology.VoedingsbodemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds microbiology reference data (mock antibiotics, staal types, tests, media,
 * test–medium links) into the microbiology SQLite database. Idempotent: safe on every startup.
 */
@Component
@Order(50)
public class MicrobiologyDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MicrobiologyDataLoader.class);

    private record TestEntry(String code, String naam, String staalTypeNaam, boolean extraTest, List<String> voedingsbodems) {}

    /** Mock antibiogram panel for development (common gram-positive / gram-negative coverage). */
    private static final List<String> MOCK_ANTIBIOTICA_NAMEN = List.of(
            "Amoxicilline",
            "Amoxicilline-clavulaanzuur",
            "Ampicilline",
            "Cefotaxim",
            "Ceftazidim",
            "Ceftriaxon",
            "Ciprofloxacine",
            "Clindamycine",
            "Erytromycine",
            "Gentamicine",
            "Meropenem",
            "Penicilline G",
            "Piperacilline-tazobactam",
            "Trimethoprim-sulfamethoxazol",
            "Vancomycine");

    private static final List<TestEntry> TESTS = List.of(
            new TestEntry("200", "Soortelijk gewicht", "Urine", false, List.of()),
            new TestEntry("201", "Sediment", "Urine", false, List.of()),
            new TestEntry("2101", "Urine gramkleuring", "Urine", false, List.of()),
            new TestEntry("2102", "Urine cultuur", "Urine", false, List.of("UTI", "BA", "McK")),
            new TestEntry("2103", "Urine antibiogram", "Urine", false, List.of()),
            new TestEntry("3101", "Sputum gramkleuring", "Sputum", false, List.of()),
            new TestEntry("3102", "Sputum cultuur", "Sputum", false, List.of("BA", "CHOCO", "MSA", "McK")),
            new TestEntry("3103", "Sputum antibiogram", "Sputum", false, List.of()),
            new TestEntry("3201", "Bronchusaspiraat gramkleuring", "Bronchusaspiraat", false, List.of()),
            new TestEntry("3202", "Bronchusaspiraat cultuur", "Bronchusaspiraat", false,
                    List.of("BA", "CHOCO", "MSA", "McK")),
            new TestEntry("3203", "Bronchusaspiraat antibiogram", "Bronchusaspiraat", false, List.of()),
            new TestEntry("311", "Influenza A/B", "BRP", true, List.of()),
            new TestEntry("312", "RSV", "BRP", true, List.of()),
            new TestEntry("313", "Adeno", "BRP", true, List.of()),
            new TestEntry("314", "Respiratoir panel", "BRP", true, List.of()),
            new TestEntry("315", "SARS-Covid-2", "BRP", true, List.of()),
            new TestEntry("316", "SARS-Covid (sneltest)", "BRP", true, List.of()),
            new TestEntry("4101", "Wonde diep gramkleuring", "Wisser diep", false, List.of()),
            new TestEntry("4102", "Wonde diep cultuur", "Wisser diep", false, List.of("BA", "Thio", "MSA", "McK")),
            new TestEntry("4103", "Wonde diep antibiogram", "Wisser diep", false, List.of()),
            new TestEntry("4201", "Wonde oppervlakkig gramkleuring", "Wisser opp", false, List.of()),
            new TestEntry("4202", "Wonde oppervlakkig cultuur", "Wisser opp", false,
                    List.of("BA", "Thio", "MSA", "McK")),
            new TestEntry("4203", "Wonde oppervlakkig antibiogram", "Wisser opp", false, List.of()),
            new TestEntry("4301", "Keel gramkleuring", "Wisser keel", false, List.of()),
            new TestEntry("4302", "Keel cultuur", "Wisser keel", false, List.of("BA")),
            new TestEntry("4303", "Keel antibiogram", "Wisser keel", false, List.of()),
            new TestEntry("4311", "Sneltest Streptococcus pyogenes", "Wisser keel", true, List.of()),
            new TestEntry("4401", "Neus gramkleuring", "Wisser neus", false, List.of()),
            new TestEntry("4402", "Neus cultuur", "Wisser neus", false, List.of("BA", "MSA")),
            new TestEntry("4403", "Neus antibiogram", "Wisser neus", false, List.of()),
            new TestEntry("5101", "Punctievocht gramkleuring", "Punctievocht", false, List.of()),
            new TestEntry("5102", "Punctievocht cultuur", "Punctievocht", false,
                    List.of("BA", "BA ANA", "CHOCO", "Thio", "MSA", "McK")),
            new TestEntry("5103", "Punctievocht antibiogram", "Punctievocht", false, List.of()),
            new TestEntry("6101", "Lumbaalvocht gramkleuring", "CSV", false, List.of()),
            new TestEntry("6102", "Lumbaalvocht cultuur", "CSV", false, List.of("BA", "Thio", "CHOCO")),
            new TestEntry("6103", "Lumbaalvocht antibiogram", "CSV", false, List.of()),
            new TestEntry("7101", "Vagina gramkleuring", "Vagina", false, List.of()),
            new TestEntry("7102", "Vagina cultuur", "Vagina", false,
                    List.of("BA", "TM", "Sabouraud", "MSA", "McK")),
            new TestEntry("7103", "Vagina antibiogram", "Vagina", false, List.of()),
            new TestEntry("7201", "Urethra gramkleuring", "Urethra", false, List.of()),
            new TestEntry("7202", "Urethra cultuur", "Urethra", false,
                    List.of("BA", "TM", "Sabouraud", "MSA", "McK")),
            new TestEntry("7203", "Urethra antibiogram", "Urethra", false, List.of()),
            new TestEntry("711", "GBS screening", "Genitaal", true, List.of()),
            new TestEntry("8101", "Faeces gramkleuring", "Faeces", false, List.of()),
            new TestEntry("8102", "Faeces cultuur", "Faeces", false,
                    List.of("McK", "XLD1", "CIN", "Seleniet", "Karmali", "Rappaport", "XLD2", "BGA")),
            new TestEntry("8103", "Faeces antibiogram", "Faeces", false, List.of()),
            new TestEntry("811", "Occult bloed", "Faeces", true, List.of()),
            new TestEntry("812", "Parasieten", "Faeces", true, List.of()),
            new TestEntry("813", "Rotavirus", "Faeces", true, List.of()),
            new TestEntry("814", "Adenovirus", "Faeces", true, List.of()),
            new TestEntry("815", "Norovirus", "Faeces", true, List.of()),
            new TestEntry("816", "Calprotectine", "Faeces", true, List.of()),
            new TestEntry("817", "Clostridium difficile", "Faeces", true, List.of()),
            new TestEntry("910", "MRSA Neus", "Neus", false, List.of("MRSA", "TSB")),
            new TestEntry("920", "MRSA Keel", "Keel", false, List.of("MRSA", "TSB")),
            new TestEntry("930", "MRSA Perineum", "Perineum", false, List.of("MRSA", "TSB")),
            new TestEntry("940", "VRE", "Perineum", false, List.of("VRE", "TSB")),
            new TestEntry("950", "CPE", "Perineum", false, List.of("CPE", "TSB")));

    private final AntibioticaRepository antibioticaRepository;
    private final StaalTypeRepository staalTypeRepository;
    private final TestRepository testRepository;
    private final VoedingsbodemRepository voedingsbodemRepository;
    private final TestVoedingsbodemRepository testVoedingsbodemRepository;

    public MicrobiologyDataLoader(AntibioticaRepository antibioticaRepository,
                                  StaalTypeRepository staalTypeRepository,
                                  TestRepository testRepository,
                                  VoedingsbodemRepository voedingsbodemRepository,
                                  TestVoedingsbodemRepository testVoedingsbodemRepository) {
        this.antibioticaRepository = antibioticaRepository;
        this.staalTypeRepository = staalTypeRepository;
        this.testRepository = testRepository;
        this.voedingsbodemRepository = voedingsbodemRepository;
        this.testVoedingsbodemRepository = testVoedingsbodemRepository;
    }

    @Override
    @Transactional(transactionManager = "microbiologyTransactionManager")
    public void run(String... args) {
        for (String naam : MOCK_ANTIBIOTICA_NAMEN) {
            findOrCreateAntibiotica(naam);
        }
        for (TestEntry entry : TESTS) {
            StaalType staalType = findOrCreateStaalType(entry.staalTypeNaam());
            Test test = findOrCreateTest(entry, staalType);
            for (String vbNaam : entry.voedingsbodems()) {
                Voedingsbodem vb = findOrCreateVoedingsbodem(vbNaam);
                linkTestVoedingsbodemIfAbsent(test, vb);
            }
        }
        log.info("Microbiology reference data loaded: {} antibiotics, {} tests (idempotent upsert).",
                MOCK_ANTIBIOTICA_NAMEN.size(), TESTS.size());
    }

    private Antibiotica findOrCreateAntibiotica(String naam) {
        return antibioticaRepository.findByNaam(naam)
                .orElseGet(() -> antibioticaRepository.save(new Antibiotica(naam)));
    }

    private StaalType findOrCreateStaalType(String naam) {
        return staalTypeRepository.findByNaam(naam).orElseGet(() -> staalTypeRepository.save(new StaalType(naam)));
    }

    private Voedingsbodem findOrCreateVoedingsbodem(String naam) {
        return voedingsbodemRepository.findByNaam(naam)
                .orElseGet(() -> voedingsbodemRepository.save(new Voedingsbodem(naam)));
    }

    private Test findOrCreateTest(TestEntry entry, StaalType staalType) {
        return testRepository.findByTestCode(entry.code()).orElseGet(() -> testRepository.save(
                new Test(entry.code(), entry.naam(), staalType, entry.extraTest())));
    }

    private void linkTestVoedingsbodemIfAbsent(Test test, Voedingsbodem voedingsbodem) {
        TestVoedingsbodemId id = new TestVoedingsbodemId(test.getId(), voedingsbodem.getId());
        if (testVoedingsbodemRepository.existsById(id)) {
            return;
        }
        TestVoedingsbodem link = new TestVoedingsbodem(test, voedingsbodem);
        testVoedingsbodemRepository.save(link);
    }
}
