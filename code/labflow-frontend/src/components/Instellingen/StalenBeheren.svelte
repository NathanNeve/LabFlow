<script lang="ts">
	import ButtonTerug from '../buttons/button_terug.svelte';
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';
	import { onMount } from 'svelte';
	import { fetchStalen } from '$lib/fetchFunctions';
	import { getCookie } from '$lib/globalFunctions';
	import { getUserId } from '$lib/globalFunctions';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;
	// types
	import Staal from '../Staal.svelte';

	let token: string = '';
	let searchCode = '';

	let stalen: Staal[] = [];
	let stalenSorted: Staal[] = [];

	onMount(async () => {
		token = getCookie('authToken') || '';
		const result = await fetchStalen();
		if (result) {
			[stalen, stalenSorted] = [result.stalen, result.stalen];
		}
	});

	function filterStalenMetCode() {
		stalenSorted = stalen.filter((staal) => {
			const codeMatch =
				staal.staalCode.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.patientAchternaam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.patientVoornaam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.patientGeboorteDatum.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.laborantNaam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.laborantRnummer.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				staal.aanmaakDatum.toString().toLowerCase().includes(searchCode.toLowerCase());
			return codeMatch;
		});
	}

	function verwijderZoek() {
		searchCode = '';
		stalenSorted = stalen;
	}

	///// DELETE staal /////
	async function deleteStaal(id: string) {
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
			[stalen, stalenSorted] = [result.stalen, result.stalen];
		}
		return;
	}

	///// POST Staal aanmaken /////
	let StaalCode = '';
	let patientAchternaam = '';
	let patientVoornaam = '';
	let patientGeslacht = '';
	let patientGeboorteDatum = '';
	let laborantNaam = '';
	let laborantRnummer = '';
	let userId = getUserId();

	let errorVeldenStaalPOST = {
		staalcode: false,
		naam: false,
		voornaam: false,
		geslacht: false,
		geboortedatum: false,
		laborantNaam: false,
		laborantRnummer: false
	};

	let errorMessageStaalPOST = '';
	async function nieuweStaal() {
		// Resetten van de errorvelden
		errorVeldenStaalPOST = {
			staalcode: false,
			naam: false,
			voornaam: false,
			geslacht: false,
			geboortedatum: false,
			laborantNaam: false,
			laborantRnummer: false
		};
		let isValid = true;
		laborantRnummer = laborantRnummer.toUpperCase();
		const regex = /^R\d{7}$/;
		if (!StaalCode) {
			errorVeldenStaalPOST.staalcode = true;
			isValid = false;
		}
		if (!patientAchternaam) {
			errorVeldenStaalPOST.naam = true;
			isValid = false;
		}
		if (!patientVoornaam) {
			errorVeldenStaalPOST.voornaam = true;
			isValid = false;
		}
		if (!patientGeslacht) {
			errorVeldenStaalPOST.geslacht = true;
			isValid = false;
		}
		if (!patientGeboorteDatum) {
			errorVeldenStaalPOST.geboortedatum = true;
			isValid = false;
		}
		if (!laborantNaam) {
			errorVeldenStaalPOST.laborantNaam = true;
			isValid = false;
		}
		if (!laborantRnummer) {
			errorVeldenStaalPOST.laborantRnummer = true;
			isValid = false;
		}
		if (!laborantRnummer || !regex.test(laborantRnummer)) {
			errorVeldenStaalPOST.laborantRnummer = true;
			errorMessageStaalPOST = 'RNummer moet beginnen met een R en gevolgd worden door 7 cijfers.';
			return;
		}
		if (laborantNaam && laborantRnummer && regex.test(laborantRnummer)) {
			isValid = true;
		}
		if (!isValid) {
			errorMessageStaalPOST = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			const response = await fetch(`${backend_path}/api/createstaal`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					staalCode: StaalCode,
					patientAchternaam: patientAchternaam,
					patientVoornaam: patientVoornaam,
					patientGeslacht: patientGeslacht,
					patientGeboorteDatum: patientGeboorteDatum,
					laborantNaam: laborantNaam,
					laborantRnummer: laborantRnummer,
					user: {
						id: userId
					}
				})
			});
			StaalCode = '';
			patientAchternaam = '';
			patientVoornaam = '';
			patientGeslacht = '';
			patientGeboorteDatum = '';
			laborantNaam = '';
			laborantRnummer = '';
			errorMessageStaalPOST = '';
			const result = await fetchStalen();
			if (result) {
				[stalen, stalenSorted] = [result.stalen, result.stalen];
			}
			if (response.status === 409) {
				errorMessageStaalPOST = 'Staalcode is niet uniek.';
			}
		} catch (error) {
			console.error('Staal kon niet worden aangemaakt: ', error);
		}
		return;
	}

	///// PUT Staal aanpassen /////
	let errorVeldenStaalPUT = {
		staalcode: false,
		naam: false,
		voornaam: false,
		geslacht: false,
		geboortedatum: false,
		laborantNaam: false,
		laborantRnummer: false
	};

	let errorMessageStaalPUT = '';

	async function updateStaal(id: string) {
		const staal = stalen.find((s) => s.id === id);
		if (!staal) return;

		let isValid = true;
		staal.laborantRnummer = staal.laborantRnummer.toUpperCase();
		// regex voor R-nummer: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_expressions/Character_classes
		const regex = /^[RU]\d{7}$/;
		errorVeldenStaalPUT = {
			staalcode: false,
			naam: false,
			voornaam: false,
			geslacht: false,
			geboortedatum: false,
			laborantNaam: false,
			laborantRnummer: false
		};
		if (!staal.StaalCode) {
			errorVeldenStaalPUT.staalcode = true;
			isValid = false;
		}
		if (!staal.laborantNaam) {
			errorVeldenStaalPUT.laborantNaam = true;
		}
		if (!staal.laborantRnummer || !regex.test(staal.laborantRnummer)) {
			errorVeldenStaalPUT.laborantRnummer = true;
			errorMessageStaalPUT = 'RNummer moet beginnen met een R en gevolgd worden door 7 cijfers.';
			return;
		}
		if (staal.laborantNaam && staal.laborantRnummer && regex.test(staal.laborantRnummer)) {
			isValid = true;
		}
		if (!staal.patientAchternaam) {
			errorVeldenStaalPUT.naam = true;
			isValid = false;
		}
		if (!staal.patientVoornaam) {
			errorVeldenStaalPUT.voornaam = true;
			isValid = false;
		}
		if (!staal.patientGeboorteDatum) {
			errorVeldenStaalPUT.geboortedatum = true;
			isValid = false;
		}
		if (!staal.patientGeslacht) {
			errorVeldenStaalPUT.geslacht = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessageStaalPUT = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			await fetch(`${backend_path}/api/updatestaal/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					staalCode: staal.staalCode,
					aanmaakDatum: staal.aanmaakDatum,
					laborantNaam: staal.laborantNaam,
					laborantRnummer: staal.laborantRnummer,
					patientAchternaam: staal.patientAchternaam,
					patientVoornaam: staal.patientVoornaam,
					patientGeslacht: staal.patientGeslacht,
					patientGeboorteDatum: staal.patientGeboorteDatum,
					user: {
						id: staal.user.id
					}
				})
			});
			errorMessageStaalPUT = '';
		} catch (error) {
			console.error('Staal kon niet worden aangepast: ', error);
		}
		return;
	}
</script>

<div class="flex flex-col w-full ml-5">
	<div class="flex flex-row justify-between w-full h-14 mb-5">
		<h1 class="font-bold text-3xl">Stalen beheren</h1>
		<ButtonTerug />
	</div>

	<div class="bg-slate-100 w-full h-full rounded-2xl p-5">
		<div class="flex mb-5 w-full">
			<input
				type="text"
				id="searchCode"
				name="searchCode"
				placeholder="zoeken"
				bind:value={searchCode}
				on:input={filterStalenMetCode}
				class="w-2/5 h-12 rounded-l-lg text-black pl-3"
			/>
			<button
				on:click={verwijderZoek}
				class="w-12 h-12 p-4 flex items-center justify-center bg-red-500 hover:bg-red-600 text-white rounded-r-lg"
			>
				<GoX />
			</button>
		</div>
		<div class="space-y-3">
			<!-- Header -->
			<div
				class="grid grid-cols-9 bg-gray-300 rounded-lg h-10 items-center px-3 font-bold space-x-3"
			>
				<div class="col-span-1 text-left">
					<p>Aanmaakdatum</p>
				</div>
				<div class="col-span-1">
					<p>Code</p>
				</div>
				<div class="col-span-1 text-left">
					<p>Naam</p>
				</div>
				<div class="col-span-1 text-left">
					<p>Voornaam</p>
				</div>
				<div class="col-span-1 text-left">
					<p>Geslacht</p>
				</div>
				<div class="col-span-1 text-left">
					<p>Geboortedatum</p>
				</div>
				<div class="col-span-1 text-left">
					<p>Naam Laborant</p>
				</div>
				<div class="col-span-1 text-left">
					<p>Rnummer</p>
				</div>
				<div class="col-span-1 text-right">
					<p>Acties</p>
				</div>
			</div>
			{#if errorMessageStaalPOST}
				<div class="text-red-500 mb-2">{errorMessageStaalPOST}</div>
			{/if}
			<div class="grid grid-cols-9 space-x-3 bg-white rounded-lg h-20 items-center px-3 shadow-md">
				<div class="col-span-1">
					<p class="bg-white rounded-lg h-14 text-lg pl-3 w-full" />
				</div>
				<div class="col-span-1">
					<input
						type="text"
						id="StaalCode"
						bind:value={StaalCode}
						placeholder="Staalcode"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenStaalPOST.staalcode ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-1">
					<input
						type="text"
						id="patientAchternaam"
						bind:value={patientAchternaam}
						placeholder="Achternaam"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenStaalPOST.naam ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-1">
					<input
						type="text"
						id="patientVoornaam"
						bind:value={patientVoornaam}
						placeholder="Voornaam"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenStaalPOST.voornaam ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-1">
					<select
						bind:value={patientGeslacht}
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenStaalPOST.geslacht ? 'border-2 border-red-500' : ''}"
					>
						<option value="" disabled selected hidden>Geslacht</option>

						<option value="V">Man</option>
						<option value="M">Vrouw</option>
					</select>
				</div>
				<div class="col-span-1">
					<input
						type="date"
						id="patientGeboorteDatum"
						bind:value={patientGeboorteDatum}
						placeholder="Geboortedatum"
						class="px-2 bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenStaalPOST.geboortedatum ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-1">
					<input
						type="text"
						id="laborantNaam"
						bind:value={laborantNaam}
						placeholder="Naam Laborant"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenStaalPOST.laborantNaam ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-1">
					<input
						type="text"
						id="laborantRnummer"
						bind:value={laborantRnummer}
						placeholder="Rnummer"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenStaalPOST.laborantRnummer ? 'border-2 border-red-500' : ''}"
					/>
				</div>

				<div class="col-span-1 flex justify-end">
					<button
						type="button"
						class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"
						on:click={nieuweStaal}
						aria-label="Nieuwe categorie toevoegen"
					>
						<FaPlus />
					</button>
				</div>
			</div>
			{#if errorMessageStaalPUT}
				<div class="text-red-500 mb-2">{errorMessageStaalPUT}</div>
			{/if}
			<div class="space-y-3">
				{#each stalenSorted as staal, index}
					<div
						class="grid grid-cols-9 bg-white rounded-lg h-20 items-center px-3 shadow-md space-x-3"
					>
						<div class="col-span-1">
							<span class=" rounded-lg h-14 text-lg pl-3 w-full p-2">
								{new Date(staal.aanmaakDatum).toLocaleDateString() || 'Loading...'}
							</span>
						</div>
						<div class="col-span-1">
							<input
								type="text"
								on:blur={() => updateStaal(staal.id)}
								id="staal-{staal?.id}"
								bind:value={staal.staalCode}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>
						<div class="col-span-1">
							<input
								type="text"
								on:blur={() => updateStaal(staal.id)}
								bind:value={staal.patientAchternaam}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>
						<div class="col-span-1">
							<input
								type="text"
								on:blur={() => updateStaal(staal.id)}
								bind:value={staal.patientVoornaam}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>
						<div class="col-span-1">
							<select
								on:change={() => updateStaal(staal.id)}
								bind:value={staal.patientGeslacht}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							>
								<option value="V">Man</option>
								<option value="M">Vrouw</option>
							</select>
						</div>
						<div class="col-span-1">
							<input
								type="date"
								on:blur={() => updateStaal(staal.id)}
								bind:value={staal.patientGeboorteDatum}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full p-2"
							/>
						</div>
						<div class="col-span-1">
							<input
								type="text"
								on:blur={() => updateStaal(staal.id)}
								bind:value={staal.laborantNaam}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>
						<div class="col-span-1">
							<input
								type="text"
								on:blur={() => updateStaal(staal.id)}
								bind:value={staal.laborantRnummer}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>

						<!-- Acties -->
						<div class="col-span-1 flex justify-end">
							{#if staal.confirmDelete}
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
										stalenSorted.forEach((c, i) => {
											if (i !== index) c.confirmDelete = false;
										});
										staal.confirmDelete = true;
									}}
									class="h-10 w-10 bg-red-300 p-2 rounded-lg text-white"
								>
									<GoX />
								</button>
							{/if}
						</div>
					</div>
				{/each}
			</div>
		</div>
	</div>
</div>
