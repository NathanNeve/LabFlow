<script lang="ts">
	import Nav from '../../components/nav.svelte';
	import { onMount } from 'svelte';
	import { fetchStalen } from '$lib/fetchFunctions';
	import { id } from '../../components/Modal/store';

	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaRegEdit from 'svelte-icons/fa/FaRegEdit.svelte';
	// @ts-ignore
	import IoMdCheckmarkCircle from 'svelte-icons/io/IoMdCheckmarkCircle.svelte';

	// modal
	import Modal from '../../components/Modal/Modal.svelte';
	import Trigger from '../../components/Modal/Trigger.svelte';
	import Content from '../../components/Modal/Content.svelte';
	import { getCookie } from '$lib/globalFunctions';
	import { staalCodeStore } from '$lib/store';
	import { goto } from '$app/navigation';

	// types
	import type { Staal as StaalBase } from '$lib/types/dbTypes';
	type StaalUI = StaalBase & { confirmDelete?: boolean }; // extends staal type to add confirmdelete
	import type { StalenSearchParams } from '$lib/types/searchTypes';

	// buttons
	import ButtonNieuweStaal from '../../components/buttons/button_plus_large.svelte';
	import ButtonInstellingen from '../../components/buttons/button_settings_large.svelte';

	export let data;
	// pull values from load() in +page.ts
	let { rol, statussen, stalen, totalPages, totalElements } = data as {
		rol: string;
		statussen: string[];
		stalen: StaalUI[];
		totalPages: number;
		totalElements: number;
	};

	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	let openModalTestId: number | null = null;

	let searchCode = '';
	let searchDate = '';
	let filteredStatus = '';
	let page = 0;
	let isLoading = false;

	const token = getCookie('authToken') ?? '';

	let editStaalError = {
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

	function resetErrors() {
		editStaalError = {
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
		editStaalErrorMessage = '';
	}

	// functie om de datum te formatteren naar dd/mm/yyyy
	function formatDateToDDMMYYYY(dateStr: string): string {
		if (!dateStr) return '';
		const date = new Date(dateStr);
		const day = String(date.getDate()).padStart(2, '0');
		const month = String(date.getMonth() + 1).padStart(2, '0');
		const year = date.getFullYear();
		return `${day}/${month}/${year}`;
	}

	// Backend verwacht YYYY-MM-DD format (ISO date)
	function formatDateForBackend(dateStr: string): string {
		if (!dateStr) return '';
		return dateStr;
	}

	function verwijderZoek() {
		searchCode = '';
		loadStalen();
	}

	function deleteFilters() {
		searchCode = '';
		searchDate = '';
		filteredStatus = '';
		page = 0;
		loadStalen();
	}

	// Apply filters - triggert een backend call
	function applyFilters() {
		page = 0; // Reset naar de eerste pagina wanneer filters worden toegepast
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
			if (filteredStatus) {
				searchParams.filteredStatus = filteredStatus;
			}

			const data = await fetchStalen(page, 25, searchParams);
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

	// Event handlers for form inputs with debouncing
	let searchTimeout: ReturnType<typeof setTimeout>;
	function handleSearchInput() {
		clearTimeout(searchTimeout);
		searchTimeout = setTimeout(() => {
			applyFilters();
		}, 500); // 500ms debounce
	}

	function handleDateChange() {
		applyFilters();
	}

	function handleStatusChange() {
		applyFilters();
	}

	// set store
	function setStore(staalCode: string) {
		staalCodeStore.set(staalCode);
		goto('stalen/nieuw');
	}

	// set staalcode in store en ga naar waarden registreren / afdrukken pdf
	function setStoreGoToDependingStatus(staal: StaalUI) {
		staalCodeStore.set(staal.staalCode);
		if (staal.status === 'GEREGISTREERD' || staal.status === 'KLAAR') {
			goto('stalen/registreren');
		} else if (staal.status === 'AANGEMAAKT') {
			goto('stalen/nieuw');
		} else {
			return;
		}
	}

	// delete staal
	async function deleteStaal(id: number) {
		try {
			await fetch(`${backend_path}/api/deletestaal/${id}`, {
				method: 'DELETE',
				headers: {
					Authorization: 'Bearer ' + token
				}
			});
			// reload na verwijderen
			await loadStalen();
		} catch (error) {
			console.error('Staal kon niet worden verwijderd: ', error);
		}
	}

	// edit staal
	let editStaalErrorMessage = '';
	async function editStaal(staal: StaalUI) {
		editStaalError = {
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
		let isValid = true;
		const regex = /^[RU]\d{7}$/;

		if (!staal.staalCode) {
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
		if (!staal.user) {
			editStaalError.user = true;
			isValid = false;
		}
		if (!staal.registeredTests) {
			editStaalError.registeredTests = true;
			isValid = false;
		}
		if (!isValid) {
			editStaalErrorMessage = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			const response = await fetch(`${backend_path}/api/updatestaal/${staal.id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					staalCode: staal.staalCode,
					patientVoornaam: staal.patientVoornaam,
					patientAchternaam: staal.patientAchternaam,
					patientGeboorteDatum: staal.patientGeboorteDatum,
					patientGeslacht: staal.patientGeslacht,
					laborantNaam: staal.laborantNaam,
					laborantRnummer: staal.laborantRnummer,
					user: {
						id: staal.user.id
					}
				})
			});
			const data = await response.status;
			if (data === 409) {
				editStaalErrorMessage = 'De staalcode bestaat al.';
			} else {
				$id = null;
				// Reload current page after edit
				await loadStalen();
			}
		} catch (error) {
			console.error('Staal kon niet worden aangepast: ', error);
			return;
		}
	}

	// functies voor paginering
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

	// Aantal pagina's berekenen
	function getVisiblePages() {
		const delta = 2; // toon 2 pagina's voor en na de huidige pagina
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

		return rangeWithDots.filter((v, i, arr) => arr.indexOf(v) === i); // Verwijder dubbele waarden
	}

	// Initiele load van data, haalt statussen en stalen op
	async function load() {}

	onMount(load);
</script>

<Nav />
<div class="px-8 flex flex-row space-x-5">
	<div class="flex flex-col space-y-5">
		<ButtonNieuweStaal label="Nieuwe staal" />
		<ButtonInstellingen label="Instellingen" />
	</div>
	<div class="bg-slate-200 w-full h-full rounded-2xl p-5">
		<!-- filteren op code en datum -->
		<div class="flex mb-5 items-center space-x-5">
			<!-- Search Code Input -->
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

			<!-- Filteren op status -->
			<div class="flex items-center w-1/5">
				<select
					id="searchStatus"
					name="searchStatus"
					bind:value={filteredStatus}
					on:change={handleStatusChange}
					class="w-full h-14 rounded-lg text-black px-3 border border-gray-300"
				>
					<option value="">Alle statussen</option>
					{#each statussen as status}
						<option value={status}>{status.toLowerCase()}</option>
					{/each}
				</select>
			</div>

			<!-- Filteren op aanmaakdatum -->
			<div class="flex items-center w-1/5">
				<label
					for="searchDate"
					class="text-black bg-gray-200 h-14 flex items-center justify-center rounded-l-lg px-3 border border-gray-300"
				>
					Datum
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

			<!-- Delete Filters Button -->
			<button
				class="bg-blue-600 rounded-lg h-14 w-1/6 flex items-center justify-center text-white hover:bg-blue-700"
				type="button"
				on:click={deleteFilters}
			>
				Verwijder Filters
			</button>
		</div>

		<!-- Loading indicator -->
		{#if isLoading}
			<div class="flex justify-center items-center h-32">
				<div class="text-gray-600 text-lg">Laden...</div>
			</div>
		{/if}

		<div class="space-y-3">
			{#each stalen as staal, index}
				<div class="flex items-center justify-between">
					<button
						type="button"
						class="grid {rol !== 'admin'
							? 'grid-cols-7'
							: 'grid-cols-7'} gap-4 rounded-lg h-16 items-center px-3
							{rol != 'admin' ? 'w-full ' : 'w-11/12'}
							{staal.status === 'AANGEMAAKT' ? 'bg-white' : ''}	
							{staal.status === 'KLAAR' ? 'bg-green-50' : ''}
							{staal.status === 'GEREGISTREERD' ? 'bg-blue-100' : ''}
							"
						on:click={() => {
							if (rol !== 'admin') {
								setStoreGoToDependingStatus(staal);
							} else {
								setStore(staal.staalCode);
							}
						}}
					>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400">Code</p>
							<p>{staal?.staalCode || ''}</p>
						</div>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400">Aanmaakdatum</p>
							<p>{formatDateToDDMMYYYY(staal?.aanmaakDatum)}</p>
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
							<p>{staal?.patientGeslacht === 'V' ? 'Vrouw' : 'Man'}</p>
						</div>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400">Geboortedatum</p>
							<p>{formatDateToDDMMYYYY(staal?.patientGeboorteDatum)}</p>
						</div>
						<div class="flex flex-col justify-center">
							<p class="text-gray-400 font-bold">Laborant</p>
							<p>{staal?.laborantNaam || ''}</p>
						</div>
					</button>

					{#if rol === 'admin'}
						<div class="col-span-1 flex justify-end space-x-2">
							<!-- Admin-only CRUD buttons -->
							{#if rol === 'admin'}
								<div class="col-span-1 flex justify-end space-x-2">
									<!-- Edit Button -->
									<Modal>
										<Trigger>
											<button
												type="button"
												class="h-10 w-10 bg-blue-400 p-2 rounded-lg text-white"
												on:click={async () => {
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
												<div class="flex flex-row space-x-4 my-4">
													<div class="flex flex-col w-1/3">
														<label for="staalCode-{staal.staalCode}">Staalcode</label>
														<input
															type="text"
															id="staalCode-{staal.id}"
															name="staalCode"
															bind:value={staal.staalCode}
															class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                    {editStaalError.staalCode
																? 'border-2 border-red-500'
																: ''}"
														/>
													</div>
													<div class="flex flex-col w-1/3">
														<label for="patientVoornaam-{staal.id}">Voornaam Patient</label>
														<input
															type="text"
															id="staalCode-{staal.id}"
															name="Patientvoornaam"
															bind:value={staal.patientVoornaam}
															class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                    {editStaalError.patientVoornaam
																? 'border-2 border-red-500'
																: ''}"
														/>
													</div>
													<div class="flex flex-col w-1/3">
														<label for="Patientachternaam-{staal.id}">Achternaam Patient</label>
														<input
															type="text"
															id="Patientachternaam-{staal.id}"
															name="Patientachternaam"
															bind:value={staal.patientAchternaam}
															class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                    {editStaalError.patientAchternaam
																? 'border-2 border-red-500'
																: ''}"
														/>
													</div>
													<div class="flex flex-col w-1/3">
														<label for="patientGeslacht-{staal.id}">Geslacht Patient</label>
														<div>
															<label
																class="container mr-5 {editStaalError.patientGeslacht
																	? 'text-red-500 font-bold'
																	: ''}"
															>
																<input
																	type="radio"
																	name="radio-{staal.id}"
																	bind:group={staal.patientGeslacht}
																	value="M"
																/>
																Man
															</label>
															<label
																class="container {editStaalError.patientGeslacht
																	? 'text-red-500 font-bold'
																	: ''}"
															>
																<input
																	type="radio"
																	name="radio-{staal.id}"
																	bind:group={staal.patientGeslacht}
																	value="V"
																/>
																Vrouw
															</label>
														</div>
													</div>
												</div>

												<div class="flex flex-row space-x-4 my-4">
													<div class="flex flex-col w-1/2">
														<label for="Laborantnaam-{staal.id}">Naam Laborant</label>
														<input
															type="text"
															id="Laborantnaam-{staal.id}"
															name="Laborantnaam"
															bind:value={staal.laborantNaam}
															class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                    {editStaalError.laborantNaam
																? 'border-2 border-red-500'
																: ''}"
														/>
													</div>
													<div class="flex flex-col w-1/2">
														<label for="laborantRnummer-{staal.id}">Rnummer Laborant</label>
														<input
															type="text"
															id="laborantRnummer-{staal.id}"
															name="laborantRnummer"
															bind:value={staal.laborantRnummer}
															class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                    {editStaalError.laborantRnummer
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

									<!-- Delete button -->
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
					{/if}
				</div>
			{/each}
		</div>

		<!-- Show message when no results -->
		{#if !isLoading && stalen.length === 0}
			<div class="flex justify-center items-center h-32">
				<div class="text-gray-600 text-lg">Geen stalen gevonden</div>
			</div>
		{/if}

		<!-- Pagination -->
		<div class="mt-4 flex items-center justify-center space-x-1">
			<!-- Vorige -->
			<button
				on:click={prevPage}
				disabled={page === 0 || isLoading}
				class="px-3 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
			>
				« Vorige
			</button>

			<!-- Pagina nummers -->
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

			<!-- Volgende -->
			<button
				on:click={nextPage}
				disabled={page >= totalPages - 1 || isLoading}
				class="px-3 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
			>
				Volgende »
			</button>
		</div>

		<!-- Results info -->
		{#if totalElements > 0}
			<div class="mt-2 text-center text-gray-600 text-sm">
				Pagina {page + 1} van {totalPages} - {totalElements} resultaten
			</div>
		{/if}
	</div>
</div>
