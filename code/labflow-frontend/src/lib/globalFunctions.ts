import { jwtDecode } from "jwt-decode";
import type { DecodedToken } from "$lib/types";
const backend_path = import.meta.env.VITE_BACKEND_PATH;

// helper functie om de jwt token te decoden
function decodeToken() {
    const token = getCookie('authToken');
    if (token) {
        return jwtDecode<DecodedToken>(token);
    }
    return null;
}

// functie voor het ophalen van de rol van de gebruiker uit de jwt token
export function getRolNaam_FromToken() {
    const token = decodeToken();
    let rol: string | undefined;
    if (token) {
        rol = token.rol;
    }
    return rol;
}

// functie voor het ophalen van de id van de gebruiker uit de jwt token
export function getUserId() {
    const token = decodeToken();
    let userId: string | undefined;
    if (token) {
        userId = token.userId;
    }
    return userId;
}

// https://jasonwatmore.com/fetch-add-bearer-token-authorization-header-to-http-request#:~:text=The%20auth%20header%20with%20bearer,to%20the%20fetch()%20function.
// https://stackoverflow.com/questions/51264913/how-to-add-authorization-token-in-incoming-http-request-header
export async function fetchAll(token: string, subject: string, params: string = '') {
    // deze header is de jwt token nodig voor authenticatie
    const headers = {
        "Authorization": "Bearer " + token
    };

    const url = params ? `${backend_path}/api/${subject}?${params}` : `${backend_path}/api/${subject}`;
    const response = await fetch(url, { headers })
        .then(response => response.json());
    return response;
}

// voor users
export async function fetchAllWithoutPrefix(token: string, subject: string) {
    const headers = {
        "Authorization": "Bearer " + token
    };

    const response = await fetch(`${backend_path}/${subject}`, { headers })
        .then(response => response.json());
        return response;
}

// https://stackoverflow.com/questions/10730362/get-cookie-by-name 
export function getCookie(name: string) {
    if (typeof document === "undefined") {
        console.warn("getCookie called in a non-browser environment.");
        return null;
    }

    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);

    if (parts.length === 2) {
        const part = parts.pop();
        if (part) return part.split(';').shift();
    }

    // Als er geen cookie is, ga naar de login pagina (dit zorgt voor een refresh goto('/') niet)
    window.location.href = '/';
    return null;
}


// formateren van date
export function formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB');
}

// set sex
export function formatSex(sex: string): string {
    if (sex == "M") {
        return "Man";
    } else {
        return "Vrouw";
    }
}