// in deze file worden types gedefinieerd van de entiteiten in onze database

export interface User {
    id: number;
    email: string;
    voorNaam: string;
    achterNaam: string;
    wachtwoord: string;
    fullName: string;
    rol: Rol;
    newWachtwoord: string | null; // bestaat niet in db, wordt gebruikt voor wachtwoord wijzigen
    confirmDelete: boolean; // voor het verwijderen van een gebruiker
}

export interface Rol {
    id: number;
    naam: string;
}

export interface Staal {
    id: number;
    staalCode: string;
    patientAchternaam: string;
    patientVoornaam: string;
    patientGeboorteDatum: string;
    patientGeslacht: string;
    laborantNaam: string;
    laborantRnummer: string;
    aanmaakDatum: string;
    status: string;
    confirmDelete: boolean; // voor het verwijderen van een staal
    user: User; // gebruiker die dit staal heeft aangemaakt/aangepast
    registeredTests: Test[]; // testen die aan staal gekoppeld zijn/worden
}

export interface Test {
    id: number;
    testCode: string;
    naam: string;
    eenheid: Eenheid;
    testcategorie: TestCategorie;
    referentiewaardes: Referentiewaarde[];
    confirmDelete: boolean; // voor het verwijderen van een test
    test: Test; // wrapper
}

export interface Eenheid {
    id: number;
    afkorting: string;
    naam: string;
    confirmDelete: boolean; // voor het verwijderen van een eenheid
}

export interface TestCategorie {
    id: number;
    naam: string;
    kleur: string;
    kleurnaam: string;
    confirmDelete: boolean; // voor het verwijderen van een testcategorie
}

export interface Referentiewaarde {
    id: number;
    waarde: string;
    label: string;
}

/** Microbiology module – staal + staaltype */
export interface MicrobiologyStaalType {
    id: number;
    naam: string;
}

export interface MicrobiologyStaal {
    id: number;
    staalCode: number;
    patientAchternaam: string;
    patientVoornaam: string;
    patientGeboorteDatum: string;
    patientGeslacht: string;
    laborantNaam: string;
    laborantRnummer: string;
    staalType: MicrobiologyStaalType;
    commentaar?: string;
    voltooidAlgemeneTesten?: boolean;
    voltooidVoedingsbodems?: boolean;
    voltooidGramkleuring?: boolean;
    voltooidAntibiogram?: boolean;
    status?: string;
    confirmDelete?: boolean;
}

export interface MicrobiologyStaalTestDto {
    id: number;
    testId: number;
    testCode: string;
    testNaam: string;
    waarde: string | null;
    commentaar: string | null;
    failed: boolean;
}

export interface MicrobiologyVoedingsbodemLogEntry {
    id?: number;
    organisme: string;
    beoordeling: string;
    commentaar: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface MicrobiologyVoedingsbodemNotebookDto {
    linkId: number;
    voedingsbodemId: number;
    voedingsbodemNaam: string;
    commentaar: string | null;
    logs: MicrobiologyVoedingsbodemLogEntry[];
}

export interface MicrobiologyGramkleuringRowDto {
    bepaling: string;
    score: string;
    commentaar: string;
}

export interface MicrobiologyGramkleuringDto {
    staalTestId?: number;
    commentaar: string | null;
    rows: MicrobiologyGramkleuringRowDto[];
}

export interface MicrobiologyAntibiogramEntryDto {
    antibioticaId: number;
    antibioticaNaam: string;
    beoordeling: 'R' | 'S' | 'I';
}

export interface MicrobiologyNotebookResponse {
    id: number;
    staalCode: number;
    patientVoornaam: string;
    patientAchternaam: string;
    patientGeboorteDatum: string;
    patientGeslacht: string;
    commentaar: string | null;
    voltooidAlgemeneTesten: boolean;
    voltooidVoedingsbodems: boolean;
    voltooidGramkleuring: boolean;
    voltooidAntibiogram: boolean;
    status: string;
    activeSections: MicrobiologyNotebookSection[];
    algemeneTesten: MicrobiologyStaalTestDto[];
    voedingsbodems: MicrobiologyVoedingsbodemNotebookDto[];
    gramkleuring: MicrobiologyGramkleuringDto;
    antibiogram: MicrobiologyAntibiogramEntryDto[];
}

export type MicrobiologyNotebookSection =
    | 'algemene-testen'
    | 'voedingsbodems'
    | 'gramkleuring'
    | 'antibiogram';

export type MicrobiologyTestType = 'GRAMKLEURING' | 'ANTIBIOGRAM' | 'CULTUUR' | 'EXTRA_TEST';

/** Microbiology catalog test (GET /api/microbiology/tests) */
export interface MicrobiologyCatalogTest {
    id: number;
    testCode: string;
    naam: string;
    extraTest: boolean;
    testType: MicrobiologyTestType;
    staalType?: MicrobiologyStaalType;
    voedingsbodems?: string[];
}

export interface MicrobiologyVoedingsbodem {
    id: number;
    naam: string;
}