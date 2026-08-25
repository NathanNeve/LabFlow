<script lang="ts">
	import Nav from '../../../../components/nav.svelte';
	import { goto } from '$app/navigation';
	import { getCookie } from '$lib/globalFunctions';
	import { onMount } from 'svelte';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import FaArrowRight from 'svelte-icons/fa/FaArrowRight.svelte';
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	import { microbiologyStaalIdStore } from '$lib/store';
	import { get } from 'svelte/store';
	import type { MicrobiologyCatalogTest, MicrobiologyStaal, MicrobiologyStaalType } from '$lib/types/dbTypes';
	import {
		createMicrobiologyStaal,
		fetchMicrobiologyStaalTypes,
		fetchMicrobiologyTests,
		clearMicrobiologyStaalTests,
		saveMicrobiologyStaalTests,
		updateMicrobiologyStaal,
		fetchMicrobiologyStaalById
	} from '$lib/fetchFunctions';

	function authToken(): string {
		return getCookie('authToken') || '';
	}

	let staalTypes: MicrobiologyStaalType[] = [];
	let tests: MicrobiologyCatalogTest[] = [];
	let testsSorted: MicrobiologyCatalogTest[] = [];
	let searchCode = '';

	let modalOpen = true;
	let modalStep: 1 | 2 = 1;
	let typeChangeMode = false;

	let laborantNaam = '';
	let laborantRnummer = '';
	let selectedStaalTypeId: number | '' = '';

	let microStaalId: number | null = null;
	let nieuweStaalCode: string = '';
	let naam = '';
	let voornaam = '';
	let geslacht = '';
	let geboortedatum = '';

	let errrorVeldenModal = { laborantNaam: false, laborantRnummer: false, staalType: false };
	let errrorVeldenStaal = {
		naam: false,
		voornaam: false,
		geslacht: false,
		geboortedatum: false
	};
	let errorMessageStaal = '';
	let geselecteerdeTestIds: number[] = [];
	let loadingTypes = true;

	onMount(async () => {
		const token = authToken();
		if (!token) {
			goto('/');
			return;
		}
		const types = await fetchMicrobiologyStaalTypes();
		if (types) staalTypes = types;
		loadingTypes = false;
		// Always start a fresh flow when opening this page from the dashboard.
		// We rely on explicitly persisted store only if the user navigates back/forward within the flow.
		const sid = get(microbiologyStaalIdStore);
		if (sid && String(sid).trim() !== '') {
			const idNum = Number(sid);
			if (Number.isFinite(idNum) && idNum > 0) {
				await resumeFromStore(idNum);
			}
		}
	});

	async function resumeFromStore(idNum: number) {
		const staal = (await fetchMicrobiologyStaalById(idNum)) as MicrobiologyStaal | null;
		if (!staal?.id) return;
		microStaalId = staal.id;
		nieuweStaalCode = String(staal.staalCode ?? '');
		naam = staal.patientAchternaam ?? '';
		voornaam = staal.patientVoornaam ?? '';
		geslacht = staal.patientGeslacht ?? '';
		geboortedatum = staal.patientGeboorteDatum
			? String(staal.patientGeboorteDatum).slice(0, 10)
			: '';
		laborantNaam = staal.laborantNaam ?? '';
		laborantRnummer = staal.laborantRnummer ?? '';
		selectedStaalTypeId = staal.staalType?.id ?? '';
		modalOpen = false;
		await loadTestsForType(Number(staal.staalType?.id));
	}

	async function loadTestsForType(staalTypeId: number) {
		const raw = await fetchMicrobiologyTests(staalTypeId);
		const list = Array.isArray(raw) ? (raw as MicrobiologyCatalogTest[]) : [];
		tests = list;
		testsSorted = list;
	}

	function filterTests() {
		const q = (searchCode || '').toLowerCase();
		testsSorted = tests.filter((t) => {
			const n = (t.naam ?? '').toLowerCase();
			const tc = (t.testCode ?? '').toLowerCase();
			const vbs = (t.voedingsbodems ?? []).join(' ').toLowerCase();
			return n.includes(q) || tc.includes(q) || vbs.includes(q);
		});
	}

	function verwijderZoek() {
		searchCode = '';
		testsSorted = tests;
	}

	function verwijderSelectie() {
		geselecteerdeTestIds = [];
	}

	function toggleTestSelectie(testId: number) {
		if (geselecteerdeTestIds.includes(testId)) {
			geselecteerdeTestIds = geselecteerdeTestIds.filter((id) => id !== testId);
		} else {
			geselecteerdeTestIds = [...geselecteerdeTestIds, testId];
		}
	}

	function cancelModal() {
		if (typeChangeMode) {
			typeChangeMode = false;
			modalOpen = false;
			modalStep = 1;
			return;
		}
		goto('/microbiologie/stalen');
	}

	function modalNext() {
		errrorVeldenModal = { laborantNaam: false, laborantRnummer: false, staalType: false };
		laborantRnummer = laborantRnummer.toUpperCase();
		const regex = /^[RU]\d{7}$/;
		if (!laborantNaam) errrorVeldenModal.laborantNaam = true;
		if (!laborantRnummer || !regex.test(laborantRnummer)) errrorVeldenModal.laborantRnummer = true;
		if (!laborantNaam || !laborantRnummer || !regex.test(laborantRnummer)) return;
		modalStep = 2;
	}

	function modalBack() {
		modalStep = 1;
	}

	async function modalStart() {
		errrorVeldenModal.staalType = false;
		if (!selectedStaalTypeId) {
			errrorVeldenModal.staalType = true;
			return;
		}
		const tid = Number(selectedStaalTypeId);
		if (typeChangeMode && microStaalId != null) {
			const geboorteIso = geboortedatum ? `${geboortedatum}T00:00:00` : '';
			const res = await updateMicrobiologyStaal(microStaalId, {
				staalCode: Number(nieuweStaalCode),
				patientVoornaam: voornaam,
				patientAchternaam: naam,
				patientGeboorteDatum: geboorteIso,
				patientGeslacht: geslacht,
				laborantNaam: laborantNaam,
				laborantRnummer: laborantRnummer,
				staalTypeId: tid
			});
			if (!res?.ok) {
				errorMessageStaal = 'Staaltype kon niet worden aangepast.';
				return;
			}
			// Clear previously connected tests (and any confirmed voedingsbodems) after type change.
			await clearMicrobiologyStaalTests(microStaalId);
			geselecteerdeTestIds = [];
			await loadTestsForType(tid);
			typeChangeMode = false;
			modalOpen = false;
			modalStep = 1;
			return;
		}

		// New sample: ensure we don't keep previous state.
		naam = '';
		voornaam = '';
		geslacht = '';
		geboortedatum = '';
		geselecteerdeTestIds = [];

		const created = await createMicrobiologyStaal({
			laborantNaam,
			laborantRnummer,
			staalTypeId: tid
		});
		if (!created?.id) {
			errorMessageStaal = 'Registratie mislukt.';
			return;
		}
		microStaalId = created.id;
		nieuweStaalCode = String(created.staalCode ?? '');
		microbiologyStaalIdStore.set(String(created.id));
		modalOpen = false;
		modalStep = 1;
		await loadTestsForType(tid);
	}

	function openChangeStaalType() {
		if (!microStaalId) return;
		typeChangeMode = true;
		modalStep = 2;
		modalOpen = true;
	}

	function patientFormValid(): boolean {
		return !!(
			naam &&
			voornaam &&
			geboortedatum &&
			geslacht &&
			(microStaalId != null || nieuweStaalCode)
		);
	}

	async function volgende() {
		errorMessageStaal = '';
		errrorVeldenStaal = { naam: false, voornaam: false, geslacht: false, geboortedatum: false };
		let ok = true;
		if (!naam) {
			errrorVeldenStaal.naam = true;
			ok = false;
		}
		if (!voornaam) {
			errrorVeldenStaal.voornaam = true;
			ok = false;
		}
		if (!geboortedatum) {
			errrorVeldenStaal.geboortedatum = true;
			ok = false;
		}
		if (!geslacht) {
			errrorVeldenStaal.geslacht = true;
			ok = false;
		}
		if (!ok) {
			errorMessageStaal = 'Vul alle patiëntvelden in.';
			return;
		}
		if (geselecteerdeTestIds.length === 0) {
			errorMessageStaal = 'Selecteer minstens één test.';
			return;
		}
		if (microStaalId == null) return;
		const geboorteIso = `${geboortedatum}T00:00:00`;
		const saved = await saveMicrobiologyStaalTests(microStaalId, {
			patientVoornaam: voornaam,
			patientAchternaam: naam,
			patientGeboorteDatum: geboorteIso,
			patientGeslacht: geslacht,
			testIds: geselecteerdeTestIds
		});
		if (!saved) {
			errorMessageStaal = 'Opslaan mislukt.';
			return;
		}
		goto('/microbiologie/stalen/labels');
	}
</script>

<Nav />

{#if modalOpen}
	<div
		class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4"
		role="dialog"
		aria-modal="true"
		aria-labelledby="mb-modal-title"
	>
		<div class="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">
			<h1 id="mb-modal-title" class="mb-3 text-xl font-bold">
				{modalStep === 1 ? 'Laborantgegevens' : 'Selecteer staaltype'}
			</h1>
			<div class="mb-4 flex max-w-[33%] gap-2">
				<!-- step 1 stays blue, step 2 becomes blue on step 2 -->
				<div class="h-2 flex-1 rounded bg-blue-600"></div>
				<div class="h-2 flex-1 rounded {modalStep === 2 ? 'bg-blue-600' : 'bg-gray-300'}"></div>
			</div>

			{#if modalStep === 1}
				<div class="mb-4 flex flex-col gap-4 sm:flex-row">
					<div class="flex flex-1 flex-col">
						<label for="mb-lab-naam">Volledige naam</label>
						<input
							id="mb-lab-naam"
							bind:value={laborantNaam}
							class="h-12 rounded-lg bg-gray-200 pl-3 text-black {errrorVeldenModal.laborantNaam
								? 'border-2 border-red-500'
								: ''}"
						/>
					</div>
					<div class="flex flex-1 flex-col">
						<label for="mb-lab-r"
							>R-nummer <span class={errrorVeldenModal.laborantRnummer ? 'text-red-500' : 'hidden'}
								>format R/U + 7 cijfers</span
							></label
						>
						<input
							id="mb-lab-r"
							bind:value={laborantRnummer}
							class="h-12 rounded-lg bg-gray-200 pl-3 text-black {errrorVeldenModal.laborantRnummer
								? 'border-2 border-red-500'
								: ''}"
						/>
					</div>
				</div>
			{:else}
				<div class="mb-4 flex flex-col">
					<label for="mb-staaltype">Staaltype</label>
					<select
						id="mb-staaltype"
						bind:value={selectedStaalTypeId}
						class="h-12 rounded-lg bg-gray-200 pl-3 text-black {errrorVeldenModal.staalType
							? 'border-2 border-red-500'
							: ''}"
						disabled={loadingTypes}
					>
						<option value="" disabled selected>Selecteer type</option>
						{#each staalTypes as t}
							<option value={t.id}>{t.naam}</option>
						{/each}
					</select>
				</div>
			{/if}

			<div class="flex flex-wrap gap-2">
				<button
					type="button"
					class="rounded-lg bg-gray-400 px-4 py-3 text-white"
					on:click={cancelModal}
				>
					Annuleren
				</button>
				{#if modalStep === 2}
					<button
						type="button"
						class="rounded-lg bg-gray-500 px-4 py-3 text-white"
						on:click={modalBack}
					>
						Terug
					</button>
				{/if}
				{#if modalStep === 1}
					<button
						type="button"
						class="rounded-lg bg-blue-600 px-4 py-3 text-white"
						on:click={modalNext}
					>
						Volgende
					</button>
				{:else}
					<button
						type="button"
						class="rounded-lg bg-blue-600 px-4 py-3 text-white"
						on:click={modalStart}
					>
						{typeChangeMode ? 'Bevestig' : 'Start'}
					</button>
				{/if}
			</div>
		</div>
	</div>
{/if}

<div class="px-8">
	<div class="rounded-2xl bg-slate-200 p-5">
		<h1 class="mb-2 text-xl font-bold">Patiëntgegevens</h1>
		{#if errorMessageStaal}
			<div class="mb-2 text-red-500">{errorMessageStaal}</div>
		{/if}
		<div class="flex flex-row space-x-4">
			<div class="grid h-auto w-5/6 grid-cols-5 gap-2 rounded-lg bg-white px-2 py-3">
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Code</p>
					<p class="font-bold">{nieuweStaalCode || '—'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Achternaam</p>
					<input
						type="text"
						bind:value={naam}
						class="h-10 rounded-lg bg-gray-200 pl-3 text-black {errrorVeldenStaal.naam
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Voornaam</p>
					<input
						type="text"
						bind:value={voornaam}
						class="h-10 rounded-lg bg-gray-200 pl-3 text-black {errrorVeldenStaal.voornaam
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Geboortedatum</p>
					<input
						type="date"
						bind:value={geboortedatum}
						class="h-10 rounded-lg bg-gray-200 px-3 text-black {errrorVeldenStaal.geboortedatum
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="flex flex-col justify-center pl-2">
					<p class="text-gray-400">Geslacht</p>
					<div>
						<label class="mr-3 {errrorVeldenStaal.geslacht ? 'font-bold text-red-500' : ''}">
							<input type="radio" name="mb-geslacht" bind:group={geslacht} value="M" /> Man
						</label>
						<label class="mr-3 {errrorVeldenStaal.geslacht ? 'font-bold text-red-500' : ''}">
							<input type="radio" name="mb-geslacht" bind:group={geslacht} value="V" /> Vrouw
						</label>
						<label class={errrorVeldenStaal.geslacht ? 'font-bold text-red-500' : ''}>
							<input type="radio" name="mb-geslacht" bind:group={geslacht} value="X" /> X
						</label>
					</div>
				</div>
			</div>
			<div class="flex w-3/12 flex-row justify-end space-x-2 pb-5">
				<button
					type="button"
					class="flex h-20 w-1/2 flex-row items-center justify-center rounded-lg bg-gray-400 p-3 text-xl text-white"
					on:click={() => goto('/microbiologie/stalen')}
				>
					<div class="mr-2 h-5 w-5"><FaArrowLeft /></div>
					Terug
				</button>
				<button
					type="button"
					class="flex h-20 w-1/2 flex-row items-center justify-center rounded-lg bg-blue-600 p-3 text-xl text-white disabled:cursor-not-allowed disabled:bg-gray-300"
					disabled={!microStaalId}
					on:click={volgende}
				>
					Volgende
					<div class="ml-2 h-5 w-5"><FaArrowRight /></div>
				</button>
			</div>
		</div>

		{#if microStaalId}
			<div class="mb-3 mt-2">
				<button
					type="button"
					class="rounded-lg bg-gray-200 px-4 py-2 text-sm font-medium text-black hover:bg-gray-300 disabled:opacity-50"
					disabled={!microStaalId}
					on:click={openChangeStaalType}
				>
					Staaltype wijzigen
				</button>
			</div>
		{/if}

		<div class="rounded-xl bg-white">
			<div class="flex flex-row flex-wrap items-center place-content-between gap-2 rounded-xl bg-white p-3">
				<div class="flex w-full max-w-md items-center sm:w-1/4">
					<input
						type="text"
						placeholder="zoeken op code of naam"
						bind:value={searchCode}
						on:input={filterTests}
						class="h-12 w-full rounded-l-lg bg-gray-200 pl-3 text-black"
					/>
					<button
						type="button"
						on:click={verwijderZoek}
						class="flex h-12 w-12 items-center justify-center rounded-r-lg bg-red-200 p-4"
					>
						<GoX />
					</button>
				</div>
				<button
					type="button"
					on:click={verwijderSelectie}
					class="h-12 rounded-lg bg-red-500 px-4 text-white"
				>
					Verwijder selectie
				</button>
				<p class="text-blue-600"><span>{geselecteerdeTestIds.length}</span> geselecteerd</p>
			</div>

			<div class="h-[calc(100vh-330px)] overflow-auto">
				{#each testsSorted as test}
					<div class="grid h-20 grid-cols-12 items-center gap-4 border-b border-gray-300 px-3">
						<div class="col-span-1">
							<input
								type="checkbox"
								checked={geselecteerdeTestIds.includes(test.id)}
								on:change={() => toggleTestSelectie(test.id)}
								class="mt-2 h-5 w-5 appearance-none rounded-md border-2 border-gray-300 checked:border-transparent checked:bg-blue-600 focus:outline-none"
							/>
						</div>
						<div class="col-span-2">
							<p class="text-gray-400">Testcode</p>
							<p>{test.testCode}</p>
						</div>
						<div class="col-span-5">
							<p class="text-gray-400">Naam</p>
							<p class="truncate">{test.naam}</p>
						</div>
						<div class="col-span-4">
							<p class="text-gray-400">Voedingsbodems</p>
							{#if (test.voedingsbodems ?? []).length > 0}
								<p class="truncate">{(test.voedingsbodems ?? []).join(', ')}</p>
							{:else}
								<p class="text-gray-400">—</p>
							{/if}
						</div>
					</div>
				{/each}
			</div>
		</div>
	</div>
</div>
