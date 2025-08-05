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