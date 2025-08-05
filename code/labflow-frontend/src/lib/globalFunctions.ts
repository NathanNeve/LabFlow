import { goto } from "$app/navigation";

const backend_path = import.meta.env.VITE_BACKEND_PATH;

// functie voor het ophalen van de rol van de gebruiker uit de jwt token
// THIS SHOULD BE CHANGED TO OTHER WAY OF GETTING ROLE & USER ID


export function getRolNaam_FromToken() {
    return sessionStorage.getItem('Role') || '';
}

// functie voor het ophalen van de id van de gebruiker uit de jwt token
export function getUserId() {
    return sessionStorage.getItem('UserId') || '';
}

// https://jasonwatmore.com/fetch-add-bearer-token-authorization-header-to-http-request#:~:text=The%20auth%20header%20with%20bearer,to%20the%20fetch()%20function.
// https://stackoverflow.com/questions/51264913/how-to-add-authorization-token-in-incoming-http-request-header
// general fetch function (ability to set: request type, subject, body and if the prefix is needed)
export async function generalFetch(
    request_type: 'GET' | 'POST' | 'PUT' | 'DELETE', 
    subject: string, 
    prefix: boolean = true, 
    id?: string | number,
    body?: any
) {
    // Setup url based on the provided parameters
    const url = `${backend_path}/${prefix ? 'api/' : ''}${subject}${id ? `/${id}` : ''}`;
    
    // Setup request parameters
    const options: RequestInit = {
        method: request_type,
        credentials: 'include',
    };
    
    // Add body (if added)
    if (body && (request_type === 'POST' || request_type === 'PUT')) {
            options.headers = {
                'Content-Type': 'application/json',
            };
            options.body = JSON.stringify(body);
    }
    
    // do the request
    const response = await fetch(url, options);
    const text = await response.text();

    if (response.status == 401) {
        console.error('Unauthorized access - redirecting to login');
        goto('/');
    }

    try {
        return text ? JSON.parse(text) : null;
    } catch (error) {
        console.error('Error parsing JSON:', error);
    }

    return response;
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