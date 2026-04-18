import { writable } from "svelte/store";

// create a session-backed store
function createSessionStore(key:string, initialValue:string) {
    let storedValue;

    // check if we are running in a browser (not in a server environment)
    if (typeof window !== 'undefined') {
        // try to retrieve the value associated with the given key from session storage
        storedValue = sessionStorage.getItem(key);
    }


    // initialize the writable store. use the value from sessionStorage if it exists, otherwise use the initialValue
    let parsedInitial = storedValue ? JSON.parse(storedValue) : initialValue;
    if (key === 'staalCode' && parsedInitial === 'defaultCode') {
        parsedInitial = '';
    }
    const store = writable(parsedInitial);

    // subscribe to changes in the store to update sessionStorage whenever the store value changes
    if (typeof window !== 'undefined') {
        store.subscribe((value) => {
            if (value !== undefined) {
                // save the updated value to session storage (JSON string)
                sessionStorage.setItem(key, JSON.stringify(value));
            }
        });
    }

    return store;
}

// create a specific session-backed store for 'staalcode' (empty = nieuwe staal)
export const staalCodeStore = createSessionStore('staalCode', '');