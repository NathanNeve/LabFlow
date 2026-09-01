package com.thomasmore.blc.labflow.config;

import com.thomasmore.blc.labflow.entity.microbiology.Antibiotica;
import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.entity.microbiology.Test;
import com.thomasmore.blc.labflow.entity.microbiology.TestType;
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
 * Seeds microbiology reference data (antibiotics, staal types, tests, media,
 * test–medium links) into the microbiology SQLite database. Idempotent: safe on every startup.
 */
@Component
@Order(50)
public class MicrobiologyDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MicrobiologyDataLoader.class);

    private static final String URINE = "Urine";
    private static final String BRP = "Broncho-pulmonair (BRP)";
    private static final String WISSERS = "Wissers";
    private static final String PUNCTIEVOCHT = "Punctievocht";
    private static final String CSV = "C.S.V.";
    private static final String GENITAAL = "Genitaal";
    private static final String FAECES = "Faeces";
    private static final String SCREENING = "Screening";

    private record TestEntry(String code, String naam, String staalTypeNaam, TestType testType,
                             List<String> voedingsbodems) {}

    private static final List<String> ANTIBIOTICA_NAMEN = List.of(
            "Ampicilline",
            "Amoxicilline+clavulaanzuur",
            "Cefotaxime",
            "Trimethoprim+sulfa",
            "Nitrofurantoine",
            "Ofloxacine",
            "Cefoxitin",
            "Ceftazidime",
            "Cefuroxime",
            "Ciprofloxacine",
            "Colistine",
            "Fosfomycine",
            "Gentamicine",
            "Imipenem",
            "Meropenem",
            "Piperacilline/Tazobactam",
            "Tobramycine",
            "Trimethoprim");

    private static final List<TestEntry> TESTS = List.of(
            // Urine
            extra("2001", "Soortelijk gewicht", URINE),
            extra("2002", "pH", URINE),
            extra("2003", "Leukocyten", URINE),
            extra("2004", "Nitriet", URINE),
            extra("2005", "Proteïnen", URINE),
            extra("2006", "Glucose", URINE),
            extra("2007", "Ketonen", URINE),
            extra("2008", "Urobilinogeen", URINE),
            extra("2009", "Bilirubine", URINE),
            extra("2010", "Erytrocyten", URINE),
            extra("2011", "Hemoglobine", URINE),
            extra("201", "Sediment", URINE),
            gram("2101", "Urine gramkleuring", URINE),
            cultuur("2102", "Urine cultuur", URINE, "UTI", "BA", "McK"),
            antibiogram("2103", "Urine antibiogram", URINE),

            // Broncho-pulmonair (BRP)
            gram("3101", "Sputum gramkleuring", BRP),
            cultuur("3102", "Sputum cultuur", BRP, "BA", "CHOCO", "MSA", "McK"),
            antibiogram("3103", "Sputum antibiogram", BRP),
            gram("3201", "Bronchusaspiraat gramkleuring", BRP),
            cultuur("3202", "Bronchusaspiraat cultuur", BRP, "BA", "CHOCO", "MSA", "McK"),
            antibiogram("3203", "Bronchusaspiraat antibiogram", BRP),
            extra("311", "Influenza A/B", BRP),
            extra("312", "RSV", BRP),
            extra("313", "Adeno", BRP),
            extra("314", "Respiratoir panel", BRP),
            extra("315", "SARS-Covid-2", BRP),
            extra("316", "SARS-Covid (sneltest)", BRP),

            // Wissers
            gram("4101", "Wonde diep gramkleuring", WISSERS),
            cultuur("4102", "Wonde diep cultuur", WISSERS, "BA", "Thio", "MSA", "McK"),
            antibiogram("4103", "Wonde diep antibiogram", WISSERS),
            gram("4201", "Wonde oppervlakkig gramkleuring", WISSERS),
            cultuur("4202", "Wonde oppervlakkig cultuur", WISSERS, "BA", "Thio", "MSA", "McK"),
            antibiogram("4203", "Wonde oppervlakkig antibiogram", WISSERS),
            gram("4301", "Keel gramkleuring", WISSERS),
            cultuur("4302", "Keel cultuur", WISSERS, "BA"),
            antibiogram("4303", "Keel antibiogram", WISSERS),
            extra("4311", "Sneltest Streptococcus pyogenes", WISSERS),
            gram("4401", "Neus gramkleuring", WISSERS),
            cultuur("4402", "Neus cultuur", WISSERS, "BA", "MSA"),
            antibiogram("4403", "Neus antibiogram", WISSERS),

            // Punctievocht
            gram("5101", "Punctievocht gramkleuring", PUNCTIEVOCHT),
            cultuur("5102", "Punctievocht cultuur", PUNCTIEVOCHT, "BA", "BA ANA", "CHOCO", "Thio", "MSA", "McK"),
            antibiogram("5103", "Punctievocht antibiogram", PUNCTIEVOCHT),

            // C.S.V.
            gram("6101", "Lumbaalvocht gramkleuring", CSV),
            cultuur("6102", "Lumbaalvocht cultuur", CSV, "BA", "Thio", "CHOCO"),
            antibiogram("6103", "Lumbaalvocht antibiogram", CSV),

            // Genitaal
            gram("7101", "Vagina gramkleuring", GENITAAL),
            cultuur("7102", "Vagina cultuur", GENITAAL, "BA", "TM", "Sabouraud", "MSA", "McK"),
            antibiogram("7103", "Vagina antibiogram", GENITAAL),
            gram("7201", "Urethra gramkleuring", GENITAAL),
            cultuur("7202", "Urethra cultuur", GENITAAL, "BA", "TM", "Sabouraud", "MSA", "McK"),
            antibiogram("7203", "Urethra antibiogram", GENITAAL),
            extra("711", "GBS screening", GENITAAL),

            // Faeces
            gram("8101", "Faeces gramkleuring", FAECES),
            cultuur("8102", "Faeces cultuur", FAECES,
                    "McK", "XLD1", "GIN", "Seleniet", "Karmali", "Rappaport", "XLD2", "BGA"),
            antibiogram("8103", "Faeces antibiogram", FAECES),
            extra("811", "Occult bloed", FAECES),
            extra("812", "Parasieten", FAECES),
            extra("813", "Rotavirus", FAECES),
            extra("814", "Adenovirus", FAECES),
            extra("815", "Norovirus", FAECES),
            extra("816", "Calprotectine", FAECES),
            extra("817", "Clostridium difficile", FAECES),

            // Screening
            cultuur("910", "MRSA Neus", SCREENING, "MRSA", "TSB"),
            cultuur("920", "MRSA Keel", SCREENING, "MRSA", "TSB"),
            cultuur("930", "MRSA Perineum", SCREENING, "MRSA", "TSB"),
            cultuur("940", "VRE", SCREENING, "VRE", "TSB"),
            cultuur("950", "CPE", SCREENING, "CPE", "TSB"));

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
        for (String naam : ANTIBIOTICA_NAMEN) {
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
                ANTIBIOTICA_NAMEN.size(), TESTS.size());
    }

    private static TestEntry extra(String code, String naam, String staalType) {
        return new TestEntry(code, naam, staalType, TestType.EXTRA_TEST, List.of());
    }

    private static TestEntry gram(String code, String naam, String staalType) {
        return new TestEntry(code, naam, staalType, TestType.GRAMKLEURING, List.of());
    }

    private static TestEntry antibiogram(String code, String naam, String staalType) {
        return new TestEntry(code, naam, staalType, TestType.ANTIBIOGRAM, List.of());
    }

    private static TestEntry cultuur(String code, String naam, String staalType, String... voedingsbodems) {
        return new TestEntry(code, naam, staalType, TestType.CULTUUR, List.of(voedingsbodems));
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
        boolean extraTest = entry.testType() == TestType.EXTRA_TEST;
        return testRepository.findByTestCode(entry.code()).orElseGet(() -> testRepository.save(
                new Test(entry.code(), entry.naam(), staalType, extraTest, entry.testType())));
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
