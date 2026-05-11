import { getCookie, fetchAll, fetchAllWithoutPrefix } from '$lib/globalFunctions';
import { goto } from '$app/navigation';
import type { Eenheid, TestCategorie } from './types/dbTypes';
import type { StalenSearchParams } from './types/searchTypes';

function authToken(): string {
	return getCookie('authToken') ?? '';
}
let testcategorieën: TestCategorie[] = [];
let eenheden: Eenheid[] = [];

// laden categorieën
export async function loadTestCategorieën() {
    const token = authToken();
    if (token) {
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
    const token = authToken();
    if (token) {
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

// fetch alle stalen
export async function fetchStalen(page = 0, size = 25, searchParams: StalenSearchParams = {}) {
    const token = authToken();
    if (token) {
        try {
            // query parameters voor paginering en zoeken op datum, code en status
            let params = `page=${page}&size=${size}&sort=id,desc`;
            
            // voeg parameters toe als ze zijn opgegeven
            if (searchParams.searchCode) {
                params += `&search=${encodeURIComponent(searchParams.searchCode)}`;
            }
            if (searchParams.searchDate) {
                params += `&date=${encodeURIComponent(searchParams.searchDate)}`;
            }
            if (searchParams.filteredStatus) {
                params += `&status=${encodeURIComponent(searchParams.filteredStatus)}`;
            }
            
            const stalen = await fetchAll(token, 'staal', params);
            
            const filteredStalen = stalen.content;
            console.log(filteredStalen);
            return { 
                stalen: filteredStalen, 
                filteredStalen,
                totalPages: stalen.totalPages,
                totalElements: stalen.totalElements,
                currentPage: stalen.number,
                isFirst: stalen.first,
                isLast: stalen.last,
                size: stalen.size
            };
        } catch (error) {
            console.error("Stalen konden niet gefetched worden:", error);
            return null;
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
        return null;
    }
}

// fetchen van 1 staal op basis van staalCode
export async function fetchStaal_StaalCode(staalCode: string) {
    const token = authToken();
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
    const token = authToken();
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
    const token = authToken();
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
    const token = authToken();
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
    const token = authToken();
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
    const token = authToken();
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
    const token = authToken();
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
    const token = authToken();
    if (token) {
        try {
            const statussen = await fetchAll(token, 'getstatus');
            return statussen;
        } catch (error) {
            console.error("Statussen konden niet gefetched worden:", error);
        }
    } else {
        console.error("JWT error: token missing of invalid");
        goto('/');
    }
}

const backend_path_fetch = import.meta.env.VITE_BACKEND_PATH;

/** Paginated microbiology stalen (GET /api/microbiology/staal) */
export async function fetchMicrobiologyStalen(
    page = 0,
    size = 25,
    searchParams: StalenSearchParams = {}
) {
    const token = authToken();
    if (token) {
        try {
            let params = `page=${page}&size=${size}`;
            if (searchParams.searchCode) {
                params += `&search=${encodeURIComponent(searchParams.searchCode)}`;
            }
            if (searchParams.searchDate) {
                params += `&date=${encodeURIComponent(searchParams.searchDate)}`;
            }
            const stalen = await fetchAll(token, 'microbiology/staal', params);
            const filteredStalen = stalen.content;
            return {
                stalen: filteredStalen,
                filteredStalen,
                totalPages: stalen.totalPages,
                totalElements: stalen.totalElements,
                currentPage: stalen.number,
                isFirst: stalen.first,
                isLast: stalen.last,
                size: stalen.size
            };
        } catch (error) {
            console.error('Microbiology stalen konden niet gefetched worden:', error);
            return null;
        }
    } else {
        console.error('JWT error: token missing of invalid');
        goto('/');
        return null;
    }
}

export async function fetchMicrobiologyStaalTypes() {
    const token = authToken();
    if (token) {
        try {
            return await fetchAll(token, 'microbiology/staal-types');
        } catch (error) {
            console.error('Microbiology staaltypes konden niet gefetched worden:', error);
        }
    } else {
        console.error('JWT error: token missing of invalid');
        goto('/');
    }
}

export async function updateMicrobiologyStaal(id: number, body: Record<string, unknown>) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    return fetch(`${backend_path_fetch}/api/microbiology/staal/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + token
        },
        body: JSON.stringify(body)
    });
}

export async function deleteMicrobiologyStaal(id: number) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    return fetch(`${backend_path_fetch}/api/microbiology/staal/${id}`, {
        method: 'DELETE',
        headers: {
            Authorization: 'Bearer ' + token
        }
    });
}

export async function createMicrobiologyStaal(body: {
    laborantNaam: string;
    laborantRnummer: string;
    staalTypeId: number;
}) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    const response = await fetch(`${backend_path_fetch}/api/microbiology/staal`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + token
        },
        body: JSON.stringify(body)
    });
    if (!response.ok) return null;
    return response.json();
}

export async function fetchMicrobiologyStaalById(id: number) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    try {
        return await fetchAll(token, `microbiology/staal/${id}`);
    } catch (e) {
        console.error('Microbiology staal ophalen mislukt:', e);
        return null;
    }
}

export async function fetchMicrobiologyTests(staalTypeId: number) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    try {
        return await fetchAll(token, 'microbiology/tests', `staalTypeId=${staalTypeId}`);
    } catch (e) {
        console.error('Microbiology tests ophalen mislukt:', e);
        return null;
    }
}

export async function saveMicrobiologyStaalTests(
    id: number,
    body: {
        patientVoornaam: string;
        patientAchternaam: string;
        patientGeboorteDatum: string;
        patientGeslacht: string;
        testIds: number[];
    }
) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    const response = await fetch(`${backend_path_fetch}/api/microbiology/staal/${id}/tests`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + token
        },
        body: JSON.stringify(body)
    });
    if (!response.ok) return null;
    return response.json();
}

export async function fetchMicrobiologyVoedingsbodems(staalId: number) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    try {
        return await fetchAll(token, `microbiology/staal/${staalId}/voedingsbodems`);
    } catch (e) {
        console.error('Voedingsbodems ophalen mislukt:', e);
        return null;
    }
}

export async function confirmMicrobiologyVoedingsbodems(staalId: number, voedingsbodemIds: number[]) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    return fetch(`${backend_path_fetch}/api/microbiology/staal/${staalId}/voedingsbodems/confirm`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + token
        },
        body: JSON.stringify({ voedingsbodemIds })
    });
}

export async function clearMicrobiologyStaalTests(staalId: number) {
    const token = authToken();
    if (!token) {
        goto('/');
        return null;
    }
    return fetch(`${backend_path_fetch}/api/microbiology/staal/${staalId}/tests`, {
        method: 'DELETE',
        headers: {
            Authorization: 'Bearer ' + token
        }
    });
}