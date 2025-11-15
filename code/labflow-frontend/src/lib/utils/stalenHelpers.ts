// Date formatting
export function formatDateToDDMMYYYY(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

export function formatDateForBackend(dateStr: string): string {
    if (!dateStr) return '';
    return dateStr;
}

// Pagination calculation (pure function, does not rely on Svelte state)
export function getVisiblePages(page: number, totalPages: number, delta = 2) {
    const range = [];
    const rangeWithDots = [];

    for (let i = Math.max(2, page - delta); i <= Math.min(totalPages - 1, page + delta); i++) {
        range.push(i);
    }

    if (page - delta > 2) {
        rangeWithDots.push(1, '...');
    } else {
        rangeWithDots.push(1);
    }

    rangeWithDots.push(...range);

    if (page + delta < totalPages - 1) {
        rangeWithDots.push('...', totalPages);
    } else if (totalPages > 1) {
        rangeWithDots.push(totalPages);
    }

    return rangeWithDots.filter((v, i, arr) => arr.indexOf(v) === i);
}

export function createEditStaalError() {
	return {
		staalCode: false,
		patientVoornaam: false,
		patientAchternaam: false,
		patientGeboorteDatum: false,
		patientGeslacht: false,
		laborantNaam: false,
		laborantRnummer: false,
		user: false,
		registeredTests: false
	};
}
