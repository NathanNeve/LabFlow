package com.thomasmore.blc.labflow.config;
// file voor het seeden van de database met standaardwaarden
import com.thomasmore.blc.labflow.entity.*;
import com.thomasmore.blc.labflow.repository.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final EenheidRepository eenheidRepository;
    private final TestCategorieRepository testCategorieRepository;
    private final TestRepository testRepository;
    private final StaalRepository staalRepository;
    private final ReferentiewaardeRepository referentiewaardeRepository;
    private final StaalTestRepository staalTestRepository;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);


    public DataLoader(UserRepository userRepository,
                      RolRepository rolRepository,
                      EenheidRepository eenheidRepository,
                      TestCategorieRepository testCategorieRepository,
                      TestRepository testRepository,
                      StaalRepository staalRepository,
                      ReferentiewaardeRepository referentiewaardeRepository, StaalTestRepository staalTestRepository) {
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
        this.eenheidRepository = eenheidRepository;
        this.testCategorieRepository = testCategorieRepository;
        this.testRepository = testRepository;
        this.staalRepository = staalRepository;
        this.referentiewaardeRepository = referentiewaardeRepository;
        this.staalTestRepository = staalTestRepository;
    }

    @Override
    public void run(String... args) throws Exception {


        String admin_password = "USER_ADMIN_PASSWORD";
        String nathan_password = "USER_NATHAN_PASSWORD";
        String cesar_password = "USER_CESAR_PASSWORD";

        // Neem de env variabelen van render.com (production)
        String adminPw = System.getenv(admin_password);
        String nathanPw = System.getenv(nathan_password);
        String cesarPw = System.getenv(cesar_password);

        // anders nemen we ze van .env file (dev)
        String adminPassword;
        String cesarPassword;
        String nathanPassword;
        if (adminPw == null || nathanPw == null || cesarPw == null) {
            Dotenv dotenv = Dotenv.configure()
                    .load();

            // encryptie van de passwoorden in .env
            adminPassword = encoder.encode(dotenv.get(admin_password));
            nathanPassword = encoder.encode(dotenv.get(nathan_password));
            cesarPassword = encoder.encode(dotenv.get(cesar_password));
        } else {
            adminPassword = adminPw;
            nathanPassword = nathanPw;
            cesarPassword = cesarPw;
        }


        // aanmaken rollen
        Rol rol_admin = new Rol("admin");
        Rol rol_student = new Rol("student");
        rolRepository.save(rol_admin);
        rolRepository.save(rol_student);


        // aanmaken users
        User user0 = new User(adminPassword,
                "adminlabflow@digitalinnovation.be", "Admin", "DI", rol_admin);
        User user1 = new User(nathanPassword,
                "nathanneve@test.be", "Nathan", "Neve", rol_admin);
        User user2 = new User(cesarPassword,
                "césarvanleuffelen@test.be", "César", "van Leuffelen", rol_student);
        userRepository.save(user0);
        userRepository.save(user1);
        userRepository.save(user2);


        // aanmaken testcategorie
        Testcategorie edtaCat = new Testcategorie("EDTA", "#CA3DD4", "Paars");
        Testcategorie citraatCat = new Testcategorie("Citraat", "#12D3E9", "Blauw");
        Testcategorie serumCat = new Testcategorie("Serum", "#ED3A3A", "Rood");
        Testcategorie heparineCat = new Testcategorie("Heparine", "#2DE57A", "Groen");
        Testcategorie fluorideCat = new Testcategorie("Fluoride", "#8c8c8c", "Grijs");
        Testcategorie urineCat = new Testcategorie("Urine", "#ffcc40","Geel");
        Testcategorie noCat = new Testcategorie("Geen Categorie", "#ffffff", "Geen");
        testCategorieRepository.save(edtaCat);
        testCategorieRepository.save(citraatCat);
        testCategorieRepository.save(serumCat);
        testCategorieRepository.save(heparineCat);
        testCategorieRepository.save(fluorideCat);
        testCategorieRepository.save(urineCat);
        testCategorieRepository.save(noCat);


        // Stalen, dit is voor development, geen echte waarden
        Staal staal1 = new Staal(2024000001L, "César", "Van Leuffelen", java.time.LocalDate.parse("2004-07-29"), 'M', "Nathan Neve", "R1234567", user1);
        Staal staal2 = new Staal(2024000002L, "Lucas", "Peeters", java.time.LocalDate.parse("1985-07-21"), 'M', "Sofie", "R1234567", user2);
        Staal staal3 = new Staal(2024000003L, "Mila", "Vermeulen", java.time.LocalDate.parse("1993-02-11"), 'V', "Bart", "R1234567", user1);
        Staal staal4 = new Staal(2024000004L, "Liam", "Claes", java.time.LocalDate.parse("1992-11-30"), 'M', "Lies", "R1234567", user2);
        Staal staal5 = new Staal(2024000005L, "Olivia", "Dubois", java.time.LocalDate.parse("2000-01-08"), 'V', "An", "R1234567", user1);
        Staal staal6 = new Staal(2024000006L, "Noah", "De Smet", java.time.LocalDate.parse("1987-03-16"), 'M', "Koen", "R1234567", user2);
        Staal staal7 = new Staal(2024000007L, "Marie", "De Vries", java.time.LocalDate.parse("1991-09-25"), 'V', "Katrien", "R1234567", user1);
        Staal staal8 = new Staal(2024000008L, "Arthur", "Van Damme", java.time.LocalDate.parse("1988-12-19"), 'M', "Jan", "R1234567", user2);
        Staal staal9 = new Staal(2024000009L, "Charlotte", "Jacobs", java.time.LocalDate.parse("1995-04-03"), 'V', "Eva", "R1234567", user1);
        Staal staal10 = new Staal(2024000010L, "Victor", "Maes", java.time.LocalDate.parse("1990-06-10"), 'M', "Lotte", "R1234567", user2);

        staalRepository.save(staal1);
        staalRepository.save(staal2);
        staalRepository.save(staal3);
        staalRepository.save(staal4);
        staalRepository.save(staal5);
        staalRepository.save(staal6);
        staalRepository.save(staal7);
        staalRepository.save(staal8);
        staalRepository.save(staal9);
        staalRepository.save(staal10);


        // aanmaken eenheden
        Eenheid gramPerDeciliter = new Eenheid("gram per deciliter", "g/dl");
        Eenheid percentage = new Eenheid("percentage", "%");
        Eenheid cellsPerMicroliter = new Eenheid("cells per microliter", "cellen/µl");
        Eenheid femtoliter = new Eenheid("femtoliter", "fl");
        Eenheid picogram = new Eenheid("picogram", "pg");
        Eenheid plateletsPerMicroliter = new Eenheid("platelets per microliter", "PLT/µl");
        Eenheid per1000RedBloodCells = new Eenheid("per 1000 red blood cells", "per 1000 RBC");
        Eenheid notAvailable = new Eenheid("not available", "/");
        Eenheid seconds = new Eenheid("seconds", "sec");
        Eenheid millimetersPerHour = new Eenheid("millimeters per hour", "mm/u");
        Eenheid internationalUnitsPerMilliliter = new Eenheid("international units per milliliter", "IU/ml");
        Eenheid milligramsPerDeciliter = new Eenheid("milligrams per deciliter", "mg/dL");
        Eenheid micromolesPerLiter = new Eenheid("micromoles per liter", "µmol/L");
        Eenheid millimolesPerLiter = new Eenheid("millimoles per liter", "mmol/L");
        Eenheid milliosmolesPerKilogram = new Eenheid("milliosmoles per kilogram", "mOsmol/kg");
        Eenheid unitsPerLiter = new Eenheid("units per liter", "U/L");
        Eenheid gramsPerLiter = new Eenheid("grams per liter", "g/L");
        Eenheid milligramsPerLiter = new Eenheid("milligrams per liter", "mg/L");
        Eenheid nanogramsPerMilliliter = new Eenheid("nanograms per milliliter", "ng/mL");
        Eenheid nanogramsPerLiter = new Eenheid("nanograms per liter", "ng/L");
        Eenheid microgramsPerDeciliter = new Eenheid("micrograms per deciliter", "µg/dL");
        Eenheid microgramsPerLiter = new Eenheid("micrograms per liter", "µg/L");
        Eenheid milliunitsPerLiter = new Eenheid("milliunits per liter", "mU/L");
        Eenheid picomolesPerLiter = new Eenheid("picomoles per liter", "pmol/L");
        Eenheid millilitersPerMinute = new Eenheid("milliliters per minute", "mL/min");

        eenheidRepository.save(gramPerDeciliter);
        eenheidRepository.save(percentage);
        eenheidRepository.save(cellsPerMicroliter);
        eenheidRepository.save(femtoliter);
        eenheidRepository.save(picogram);
        eenheidRepository.save(plateletsPerMicroliter);
        eenheidRepository.save(per1000RedBloodCells);
        eenheidRepository.save(notAvailable);
        eenheidRepository.save(seconds);
        eenheidRepository.save(millimetersPerHour);
        eenheidRepository.save(internationalUnitsPerMilliliter);
        eenheidRepository.save(milligramsPerDeciliter);
        eenheidRepository.save(micromolesPerLiter);
        eenheidRepository.save(millimolesPerLiter);
        eenheidRepository.save(milliosmolesPerKilogram);
        eenheidRepository.save(unitsPerLiter);
        eenheidRepository.save(gramsPerLiter);
        eenheidRepository.save(milligramsPerLiter);
        eenheidRepository.save(nanogramsPerMilliliter);
        eenheidRepository.save(nanogramsPerLiter);
        eenheidRepository.save(microgramsPerDeciliter);
        eenheidRepository.save(microgramsPerLiter);
        eenheidRepository.save(milliunitsPerLiter);
        eenheidRepository.save(picomolesPerLiter);
        eenheidRepository.save(millilitersPerMinute);


        // Aanmaken van tests met hun codes en opslaan
        Test test1 = new Test("X", "Notitie", notAvailable, noCat);
        testRepository.save(test1);
        Test test601 = new Test("601", "Hemoglobine", gramPerDeciliter, edtaCat);
        testRepository.save(test601);
        Test test602 = new Test("602", "Hematocriet", percentage, edtaCat);
        testRepository.save(test602);
        Test test603 = new Test("603", "RBC", cellsPerMicroliter, edtaCat);
        testRepository.save(test603);
        Test test604 = new Test("604", "MCV", femtoliter, edtaCat);
        testRepository.save(test604);
        Test test605 = new Test("605", "MCH", picogram, edtaCat);
        testRepository.save(test605);
        Test test606 = new Test("606", "MCHC", gramPerDeciliter, edtaCat);
        testRepository.save(test606);
        Test test607 = new Test("607", "WBC", cellsPerMicroliter, edtaCat);
        testRepository.save(test607);
        Test test608 = new Test("608", "Formule: staafkerninge neutrofielen, segmentkerninge neutrofielen, lymfocyten, monocyten, eosinofielen, basofielen", notAvailable, edtaCat);
        testRepository.save(test608);
        Test test609 = new Test("609", "Thrombocyten", plateletsPerMicroliter, edtaCat);
        testRepository.save(test609);
        Test test610 = new Test("610", "Reticulocyten", cellsPerMicroliter, edtaCat);
        testRepository.save(test610);
        Test test611 = new Test("611", "Foetale RBC", notAvailable, edtaCat);
        testRepository.save(test611);
        Test test64 = new Test("64", "Bloedgroep ABO-D", notAvailable, edtaCat);
        testRepository.save(test64);
        Test test65 = new Test("65", "Cc Ee Kell", notAvailable, edtaCat);
        testRepository.save(test65);
        Test test66 = new Test("66", "Screening op irregulier AL", notAvailable, edtaCat);
        testRepository.save(test66);
        Test test67 = new Test("67", "Identificatie van irreguliere AL", notAvailable, edtaCat);
        testRepository.save(test67);
        Test test68 = new Test("68", "Kruisproef", notAvailable, edtaCat);
        testRepository.save(test68);
        Test test630 = new Test("630", "PT", seconds, citraatCat);
        testRepository.save(test630);
        Test test631 = new Test("631", "APTT", seconds, citraatCat);
        testRepository.save(test631);
        Test test221 = new Test("221", "ESR", millimetersPerHour, edtaCat);
        testRepository.save(test221);
        Test test222 = new Test("222", "Reumafactor", notAvailable, serumCat);
        testRepository.save(test222);
        Test test223 = new Test("223", "ANA", notAvailable, serumCat);
        testRepository.save(test223);
        Test test224 = new Test("224", "HLA B27", notAvailable, edtaCat);
        testRepository.save(test224);
        Test test280 = new Test("280", "Epstein Barr IgG", notAvailable, serumCat);
        testRepository.save(test280);
        Test test281 = new Test("281", "Epstein Barr Paul & Bunnell", notAvailable, serumCat);
        testRepository.save(test281);
        Test test270 = new Test("270", "ASLO", notAvailable, serumCat);
        testRepository.save(test270);
        Test test727 = new Test("727", "Flow cytometrie volbloed", internationalUnitsPerMilliliter, edtaCat);
        testRepository.save(test727);
        Test test728 = new Test("728", "Immunofentoypering", notAvailable, edtaCat);
        testRepository.save(test728);
        Test test729 = new Test("729", "Karyotypering", notAvailable, heparineCat);
        testRepository.save(test729);
        Test test730 = new Test("730", "Glucose 6 fosfaat dehydrogenase", notAvailable, edtaCat);
        testRepository.save(test730);
        Test test100 = new Test("100", "Glucose", milligramsPerDeciliter, fluorideCat);
        testRepository.save(test100);
        Test test103 = new Test("103", "Hemoglobine A1c", percentage, edtaCat);
        testRepository.save(test103);
        Test test120 = new Test("120", "Cholesterol", milligramsPerDeciliter, serumCat);
        testRepository.save(test120);
        Test test121 = new Test("121", "HDL-cholesterol", milligramsPerDeciliter, serumCat);
        testRepository.save(test121);
        Test test122 = new Test("122", "LDL-Cholesterol", milligramsPerDeciliter, serumCat);
        testRepository.save(test122);
        Test test123 = new Test("123", "Triglyceriden", milligramsPerDeciliter, serumCat);
        testRepository.save(test123);
        Test test130 = new Test("130", "Ureum", milligramsPerDeciliter, serumCat);
        testRepository.save(test130);
        Test test131 = new Test("131", "Creatinine", milligramsPerDeciliter, serumCat);
        testRepository.save(test131);
        Test test132 = new Test("132", "Urinezuur", micromolesPerLiter, serumCat);
        testRepository.save(test132);
        Test test141 = new Test("141", "Natrium", millimolesPerLiter, serumCat);
        testRepository.save(test141);
        Test test142 = new Test("142", "Kalium", millimolesPerLiter, serumCat);
        testRepository.save(test142);
        Test test143 = new Test("143", "Chloride", millimolesPerLiter, serumCat);
        testRepository.save(test143);
        Test test144 = new Test("144", "Bicarbonaat", millimolesPerLiter, serumCat);
        testRepository.save(test144);
        Test test145 = new Test("145", "Calcium", milligramsPerDeciliter, serumCat);
        testRepository.save(test145);
        Test test156 = new Test("156", "Fosfaat", milligramsPerDeciliter, serumCat);
        testRepository.save(test156);
        Test test157 = new Test("157", "Magnesium", milligramsPerDeciliter, serumCat);
        testRepository.save(test157);
        Test test203 = new Test("203", "Osmolaliteit", milliosmolesPerKilogram, serumCat);
        testRepository.save(test203);
        Test test180 = new Test("180", "AST", unitsPerLiter, serumCat);
        testRepository.save(test180);
        Test test181 = new Test("181", "ALT", unitsPerLiter, serumCat);
        testRepository.save(test181);
        Test test182 = new Test("182", "AF", unitsPerLiter, serumCat);
        testRepository.save(test182);
        Test test184 = new Test("184", "GGT", unitsPerLiter, serumCat);
        testRepository.save(test184);
        Test test194 = new Test("194", "Bilirubine totaal", milligramsPerDeciliter, serumCat);
        testRepository.save(test194);
        Test test195 = new Test("195", "Bilirubine direct", milligramsPerDeciliter, serumCat);
        testRepository.save(test195);
        Test test185 = new Test("185", "LDH", unitsPerLiter, serumCat);
        testRepository.save(test185);
        Test test186 = new Test("186", "CK", unitsPerLiter, serumCat);
        testRepository.save(test186);
        Test test187 = new Test("187", "Troponine", unitsPerLiter, heparineCat);
        testRepository.save(test187);
        Test test188 = new Test("188", "CK-MB", unitsPerLiter, heparineCat);
        testRepository.save(test188);
        Test test160 = new Test("160", "Totaal eiwit", gramsPerLiter, serumCat);
        testRepository.save(test160);
        Test test161 = new Test("161", "Albumine", gramsPerLiter, serumCat);
        testRepository.save(test161);
        Test test162 = new Test("162", "Eiwit elektroforese", percentage, serumCat);
        testRepository.save(test162);
        Test test163 = new Test("163", "Immuunfixatie", notAvailable, serumCat);
        testRepository.save(test163);
        Test test164 = new Test("164", "CRP", milligramsPerLiter, serumCat);
        testRepository.save(test164);
        Test test210 = new Test("210", "Vit. D3 (25-OH)", microgramsPerDeciliter, serumCat);
        testRepository.save(test210);
        Test test211 = new Test("211", "Vit. B12", microgramsPerLiter, serumCat);
        testRepository.save(test211);
        Test test620 = new Test("620", "Ijzer", milligramsPerDeciliter, serumCat);
        testRepository.save(test620);
        Test test621 = new Test("621", "TIBC", milligramsPerDeciliter, serumCat);
        testRepository.save(test621);
        Test test622 = new Test("622", "Transferrine", gramsPerLiter, serumCat);
        testRepository.save(test622);
        Test test623 = new Test("623", "Foliumzuur", milligramsPerDeciliter, serumCat);
        testRepository.save(test623);
        Test test624 = new Test("624", "Ferritine", milligramsPerDeciliter, serumCat);
        testRepository.save(test624);
        Test test390 = new Test("390", "TSH", milliunitsPerLiter, serumCat);
        testRepository.save(test390);
        Test test391 = new Test("391", "Vrije T3", picomolesPerLiter, serumCat);
        testRepository.save(test391);
        Test test392 = new Test("392", "Vrije T4", picomolesPerLiter, serumCat);
        testRepository.save(test392);
        Test test430 = new Test("430", "HCG", unitsPerLiter, serumCat);
        testRepository.save(test430);
        Test test431 = new Test("431", "LH", unitsPerLiter, serumCat);
        testRepository.save(test431);
        Test test550 = new Test("550", "Totaal Eiwit", gramsPerLiter, urineCat);
        testRepository.save(test550);
        Test test551 = new Test("551", "Glucose", gramsPerLiter, urineCat);
        testRepository.save(test551);
        Test test552 = new Test("552", "Osmolaliteit", milliosmolesPerKilogram, urineCat);
        testRepository.save(test552);
        Test test553 = new Test("553", "Creatinine clearance", millilitersPerMinute, urineCat);
        testRepository.save(test553);


        // referentiewaarden aanmaken
        // Hematologie (EDTA)
        referentiewaardeRepository.save(new Referentiewaarde("man 14-18", test601));
        referentiewaardeRepository.save(new Referentiewaarde("vrouw 12-16", test601));

        referentiewaardeRepository.save(new Referentiewaarde("man 40-52", test602));
        referentiewaardeRepository.save(new Referentiewaarde("vrouw 35-47", test602));

        referentiewaardeRepository.save(new Referentiewaarde("man 4,5-5,9 x 10⁶", test603));
        referentiewaardeRepository.save(new Referentiewaarde("vrouw 3,8-5,2 x 10⁶", test603));

        referentiewaardeRepository.save(new Referentiewaarde("89+5", test604));
        referentiewaardeRepository.save(new Referentiewaarde("29+2", test605));
        referentiewaardeRepository.save(new Referentiewaarde("33+2", test606));
        referentiewaardeRepository.save(new Referentiewaarde("4000-10000", test607));

        // Formule: staafkerninge neutrofielen, segmentkerninge neutrofielen, lymfocyten, monocyten, eosinofielen, basofielen
        referentiewaardeRepository.save(new Referentiewaarde("staafkerninge neutrofielen 5-16", test608));
        referentiewaardeRepository.save(new Referentiewaarde("segmentkerninge neutrofielen 50-70", test608));
        referentiewaardeRepository.save(new Referentiewaarde("lymfocyten 20-35", test608));
        referentiewaardeRepository.save(new Referentiewaarde(" monocyten 2-6", test608));
        referentiewaardeRepository.save(new Referentiewaarde("eosinofielen 0-1", test608));
        referentiewaardeRepository.save(new Referentiewaarde("basofielen 0-1", test608));

        referentiewaardeRepository.save(new Referentiewaarde("150000-400000", test609));
        referentiewaardeRepository.save(new Referentiewaarde("2-20", test610));
        referentiewaardeRepository.save(new Referentiewaarde("ja/nee", test611));

        // Immuunhematologie (EDTA)
        referentiewaardeRepository.save(new Referentiewaarde("A/B/AB/O", test64));
        referentiewaardeRepository.save(new Referentiewaarde("D+/D-", test64));

        referentiewaardeRepository.save(new Referentiewaarde("C/c/E/e +/-", test65));
        referentiewaardeRepository.save(new Referentiewaarde("Kell +/-", test65));

        referentiewaardeRepository.save(new Referentiewaarde(" +/-", test66));
        referentiewaardeRepository.save(new Referentiewaarde("hier graag een tekstvlak om zelf uitslag in te typen", test67));
        referentiewaardeRepository.save(new Referentiewaarde(" +/-", test68));

        // Hemostase (citraat)
        referentiewaardeRepository.save(new Referentiewaarde("10-15", test630));
        referentiewaardeRepository.save(new Referentiewaarde("32-42", test631));

        // Serologie Inflammatoir Rheuma & Auto-immuniteit (serum)
        referentiewaardeRepository.save(new Referentiewaarde("man 0-5", test221));
        referentiewaardeRepository.save(new Referentiewaarde("vrouw 0-7", test221));
        referentiewaardeRepository.save(new Referentiewaarde(" +/-", test222));
        referentiewaardeRepository.save(new Referentiewaarde("hier graag een tekstvlak om zelf uitslag in te typen", test223));
        referentiewaardeRepository.save(new Referentiewaarde(" +/-", test224));

        // Serologie Viraal (serum)
        referentiewaardeRepository.save(new Referentiewaarde("negatief ≤0,90 /hertesten 0,91-1,09 / positief ≥1,10", test280));
        referentiewaardeRepository.save(new Referentiewaarde(" +/-", test281));

        // Serologie Bacterieel (serum)
        referentiewaardeRepository.save(new Referentiewaarde("positief >200", test270));

        // Flow cytometrie (EDTA)
        referentiewaardeRepository.save(new Referentiewaarde("hier graag een tekstvlak om zelf uitslag in te typen", test727));
        referentiewaardeRepository.save(new Referentiewaarde("hier graag een tekstvlak om zelf uitslag in te typen", test728));

        // Genetica
        referentiewaardeRepository.save(new Referentiewaarde("hier graag een tekstvlak om zelf uitslag in te typen", test729));

        // Metabole aandoeningen
        referentiewaardeRepository.save(new Referentiewaarde("hier graag een tekstvlak om zelf uitslag in te typen", test730));

        // Biochemie - Suikers
        referentiewaardeRepository.save(new Referentiewaarde("70-105", test100));
        referentiewaardeRepository.save(new Referentiewaarde("4-6%", test103));

        // Biochemie - Vetten
        referentiewaardeRepository.save(new Referentiewaarde("<200", test120));
        referentiewaardeRepository.save(new Referentiewaarde(">60", test121));
        referentiewaardeRepository.save(new Referentiewaarde("<100", test122));
        referentiewaardeRepository.save(new Referentiewaarde("<150", test123));

        // Biochemie - Nier
        referentiewaardeRepository.save(new Referentiewaarde("15-39", test130));
        referentiewaardeRepository.save(new Referentiewaarde("man 0,9-1,3", test131));
        referentiewaardeRepository.save(new Referentiewaarde("vrouw 0,6-1,1", test131));
        referentiewaardeRepository.save(new Referentiewaarde("man 3,5-7,2", test132));
        referentiewaardeRepository.save(new Referentiewaarde("vrouw 2,6-6,0", test132));

        // Electrolytes
        referentiewaardeRepository.save(new Referentiewaarde("136-146", test141));
        referentiewaardeRepository.save(new Referentiewaarde("3,5-5,1", test142));
        referentiewaardeRepository.save(new Referentiewaarde("101-109", test143));
        referentiewaardeRepository.save(new Referentiewaarde("21-31", test144));
        referentiewaardeRepository.save(new Referentiewaarde("2,15-2,55", test145));
        referentiewaardeRepository.save(new Referentiewaarde("0,8-1,4", test156));
        referentiewaardeRepository.save(new Referentiewaarde("0,75-0,95", test157));
        referentiewaardeRepository.save(new Referentiewaarde("<61 jaar: 275-295, >61 jaar: 280-301", test203));

        // Enzymes
        referentiewaardeRepository.save(new Referentiewaarde("<50", test180));
        referentiewaardeRepository.save(new Referentiewaarde("<41", test181));
        referentiewaardeRepository.save(new Referentiewaarde("17-45", test182));
        referentiewaardeRepository.save(new Referentiewaarde("man: <22, vrouw: <15", test184));
        referentiewaardeRepository.save(new Referentiewaarde("<2", test194));
        referentiewaardeRepository.save(new Referentiewaarde("<0,3", test195));
        referentiewaardeRepository.save(new Referentiewaarde("<248", test185));
        referentiewaardeRepository.save(new Referentiewaarde("man: 10-65, vrouw: 7-55", test186));
        referentiewaardeRepository.save(new Referentiewaarde("man: <19,8, vrouw: <11,6", test187));
        referentiewaardeRepository.save(new Referentiewaarde("<25", test188));

        // Proteins
        referentiewaardeRepository.save(new Referentiewaarde("63-83", test160));
        referentiewaardeRepository.save(new Referentiewaarde("35-52", test161));
        referentiewaardeRepository.save(new Referentiewaarde("Albumine: 59.8-72.4", test162));
        referentiewaardeRepository.save(new Referentiewaarde("Alfa-1 globulinen: 1-3.2%", test162));
        referentiewaardeRepository.save(new Referentiewaarde("Alfa-2 globulinen: 7.4-12.6%", test162));
        referentiewaardeRepository.save(new Referentiewaarde("Bèta globulinen: 7.5-12.9%", test162));
        referentiewaardeRepository.save(new Referentiewaarde("Gamma globulinen: 8-15.8%", test162));
        referentiewaardeRepository.save(new Referentiewaarde("hier graag een tekstvlak om zelf uitslag in te typen", test163));

        referentiewaardeRepository.save(new Referentiewaarde("<5", test164)); // CRP
        referentiewaardeRepository.save(new Referentiewaarde("30-100", test210)); // Vit. D3 (25-OH)
        referentiewaardeRepository.save(new Referentiewaarde("180-914", test211)); // Vit. B12
        referentiewaardeRepository.save(new Referentiewaarde("man: 65-175, vrouw: 50-170", test620)); // Ijzer
        referentiewaardeRepository.save(new Referentiewaarde("250-425", test621)); // TIBC
        referentiewaardeRepository.save(new Referentiewaarde("2-3,60", test622)); // Transferrine
        referentiewaardeRepository.save(new Referentiewaarde(">3,5", test623)); // Foliumzuur
        referentiewaardeRepository.save(new Referentiewaarde("man: 25-250, vrouw: 20-250", test624)); // Ferritine
        referentiewaardeRepository.save(new Referentiewaarde("0,38-5,33", test390)); // TSH
        referentiewaardeRepository.save(new Referentiewaarde("3,85-6,01", test391)); // Vrije T3
        referentiewaardeRepository.save(new Referentiewaarde("7,85-14,41", test392)); // Vrije T4

        // Referentiewaarden for Biochemie - Gonaden tests
        referentiewaardeRepository.save(new Referentiewaarde("<5", test430)); // HCG
        referentiewaardeRepository.save(new Referentiewaarde("LH Mid-folliculair 2.1-10.9", test431)); // LH Mid-folliculair
        referentiewaardeRepository.save(new Referentiewaarde("LH Mid-cyclus 19.2-103", test431)); // LH Mid-cyclus
        referentiewaardeRepository.save(new Referentiewaarde("LH Mid-luteaal 1.2-12.9", test431)); // LH Mid-luteaal
        referentiewaardeRepository.save(new Referentiewaarde("LH Postmenopause 10.9-58.6", test431)); // LH Postmenopause
        referentiewaardeRepository.save(new Referentiewaarde("man 1.2-8.6", test431)); // LH man

        // Referentiewaarden for Biochemie - Urine tests
        referentiewaardeRepository.save(new Referentiewaarde("<0.16", test550)); // Totaal Eiwit
        referentiewaardeRepository.save(new Referentiewaarde("<0.15", test551)); // Glucose
        referentiewaardeRepository.save(new Referentiewaarde("50-1200", test552)); // Osmolaliteit
        referentiewaardeRepository.save(new Referentiewaarde("71-151", test553)); // Creatinine clearance

        // 3 tests toevoegen aan staal 1
        StaalTest staalTestx= new StaalTest(staal1, test1);
        StaalTest staalTest1 = new StaalTest(staal1, test601);
        StaalTest staalTest2 = new StaalTest(staal1, test602);
        StaalTest staalTest3 = new StaalTest(staal1, test630);
        staalTestx.setNote("this a note for notitie");
        staalTestx.setResult("dronken");
        staalTest1.setResult("15.0");
        staalTest2.setResult("45.0");
        staalTest1.setNote("This is a note for test 601");
        staalTestRepository.save(staalTestx);
        staalTestRepository.save(staalTest1);
        staalTestRepository.save(staalTest2);
        staalTestRepository.save(staalTest3);
    }
}