import { getCookie, fetchAll, fetchAllWithoutPrefix } from '$lib/globalFunctions';
import { goto } from '$app/navigation';
import type { Eenheid, TestCategorie } from './types/dbTypes';

const token = getCookie('authToken') ?? '';
let testcategorieën: TestCategorie[] = [];
let eenheden: Eenheid[] = [];

// laden categorieën
export async function loadTestCategorieën() {
    if (token != null) {
        try {
            testcategorieën = await fetchAll(token, 'testcategorieen');
            return testcategorieën;
        } catch (error) {
            console.error("testcategorieën konden niet gefetched worden:", error);
        }
    } else {
        console.error("jwt error");
        goto('/');
    }
}

// laden eenheden voor popup test aanmaken
export async function loadEenheden() {
    if (token != null) {
        try {
            eenheden = await fetchAll(token, 'readeenheid');
            return eenheden;
        } catch (error) {
            console.error("eenheden kon niet gefetched worden:", error);
        }
    } else {
        console.error("jwt error");
        goto('/');
    }
}

// fetchen van stalen
export async function fetchStalen() {
    if (token) {
        try {
            const stalen = await fetchAll(token, 'stalen');
            const filteredStalen = stalen;
            return { stalen, filteredStalen };
        } catch (error) {
            console.error("Stalen konden niet gefetched worden:", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

// fetchen van stalen
export async function fetchStaal_StaalCode(staalCode: string) {
    if (token) {
        try {
            const staal = await fetchAll(token, `staal/${staalCode}`);
            return staal;
        } catch (error) {
            console.error("Staal kon niet gefetched worden:", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

// fetchen van users
export async function fetchUsers() {
    if (token) {
        try {
            const users = await fetchAllWithoutPrefix(token, 'getusers');
            return users;
        } catch (error) {
            console.error("Users konden niet gefetched worden: ", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

// fetchen van rollen
export async function fetchRollen() {
    if (token) {
        try {
            const rollen = await fetchAll(token, 'rollen');
            return rollen;
        } catch (error) {
            console.error("Rollen konden niet gefetched worden:", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

// fetchen van tests
export async function fetchTests() {
    if (token) {
        try {
            const tests = await fetchAll(token, 'tests');
            return tests;
        } catch (error) {
            console.error("Tests konden niet gefetched worden:", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

// fetchen van testcategorieën
export async function fetchTestcategorieën() {
    if (token) {
        try {
            const categorieën = await fetchAll(token, 'testcategorieen');
            return categorieën;
        } catch (error) {
            console.error("Categorieën konden niet gefetched worden:", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

// fetchen van eenheden
export async function fetchEenheden() {
    if (token) {
        try {
            const eenheden = await fetchAll(token, 'readeenheid');
            return eenheden;
        } catch (error) {
            console.error("Eenheden konden niet gefetched worden:", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

// fetchen van referentiewaarden
export async function fetchReferentiewaarden() {
    if (token) {
        try {
            const referentiewaarden = await fetchAll(token, 'referentiewaarden');
            return referentiewaarden;
        } catch (error) {
            console.error("Referentiewaarden konden niet gefetched worden:", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

// fetchen van statussen
export async function fetchStatussen() {
    if (token) {
        try {
            const statussen = await fetchAll(token, 'getstatus');
            return statussen;
        } catch (error) {
            console.error("Statussen konden niet gefetched worden:", error);
        }
    }
}