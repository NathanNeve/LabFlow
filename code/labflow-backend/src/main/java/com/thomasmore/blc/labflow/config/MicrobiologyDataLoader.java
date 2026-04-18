package com.thomasmore.blc.labflow.config;

import com.thomasmore.blc.labflow.entity.microbiology.Antibiotica;
import com.thomasmore.blc.labflow.entity.microbiology.Staal;
import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.entity.microbiology.Test;
import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodemId;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;
import com.thomasmore.blc.labflow.repository.microbiology.AntibioticaRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;
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

import java.time.LocalDateTime;
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

    /** Mock patient stalen for development dashboards (must match frontend R-nummer pattern ^[RU]\\d{7}$). */
    private record MockStaalEntry(long staalCode, String voornaam, String achternaam, LocalDateTime geboorte,
                                  char geslacht, String laborantNaam, String laborantRnummer, String staalTypeNaam) {}

    private static final List<MockStaalEntry> MOCK_STALEN = List.of(
            new MockStaalEntry(602600001L, "Emma", "Janssen", LocalDateTime.of(1992, 3, 14, 0, 0), 'V',
                    "Lisa Vermeulen", "R1000001", "Urine"),
            new MockStaalEntry(602600002L, "Lucas", "Peeters", LocalDateTime.of(1987, 11, 21, 0, 0), 'M',
                    "Tom Willems", "U1000002", "Faeces"),
            new MockStaalEntry(602600003L, "Marie", "Dupont", LocalDateTime.of(2001, 7, 9, 0, 0), 'V',
                    "Sophie Jacobs", "R1000003", "CSV"),
            new MockStaalEntry(602600004L, "Noah", "De Smet", LocalDateTime.of(1995, 1, 18, 0, 0), 'M',
                    "Pieter Claes", "R1000004", "Sputum"),
            new MockStaalEntry(602600005L, "Elise", "Van den Broeck", LocalDateTime.of(1989, 9, 30, 0, 0), 'V',
                    "Katrien Maes", "U1000005", "Bronchusaspiraat"),
            new MockStaalEntry(602600006L, "Victor", "Hermans", LocalDateTime.of(1968, 5, 22, 0, 0), 'M',
                    "Jan Goossens", "R1000006", "Wisser keel"),
            new MockStaalEntry(602600007L, "Tessa", "Wouters", LocalDateTime.of(2004, 12, 5, 0, 0), 'V',
                    "Anouk Peeters", "R1000007", "Neus"),
            new MockStaalEntry(602600008L, "Ruben", "Declercq", LocalDateTime.of(1990, 4, 12, 0, 0), 'M',
                    "David Van Damme", "U1000008", "Keel"),
            new MockStaalEntry(602600009L, "Julie", "Martens", LocalDateTime.of(1976, 8, 27, 0, 0), 'V',
                    "Els De Vries", "R1000009", "Punctievocht"),
            new MockStaalEntry(602600010L, "Sam", "Aerts", LocalDateTime.of(1999, 2, 3, 0, 0), 'X',
                    "Nick Vervoort", "R1000010", "Genitaal"),
            new MockStaalEntry(602600011L, "Laura", "Segers", LocalDateTime.of(1984, 10, 16, 0, 0), 'V',
                    "Femke Van Hove", "U1000011", "Vagina"),
            new MockStaalEntry(602600012L, "Daan", "Cornelis", LocalDateTime.of(2008, 6, 20, 0, 0), 'M',
                    "Wouter Simons", "R1000012", "Urethra"),
            new MockStaalEntry(602600013L, "Femke", "Thijs", LocalDateTime.of(1993, 12, 11, 0, 0), 'V',
                    "Sara Lemmens", "R1000013", "BRP"),
            new MockStaalEntry(602600014L, "Robbe", "Nijs", LocalDateTime.of(1972, 5, 8, 0, 0), 'M',
                    "Geert Bosmans", "U1000014", "Perineum"),
            new MockStaalEntry(602600015L, "Mila", "Verhoeven", LocalDateTime.of(1997, 1, 25, 0, 0), 'V',
                    "Ine Van Loon", "R1000015", "Wisser diep"),
            new MockStaalEntry(602600016L, "Aaron", "Vervoort", LocalDateTime.of(1981, 4, 8, 0, 0), 'M',
                    "Niels Coppens", "R1000016", "Wisser opp"),
            new MockStaalEntry(602600017L, "Chiara", "Ruys", LocalDateTime.of(1996, 11, 19, 0, 0), 'V',
                    "Lotte Praet", "U1000017", "Wisser neus"),
            new MockStaalEntry(602600018L, "Finn", "Desmet", LocalDateTime.of(2006, 2, 27, 0, 0), 'M',
                    "Brent Maertens", "R1000018", "Faeces"),
            new MockStaalEntry(602600019L, "Zoë", "Van Rompaey", LocalDateTime.of(1979, 7, 13, 0, 0), 'V',
                    "Hanne Verschuren", "U1000019", "Urine"),
            new MockStaalEntry(602600020L, "Mats", "De Pauw", LocalDateTime.of(1991, 9, 6, 0, 0), 'M',
                    "Stijn Roels", "R1000020", "CSV"),
            new MockStaalEntry(602600021L, "Elena", "Verstraete", LocalDateTime.of(2003, 5, 31, 0, 0), 'V',
                    "Ophélie Tanghe", "U1000021", "Sputum"),
            new MockStaalEntry(602600022L, "Jamie", "Lefever", LocalDateTime.of(1988, 12, 22, 0, 0), 'X',
                    "Quentin Devos", "R1000022", "Bronchusaspiraat"),
            new MockStaalEntry(602600023L, "Isa", "Callens", LocalDateTime.of(1974, 6, 4, 0, 0), 'V',
                    "Merel Struyf", "U1000023", "Keel"),
            new MockStaalEntry(602600024L, "Otis", "Baert", LocalDateTime.of(1998, 8, 17, 0, 0), 'M',
                    "Vince De Clercq", "R1000024", "Neus"),
            new MockStaalEntry(602600025L, "Kaat", "De Witte", LocalDateTime.of(1994, 4, 2, 0, 0), 'V',
                    "Yara Smets", "U1000025", "Perineum"));

    private final AntibioticaRepository antibioticaRepository;
    private final StaalRepository staalRepository;
    private final StaalTypeRepository staalTypeRepository;
    private final TestRepository testRepository;
    private final VoedingsbodemRepository voedingsbodemRepository;
    private final TestVoedingsbodemRepository testVoedingsbodemRepository;

    public MicrobiologyDataLoader(AntibioticaRepository antibioticaRepository,
                                  StaalRepository staalRepository,
                                  StaalTypeRepository staalTypeRepository,
                                  TestRepository testRepository,
                                  VoedingsbodemRepository voedingsbodemRepository,
                                  TestVoedingsbodemRepository testVoedingsbodemRepository) {
        this.antibioticaRepository = antibioticaRepository;
        this.staalRepository = staalRepository;
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
        seedMockStalen();
        log.info("Microbiology reference data loaded: {} antibiotics, {} tests, {} mock stalen (idempotent upsert).",
                MOCK_ANTIBIOTICA_NAMEN.size(), TESTS.size(), MOCK_STALEN.size());
    }

    private void seedMockStalen() {
        for (MockStaalEntry m : MOCK_STALEN) {
            if (staalRepository.findByStaalCode(m.staalCode()).isPresent()) {
                continue;
            }
            StaalType type = staalTypeRepository.findByNaam(m.staalTypeNaam())
                    .orElseGet(() -> staalTypeRepository.save(new StaalType(m.staalTypeNaam())));
            Staal staal = new Staal();
            staal.setStaalCode(m.staalCode());
            staal.setPatientVoornaam(m.voornaam());
            staal.setPatientAchternaam(m.achternaam());
            staal.setPatientGeboorteDatum(m.geboorte());
            staal.setPatientGeslacht(m.geslacht());
            staal.setLaborantNaam(m.laborantNaam());
            staal.setLaborantRnummer(m.laborantRnummer());
            staal.setStaalType(type);
            staalRepository.save(staal);
        }
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
