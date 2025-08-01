import { generalFetch } from './globalFunctions';
import type { Eenheid, TestCategorie } from './types/dbTypes';

let testcategorieën: TestCategorie[] = [];
let eenheden: Eenheid[] = [];

// laden categorieën
export async function loadTestCategorieën() {
        try {
            testcategorieën = await generalFetch('GET', 'testcategorieen', true);
            return testcategorieën;
        } catch (error) {
            console.error("testcategorieën konden niet gefetched worden:", error);
        }
}

// laden eenheden voor popup test aanmaken
export async function loadEenheden() {
        try {
            eenheden = await generalFetch('GET', 'readeenheid', true);
            return eenheden;
        } catch (error) {
            console.error("eenheden kon niet gefetched worden:", error);
        }
}

// fetchen van stalen
export async function fetchStalen() {
        try {
            const stalen = await generalFetch('GET', 'stalen', true);
            const filteredStalen = stalen;
            return { stalen, filteredStalen };
        } catch (error) {
            console.error("Stalen konden niet gefetched worden:", error);
        }
}

// fetchen van stalen
export async function fetchStaal_StaalCode(staalCode: string) {
        try {
            const staal = await generalFetch('GET', `staal`, true, staalCode);
            return staal;
        } catch (error) {
            console.error("Staal kon niet gefetched worden:", error);
        }
}

// fetchen van users
export async function fetchUsers() {
        try {
            const users = await generalFetch('GET', 'getusers', false);
            return users;
        } catch (error) {
            console.error("Users konden niet gefetched worden: ", error);
        }
}

// fetchen van rollen
export async function fetchRollen() {
        try {
            const rollen = await generalFetch('GET', 'rollen', true);
            return rollen;
        } catch (error) {
            console.error("Rollen konden niet gefetched worden:", error);
        }
}

// fetchen van tests
export async function fetchTests() {
        try {
            const tests = await generalFetch('GET', 'tests', true);
            return tests;
        } catch (error) {
            console.error("Tests konden niet gefetched worden:", error);
        }
}

// fetchen van testcategorieën
export async function fetchTestcategorieën() {
    try {
        const categorieën = await generalFetch('GET', 'testcategorieen', true);
        return categorieën;
    } catch (error) {
        console.error("Categorieën konden niet gefetched worden:", error);
    }
}

// fetchen van eenheden
export async function fetchEenheden() {
    try {
        const eenheden = await generalFetch('GET', 'readeenheid', true);
        return eenheden;
    } catch (error) {
        console.error("Eenheden konden niet gefetched worden:", error);
    }
}

// fetchen van referentiewaarden
export async function fetchReferentiewaarden() {
    try {
        const referentiewaarden = await generalFetch('GET', 'referentiewaarden', true);
        return referentiewaarden;
    } catch (error) {
        console.error("Referentiewaarden konden niet gefetched worden:", error);
    }
}

// fetchen van statussen
export async function fetchStatussen() {
    try {
        const statussen = await generalFetch('GET', 'getstatus', true);
        return statussen;
    } catch (error) {
        console.error("Statussen konden niet gefetched worden:", error);
    }
}