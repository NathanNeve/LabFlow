// src/routes/stalen/+page.ts
import type { PageLoad } from './$types';
import { fetchStalen, fetchStatussen } from '$lib/fetchFunctions';
import { getRolNaam_FromToken } from '$lib/globalFunctions';
import type { StalenSearchParams } from '$lib/types/searchTypes';

export const load: PageLoad = async () => {
	const rol = getRolNaam_FromToken();

	// load statussen and initial stalen
	const statussen = await fetchStatussen();

	const searchParams: StalenSearchParams = {};
	const stalenResult = await fetchStalen(0, 25, searchParams);

	const stalen = stalenResult?.stalen ?? [];
	const totalPages = stalenResult?.totalPages ?? 0;
	const totalElements = stalenResult?.totalElements ?? 0;

	return {
		rol,
		statussen,
		stalen,
		totalPages,
		totalElements
	};
};
