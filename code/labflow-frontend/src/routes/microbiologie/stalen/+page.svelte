<script lang="ts">
	import Nav from '../../../components/nav.svelte';
	import { onMount } from 'svelte';
	import { getRolNaam_FromToken } from '$lib/globalFunctions';
	import {
		fetchMicrobiologyStalen,
		fetchMicrobiologyStaalTypes,
		updateMicrobiologyStaal,
		deleteMicrobiologyStaal
	} from '$lib/fetchFunctions';
	import { id } from '../../../components/Modal/store';

	// @ts-ignore
	import GoPlus from 'svelte-icons/go/GoPlus.svelte';
	// @ts-ignore
	import IoMdSettings from 'svelte-icons/io/IoMdSettings.svelte';
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaRegEdit from 'svelte-icons/fa/FaRegEdit.svelte';
	// @ts-ignore
	import IoMdCheckmarkCircle from 'svelte-icons/io/IoMdCheckmarkCircle.svelte';
	import Modal from '../../../components/Modal/Modal.svelte';
	import Trigger from '../../../components/Modal/Trigger.svelte';
	import Content from '../../../components/Modal/Content.svelte';
	import type { MicrobiologyStaal, MicrobiologyStaalType } from '$lib/types/dbTypes';
	import type { StalenSearchParams } from '$lib/types/searchTypes';

	let openModalTestId: number | null = null;

	let bgColor = 'bg-blue-400';
	let pointerEvent = 'pointer-events-auto';
	const rol = getRolNaam_FromToken();
	if (rol !== 'admin') {
		bgColor = 'bg-gray-400';
		pointerEvent = 'pointer-events-none';
	}

	let stalen: MicrobiologyStaal[] = [];
	let staalTypes: MicrobiologyStaalType[] = [];
	let searchCode = '';
	let searchDate = '';
	/** Mock data (~25 rows): page size 10 yields multiple pages; raise to 25 for a denser table. */
	const pageSize = 10;
	let page = 0;
	let totalPages = 0;
	let totalElements = 0;
	let isLoading = false;

	let editStaalError = {
		staalCode: false,
		patientVoornaam: false,
		patientAchternaam: false,
		patientGeboorteDatum: false,
		patientGeslacht: false,
		laborantNaam: false,
		laborantRnummer: false,
		staalType: false
	};

	function resetErrors() {
		editStaalError = {
			staalCode: false,
			patientVoornaam: false,
			patientAchternaam: false,
			patientGeboorteDatum: false,
			patientGeslacht: false,
			laborantNaam: false,
			laborantRnummer: false,
			staalType: false
		};
		editStaalErrorMessage = '';
	}

	function formatDateToDDMMYYYY(dateStr: string): string {
		if (!dateStr) return '';
		const date = new Date(dateStr);
		const day = String(date.getDate()).padStart(2, '0');
		const month = String(date.getMonth() + 1).padStart(2, '0');
		const year = date.getFullYear();
		return `${day}/${month}/${year}`;
	}

	function formatDateForBackend(dateStr: string): string {
		if (!dateStr) return '';
		return dateStr;
	}

	function geslachtLabel(g: string): string {
		if (g === 'V') return 'Vrouw';
		if (g === 'M') return 'Man';
		if (g === 'X') return 'X';
		return g || '';
	}

	function verwijderZoek() {
		searchCode = '';
		loadStalen();
	}

	function deleteFilters() {
		searchCode = '';
		searchDate = '';
		page = 0;
		loadStalen();
	}

	function applyFilters() {
		page = 0;
		loadStalen();
	}

	async function loadStalen() {
		isLoading = true;
		try {
			const searchParams: StalenSearchParams = {};
			if (searchCode.trim()) {
				searchParams.searchCode = searchCode.trim();
			}
			if (searchDate) {
				searchParams.searchDate = formatDateForBackend(searchDate);
			}
			const data = await fetchMicrobiologyStalen(page, pageSize, searchParams);
			if (data) {
				stalen = data.stalen;
				totalPages = data.totalPages;
				totalElements = data.totalElements;
			}
		} catch (error) {
			console.error('Error loading stalen:', error);
		} finally {
			isLoading = false;
		}
	}

	let searchTimeout: ReturnType<typeof setTimeout>;
	function handleSearchInput() {
		clearTimeout(searchTimeout);
		searchTimeout = setTimeout(() => {
			applyFilters();
		}, 500);
	}

	function handleDateChange() {
		applyFilters();
	}

	async function deleteStaal(id: number) {
		try {
			await deleteMicrobiologyStaal(id);
			await loadStalen();
		} catch (error) {
			console.error('Staal kon niet worden verwijderd: ', error);
		}
	}

	let editStaalErrorMessage = '';
	function patientGeboorteDatumInputValue(staal: MicrobiologyStaal): string {
		const v = staal.patientGeboorteDatum;
		if (!v) return '';
		return v.length >= 10 ? v.slice(0, 10) : v;
	}

	function setPatientGeboorteDatumFromDate(staal: MicrobiologyStaal, ymd: string) {
		staal.patientGeboorteDatum = ymd ? `${ymd}T00:00:00` : '';
	}

	async function editStaal(staal: MicrobiologyStaal) {
		editStaalError = {
			staalCode: false,
			patientVoornaam: false,
			patientAchternaam: false,
			patientGeboorteDatum: false,
			patientGeslacht: false,
			laborantNaam: false,
			laborantRnummer: false,
			staalType: false
		};
		let isValid = true;
		const regex = /^[RU]\d{7}$/;

		if (staal.staalCode === undefined || staal.staalCode === null) {
			editStaalError.staalCode = true;
			isValid = false;
		}
		if (!staal.patientVoornaam) {
			editStaalError.patientVoornaam = true;
			isValid = false;
		}
		if (!staal.patientAchternaam) {
			editStaalError.patientAchternaam = true;
			isValid = false;
		}
		if (!staal.patientGeboorteDatum) {
			editStaalError.patientGeboorteDatum = true;
			isValid = false;
		}
		if (!staal.patientGeslacht) {
			editStaalError.patientGeslacht = true;
			isValid = false;
		}
		if (!staal.laborantNaam) {
			editStaalError.laborantNaam = true;
			isValid = false;
		}
		if (!staal.laborantRnummer || !regex.test(staal.laborantRnummer)) {
			editStaalError.laborantRnummer = true;
			isValid = false;
		}
		if (!staal.staalType?.id) {
			editStaalError.staalType = true;
			isValid = false;
		}
		if (!isValid) {
			editStaalErrorMessage = 'Vul alle verplichte velden in.';
			return;
		}

		let geboorteIso = staal.patientGeboorteDatum;
		if (geboorteIso && !geboorteIso.includes('T')) {
			geboorteIso = `${geboorteIso}T00:00:00`;
		}

		try {
			const response = await updateMicrobiologyStaal(staal.id, {
				staalCode: Number(staal.staalCode),
				patientVoornaam: staal.patientVoornaam,
				patientAchternaam: staal.patientAchternaam,
				patientGeboorteDatum: geboorteIso,
				patientGeslacht: staal.patientGeslacht,
				laborantNaam: staal.laborantNaam,
				laborantRnummer: staal.laborantRnummer,
				staalTypeId: staal.staalType.id
			});
			if (!response) return;
			if (response.status === 409) {
				editStaalErrorMessage = 'De staalcode bestaat al.';
			} else if (response.ok) {
				$id = null;
				await loadStalen();
			}
		} catch (error) {
			console.error('Staal kon niet worden aangepast: ', error);
		}
	}

	function nextPage() {
		if (page < totalPages - 1) {
			page++;
			loadStalen();
		}
	}

	function prevPage() {
		if (page > 0) {
			page--;
			loadStalen();
		}
	}

	function goToPage(targetPage: number) {
		if (targetPage >= 0 && targetPage < totalPages) {
			page = targetPage;
			loadStalen();
		}
	}

	function getVisiblePages() {
		const delta = 2;
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

	async function load() {
		const types = await fetchMicrobiologyStaalTypes();
		if (types) staalTypes = types;
		await loadStalen();
	}

	onMount(load);
</script>

<Nav />
<div class="px-8 flex flex-row space-x-5">
	<div class="flex flex-col space-y-5">
		<button
			type="button"
			class="bg-blue-400 flex flex-col items-center justify-center w-56 h-56 rounded-2xl"
		>
			<div class="w-28 h-28 text-white flex items-center justify-center">
				<GoPlus />
			</div>
			<p class="text-white text-2xl text-center mt-2">Nieuwe staal</p>
		</button>

		<button
			type="button"
			class="{bgColor} flex flex-col items-center justify-center w-56 h-56 rounded-2xl {pointerEvent}"
			on:click|preventDefault
		>
			<div class="w-28 h-28 text-white flex items-center justify-center">
				<IoMdSettings />
			</div>
			<p class="text-white text-2xl text-center mt-2">Instellingen</p>
		</button>
	</div>
	<div class="bg-slate-200 w-full h-full rounded-2xl p-5 mb-2">
		<div class="flex mb-5 items-center space-x-5">
			<div class="flex items-center flex-grow">
				<input
					type="text"
					id="searchCode"
					name="searchCode"
					placeholder="Zoeken"
					bind:value={searchCode}
					on:input={handleSearchInput}
					class="h-14 rounded-l-lg text-black pl-3 flex-grow border border-gray-300"
				/>
				<button
					on:click={verwijderZoek}
					class="w-14 h-14 p-4 flex items-center justify-center bg-red-500 hover:bg-red-600 text-white rounded-r-lg"
				>
					<GoX />
				</button>
			</div>

			<div class="flex items-center w-1/4">
				<label
					for="searchDate"
					class="text-black bg-gray-200 h-14 flex items-center justify-center rounded-l-lg px-3 border border-gray-300"
				>
					Geboortedatum
				</label>
				<input
					type="date"
					id="searchDate"
					name="searchDate"
					bind:value={searchDate}
					on:change={handleDateChange}
					class="flex-grow h-14 rounded-r-lg text-black px-3 border border-gray-300"
				/>
			</div>

			<button
				class="bg-blue-600 rounded-lg h-14 w-48 flex items-center justify-center text-white hover:bg-blue-700 shrink-0"
				type="button"
				on:click={deleteFilters}
			>
				Verwijder Filters
			</button>
		</div>

		{#if isLoading}
			<div class="flex justify-center items-center h-32">
				<div class="text-gray-600 text-lg">Laden...</div>
			</div>
		{/if}

		<div class="space-y-3 overflow-auto h-[calc(100vh-288px)] pr-2">
			{#each stalen as staal, index}
				<div class="flex items-center justify-between">
					<div
						class="grid grid-cols-7 gap-4 rounded-lg h-16 items-center px-3 bg-white {rol === 'admin'
							? 'w-11/12'
							: 'w-full'} border border-gray-100"
					>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400">Code</p>
							<p>{staal?.staalCode ?? ''}</p>
						</div>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400">Naam</p>
							<p>{staal?.patientAchternaam || ''}</p>
						</div>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400">Voornaam</p>
							<p>{staal?.patientVoornaam || ''}</p>
						</div>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400">Geslacht</p>
							<p>{geslachtLabel(staal?.patientGeslacht)}</p>
						</div>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400">Geboortedatum</p>
							<p>{formatDateToDDMMYYYY(staal?.patientGeboorteDatum)}</p>
						</div>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400 font-bold">Laborant</p>
							<p>{staal?.laborantNaam || ''}</p>
						</div>
						<div class="flex flex-col justify-center min-w-0">
							<p class="text-gray-400">Staaltype</p>
							<p class="truncate">{staal?.staalType?.naam || ''}</p>
						</div>
					</div>

					{#if rol === 'admin'}
						<div class="col-span-1 flex justify-end space-x-2">
							<Modal id={`mb-staal-edit-${staal.id}`}>
								<Trigger>
									<button
										type="button"
										class="h-10 w-10 bg-blue-400 p-2 rounded-lg text-white"
										on:click={() => {
											openModalTestId = staal.id;
											resetErrors();
										}}
									>
										<FaRegEdit />
									</button>
								</Trigger>
								{#if openModalTestId === staal.id}
									<Content>
										{#if editStaalErrorMessage}
											<div class="text-red-500 mb-2">{editStaalErrorMessage}</div>
										{/if}
										<div class="flex flex-row flex-wrap gap-4 my-4">
											<div class="flex flex-col w-[30%] min-w-[140px]">
												<label for="staalCode-{staal.id}">Staalcode</label>
												<input
													type="number"
													id="staalCode-{staal.id}"
													bind:value={staal.staalCode}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3 {editStaalError.staalCode
														? 'border-2 border-red-500'
														: ''}"
												/>
											</div>
											<div class="flex flex-col w-[30%] min-w-[140px]">
												<label for="patientVoornaam-{staal.id}">Voornaam patient</label>
												<input
													type="text"
													id="patientVoornaam-{staal.id}"
													bind:value={staal.patientVoornaam}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3 {editStaalError.patientVoornaam
														? 'border-2 border-red-500'
														: ''}"
												/>
											</div>
											<div class="flex flex-col w-[30%] min-w-[140px]">
												<label for="Patientachternaam-{staal.id}">Achternaam patient</label>
												<input
													type="text"
													id="Patientachternaam-{staal.id}"
													bind:value={staal.patientAchternaam}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3 {editStaalError.patientAchternaam
														? 'border-2 border-red-500'
														: ''}"
												/>
											</div>
											<div class="flex flex-col w-[30%] min-w-[140px]">
												<label for="patientGeslacht-{staal.id}">Geslacht</label>
												<div>
													<label class="mr-5 {editStaalError.patientGeslacht ? 'text-red-500 font-bold' : ''}">
														<input type="radio" name="radio-{staal.id}" bind:group={staal.patientGeslacht} value="M" />
														Man
													</label>
													<label class="mr-5 {editStaalError.patientGeslacht ? 'text-red-500 font-bold' : ''}">
														<input type="radio" name="radio-{staal.id}" bind:group={staal.patientGeslacht} value="V" />
														Vrouw
													</label>
													<label class="{editStaalError.patientGeslacht ? 'text-red-500 font-bold' : ''}">
														<input type="radio" name="radio-{staal.id}" bind:group={staal.patientGeslacht} value="X" />
														X
													</label>
												</div>
											</div>
											<div class="flex flex-col w-[30%] min-w-[140px]">
												<label for="geboorte-{staal.id}">Geboortedatum</label>
												<input
													type="date"
													id="geboorte-{staal.id}"
													value={patientGeboorteDatumInputValue(staal)}
													on:change={(e) =>
														setPatientGeboorteDatumFromDate(staal, e.currentTarget.value)}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3 {editStaalError.patientGeboorteDatum
														? 'border-2 border-red-500'
														: ''}"
												/>
											</div>
											<div class="flex flex-col w-full min-w-[200px]">
												<label for="staalType-{staal.id}">Staaltype</label>
												<select
													id="staalType-{staal.id}"
													class="rounded-lg text-black bg-gray-200 h-12 pl-3 {editStaalError.staalType
														? 'border-2 border-red-500'
														: ''}"
													value={staal.staalType?.id ?? ''}
													on:change={(e) => {
														const tid = Number(e.currentTarget.value);
														const found = staalTypes.find((t) => t.id === tid);
														if (found) staal.staalType = found;
													}}
												>
													{#each staalTypes as t}
														<option value={t.id}>{t.naam}</option>
													{/each}
												</select>
											</div>
										</div>

										<div class="flex flex-row space-x-4 my-4">
											<div class="flex flex-col w-1/2">
												<label for="Laborantnaam-{staal.id}">Naam laborant</label>
												<input
													type="text"
													id="Laborantnaam-{staal.id}"
													bind:value={staal.laborantNaam}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3 {editStaalError.laborantNaam
														? 'border-2 border-red-500'
														: ''}"
												/>
											</div>
											<div class="flex flex-col w-1/2">
												<label for="laborantRnummer-{staal.id}">R-nummer laborant</label>
												<input
													type="text"
													id="laborantRnummer-{staal.id}"
													bind:value={staal.laborantRnummer}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3 {editStaalError.laborantRnummer
														? 'border-2 border-red-500'
														: ''}"
												/>
											</div>
										</div>

										<button
											type="button"
											class="bg-green-500 rounded-lg p-3 text-black h-12 flex flex-row items-center justify-center flex-grow w-56 font-bold text-lg"
											on:click={async () => await editStaal(staal)}
										>
											Opslaan
											<div class="w-5 h-5 ml-5"><IoMdCheckmarkCircle /></div>
										</button>
									</Content>
								{/if}
							</Modal>

							{#if staal?.confirmDelete}
								<button
									type="button"
									on:click={() => deleteStaal(staal?.id)}
									class="h-10 w-10 bg-red-500 p-2 rounded-lg text-white"
								>
									<FaTrashAlt />
								</button>
							{:else}
								<button
									type="button"
									on:click={() => {
										stalen.forEach((s, i) => {
											if (i !== index) s.confirmDelete = false;
										});
										staal.confirmDelete = true;
									}}
									class="h-10 w-10 bg-red-300 p-2 rounded-lg text-white"
								>
									<GoX />
								</button>
							{/if}
						</div>
					{/if}
				</div>
			{/each}
		</div>

		{#if !isLoading && stalen.length === 0}
			<div class="flex justify-center items-center h-32">
				<div class="text-gray-600 text-lg">Geen stalen gevonden</div>
			</div>
		{/if}

		<div class="mt-4 flex items-center justify-center space-x-1">
			<button
				on:click={prevPage}
				disabled={page === 0 || isLoading}
				class="px-3 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
			>
				« Vorige
			</button>

			{#if totalPages > 0}
				{#each getVisiblePages() as pageNum}
					{#if pageNum === '...'}
						<span class="px-3 py-2 text-gray-500">...</span>
					{:else}
						<button
							on:click={() => typeof pageNum === 'number' && goToPage(pageNum - 1)}
							disabled={isLoading}
							class="px-3 py-2 rounded {typeof pageNum === 'number' && page === pageNum - 1
								? 'bg-blue-500 text-white'
								: 'bg-gray-200 text-gray-700 hover:bg-gray-300'} disabled:opacity-50 disabled:cursor-not-allowed"
						>
							{pageNum}
						</button>
					{/if}
				{/each}
			{/if}

			<button
				on:click={nextPage}
				disabled={page >= totalPages - 1 || isLoading}
				class="px-3 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
			>
				Volgende »
			</button>
		</div>

		{#if totalElements > 0}
			<div class="mt-2 text-center text-gray-600 text-sm">
				Pagina {page + 1} van {totalPages} - {totalElements} resultaten
			</div>
		{/if}
	</div>
</div>
