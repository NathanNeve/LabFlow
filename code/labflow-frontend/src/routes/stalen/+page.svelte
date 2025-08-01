<script lang="ts">
	import Nav from '../../components/nav.svelte';
	import { onMount } from 'svelte';
	import { getRolNaam_FromToken } from '$lib/globalFunctions';
	import { fetchStalen } from '$lib/fetchFunctions';
	import { id } from '../../components/Modal/store';

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
	// modal
	import Modal from '../../components/Modal/Modal.svelte';
	import Trigger from '../../components/Modal/Trigger.svelte';
	import Content from '../../components/Modal/Content.svelte';
	import { staalCodeStore } from '$lib/store';
	import { goto } from '$app/navigation';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;
	// types
	import type { Staal } from '$lib/types/dbTypes';

	let openModalTestId: number | null = null;

	// achtergrond en klikbaar maken van instellingen gebaseerd op rol
	let bgColor = 'bg-blue-400';
	let pointerEvent = 'pointer-events-auto';
	const rol = getRolNaam_FromToken();
	if (rol !== 'admin') {
		bgColor = 'bg-gray-400';
		pointerEvent = 'pointer-events-none';
	}

	// fetchen van stalen
	let stalen: Staal[] = [];
	let stalenSorted: Staal[] = [];
	let statussen: string[] = [];
	let searchCode = '';
	let searchDate = '';

	let token: string = '';

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
	/*
	onMount(async () => {
		token = getCookie('authToken') || '';
		const result = await fetchStalen();
		if (result) {
			stalen = result.stalen;
			stalenSorted = result.stalen;
			statussen = await fetchStatussen();
		}
	});*/

	// Function om te filteren op staalcode en datum
	function filterStalen() {
		stalenSorted = stalen.filter((staal) => {
			const codeMatch =
				staal.staalCode.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.patientAchternaam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.patientVoornaam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.patientGeboorteDatum.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.laborantNaam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.laborantRnummer.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.aanmaakDatum.toString().toLowerCase().includes(searchCode.toLowerCase());
			// Converteren searchdate en aanmaakdatum naar een datumobject
			const searchDateObject = new Date(searchDate);
			const aanmaakDatumObject = new Date(staal.aanmaakDatum);

			const isValidDate = !isNaN(searchDateObject.getTime());

			// Date match: vergelijken van de datums, alleen als de searchDate een geldige datum is
			const dateMatch = isValidDate
				? searchDateObject.toDateString() === aanmaakDatumObject.toDateString()
				: true;

			return codeMatch && dateMatch;
		});
	}

	// Functie om te filteren op status
	let filteredStatus = '';

	function filterStatus() {
		// Make sure filteredStatus is in uppercase for a consistent comparison
		const normalizedFilteredStatus = filteredStatus.toUpperCase();

		stalenSorted = stalen.filter((staal) => {
			// Ensure staal.status is in uppercase as well
			const normalizedStaalStatus = staal.status.toUpperCase();

			// Compare the statuses directly
			return normalizedStaalStatus === normalizedFilteredStatus;
		});
	}

	function verwijderZoek() {
		searchCode = '';
		stalenSorted = stalen;
	}

	function deleteFilters() {
		searchCode = '';
		searchDate = '';
		filteredStatus = '';
		filterStalen();
	}

	// set store
	function setStore(staalCode: string) {
		staalCodeStore.set(staalCode);
		goto('stalen/nieuw');
	}

	// set staalcode in store en ga naar waarden registreren / afdrukken pdf
	function setStoreGoToDependingStatus(staal: Staal) {
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
		} catch (error) {
			console.error('Staal kon niet worden verwijderd: ', error);
		}
		const result = await fetchStalen();
		if (result) {
			stalen = result.stalen;
			stalenSorted = result.stalen;
		}
	}

	// edit staal
	let editStaalErrorMessage = '';
	async function editStaal(staal: Staal) {
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
				return ($id = null);
			}
		} catch (error) {
			console.error('Staal kon niet worden aangepast: ', error);
			return;
		}
	}
</script>

<Nav />
<div class="px-8 flex flex-row space-x-5">
	<div class="flex flex-col space-y-5">
		<a
			on:click={() => setStore('')}
			href="/stalen/nieuw"
			class="bg-blue-400 flex flex-col items-center justify-center w-56 h-56 rounded-2xl"
		>
			<div class="w-28 h-28 text-white flex items-center justify-center">
				<GoPlus />
			</div>
			<p class="text-white text-2xl text-center mt-2">Nieuwe staal</p>
		</a>

		<a
			href={rol === 'admin' ? '/instellingen' : '#'}
			class="{bgColor} flex flex-col items-center justify-center w-56 h-56 rounded-2xl {pointerEvent}"
		>
			<div class="w-28 h-28 text-white flex items-center justify-center">
				<IoMdSettings />
			</div>
			<p class="text-white text-2xl text-center mt-2">Instellingen</p>
		</a>
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
					on:input={filterStalen}
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
					on:change={filterStatus}
					class="w-full h-14 rounded-lg text-black px-3 border border-gray-300"
				>
					<option value="" disabled>Status</option>
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
					on:input={filterStalen}
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

		<div class="space-y-3">
			{#each stalenSorted as staal, index}
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
							<p>{new Date(staal?.aanmaakDatum).toLocaleDateString() || ''}</p>
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
							<p>{new Date(staal?.patientGeboorteDatum).toLocaleDateString() || ''}</p>
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
																	name="radio"
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
																	name="radio"
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
												stalenSorted.forEach((s, i) => {
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
	</div>
</div>
