<script lang="ts">
	import Nav from '../../../components/nav.svelte';
	import { goto } from '$app/navigation';
	import { getCookie, fetchAll } from '$lib/globalFunctions';
	import { getRolNaam_FromToken } from '$lib/globalFunctions';
	import { getUserId } from '$lib/globalFunctions';
	import { onMount } from 'svelte';

	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaRegEdit from 'svelte-icons/fa/FaRegEdit.svelte';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';

	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	import { staalCodeStore } from '$lib/store';
	// @ts-ignore
	import IoMdCheckmarkCircle from 'svelte-icons/io/IoMdCheckmarkCircle.svelte';
	// popup laborantgegevens, test & categorie aanmaken
	import Modal from '../../../components/Modal/Modal.svelte';
	import AutoTrigger from '../../../components/Modal/AutoTrigger.svelte';
	import ContentWithoutClose from '../../../components/Modal/ContentWithoutClose.svelte';
	import { id } from '../../../components/Modal/store.js';
	import Trigger from '../../../components/Modal/Trigger.svelte';
	import Content from '../../../components/Modal/Content.svelte';
	// https://svelte-awesome-color-picker.vercel.app/
	import ColorPicker, { ChromeVariant } from 'svelte-awesome-color-picker';
	import { loadTestCategorieën, loadEenheden } from '$lib/fetchFunctions';
	import { fetchStaal_StaalCode } from '$lib/fetchFunctions';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	// types
	import type { Test, TestCategorie, Eenheid } from '$lib/types/dbTypes';
	import ButtonTerug from '../../../components/buttons/button_terug.svelte';
	import ButtonVerder from '../../../components/buttons/button_verder.svelte';
	// wrapper for array of testcodes (strings)
	interface TestCodeWrapper {
		test: {
			testCode: string;
		};
	}

	// voor het inladen van crud voor admins
	const rol = getRolNaam_FromToken();
	let userId = getUserId();

	let tests: Test[] = [];
	let testsSorted: Test[] = [];
	let searchCode = '';
	let token: string = '';

	// nieuwe staalcode
	let nieuweStaalCode: string = '';
	let naam = '';
	let voornaam = '';
	let geslacht = '';
	let geboortedatum = '';

	// laborantgegevens
	let laborantNaam = '';
	let laborantRnummer = '';

	let errorVeldenStaal = {
		naam: false,
		voornaam: false,
		geslacht: false,
		geboortedatum: false,
		laborantNaam: false,
		laborantRnummer: false
	};

	// variabelen voor popup test aanmaken
	let testCode = '';
	let testNaam = '';
	let eenheid = '';
	let testcategorie = '';

	let testcategorieën: TestCategorie[] = [];
	let eenheden: Eenheid[] = [];
	let errorVeldenTest = {
		testCode: false,
		testNaam: false,
		eenheid: false,
		testcategorie: false
	};

	// variabelen voor popup categorie aanmaken
	let categorienaam = '';
	let hex = '';

	let errorVeldenCategorie = {
		categorienaam: false,
		kleur: false
	};

	// array geselecteerde testcodes
	let geselecteerdeTests: string[] = [];

	onMount(() => {
		token = getCookie('authToken') || '';
		loadTests();
	});

	// neem de id van de store
	let sampleCode: string | undefined;
	staalCodeStore.subscribe((value) => {
		sampleCode = value;
	});

	let loading = true;
	let staalId: string = '';
	// fetchen van tests op "tests"
	// verkrijgen nieuwe staalcode op "/api/newStaalCode"
	// als we in de session storage een staalcode hebben, binden we deze zijn waarden aan de variabelen
	async function loadTests() {
		if (token != null && sampleCode == '') {
			try {
				tests = await fetchAll(token, 'tests');
				testsSorted = tests;
				nieuweStaalCode = await fetchAll(token, 'newStaalCode');
				loading = false;
			} catch (error) {
				console.error('testen kon niet gefetched worden:', error);
			}
		} else if (token != null && sampleCode != '') {
			try {
				tests = await fetchAll(token, 'tests');
				const test = sampleCode ? await fetchStaal_StaalCode(sampleCode) : null;
				testsSorted = tests;
				// binden van de bestaande staalwaarden aan de variabelen
				nieuweStaalCode = test.staalCode;
				naam = test.patientAchternaam;
				voornaam = test.patientVoornaam;
				geslacht = test.patientGeslacht;
				geboortedatum = test.patientGeboorteDatum;
				laborantNaam = test.laborantNaam;
				laborantRnummer = test.laborantRnummer;
				geselecteerdeTests = test.registeredTests.map(
					(test: TestCodeWrapper) => test.test.testCode
				);
				// setten van de id die we aan het aanpassen zijn
				staalId = test.id;
				loading = false;
			} catch (error) {
				console.error('testen kon niet gefetched worden:', error);
			}
		} else {
			console.error('jwt error');
			goto('/');
		}
	}

	function setLaborant() {
		let isValid = false;
		laborantRnummer = laborantRnummer.toUpperCase();
		// regex voor R-nummer: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_expressions/Character_classes
		const regex = /^[RU]\d{7}$/;

		if (!laborantNaam) {
			errorVeldenStaal.laborantNaam = true;
		}
		if (!laborantRnummer || !regex.test(laborantRnummer)) {
			errorVeldenStaal.laborantRnummer = true;
		}

		if (laborantNaam && laborantRnummer && regex.test(laborantRnummer)) {
			isValid = true;
		}
		if (isValid) {
			return ($id = null);
		}
		return;
	}

	// zoeken op basis van code
	function filterTests() {
		testsSorted = tests.filter((test) => {
			const codeMatch =
				test.naam.toLowerCase().includes(searchCode.toLowerCase()) ||
				test.testCode.toLowerCase().includes(searchCode.toLowerCase()) ||
				test.testcategorie.naam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				test.eenheid.afkorting.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				test.eenheid.naam.toString().toLowerCase().includes(searchCode.toLowerCase());
			return codeMatch;
		});
	}

	// verwijderen van zoekparameter, terug alle tests tonen
	function verwijderZoek() {
		searchCode = '';
		testsSorted = tests;
	}

	// verwijderen van geselecteerde testen
	function verwijderSelectie() {
		geselecteerdeTests = [];
	}

	// toevoegen van geselecteerde test, of verwijderen indien al geselecteerd
	function toggleTestSelectie(testCode: string) {
		if (geselecteerdeTests.includes(testCode)) {
			geselecteerdeTests = geselecteerdeTests.filter((code) => code !== testCode);
		} else {
			geselecteerdeTests = [...geselecteerdeTests, testCode];
		}
	}

	// POST: Aanmaken van een nieuwe test
	let errorMessageTest = '';
	async function nieuweTest() {
		// resetten errorvelden
		errorVeldenTest = { testCode: false, testNaam: false, eenheid: false, testcategorie: false };
		let isValid = true;

		if (!testCode) {
			errorVeldenTest.testCode = true;
			isValid = false;
		}
		if (!testNaam) {
			errorVeldenTest.testNaam = true;
			isValid = false;
		}
		if (!eenheid) {
			errorVeldenTest.eenheid = true;
			isValid = false;
		}
		if (!testcategorie) {
			errorVeldenTest.testcategorie = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessageTest = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			await fetch(`${backend_path}/api/createtest`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					testCode: testCode,
					naam: testNaam,
					eenheid: {
						id: eenheid
					},
					testcategorie: {
						id: testcategorie
					}
				})
			});
		} catch (error) {
			console.error('test kon niet worden aangemaakt: ', error);
		}
		tests = await fetchAll(token, 'tests'); // tests refreshen, triggert een refresh
		testsSorted = tests;
		return ($id = null);
	}

	// helper functie om te checken of er een warning moet worden gegeven 'heb je zeker nagekeken of je de extra tests hebt toegevoegd?'
	let isWarningAcknowledged = false; // Tracken of warning getoond is of niet

	function checkWarning(geselecteerdeTestsArray: TestCodeWrapper[]) {
		if (
			geselecteerdeTestsArray.some((test) => test.test.testCode !== 'X') ||
			geselecteerdeTestsArray.length === 0
		) {
			alert('Heb je nagekeken dat je geen notitie moet toevoegen?');
			isWarningAcknowledged = true; // setten de warning als acknowledged (want hij is getoond)
		}
	}

	let errorMessageStaal = '';

	// POST: Aanmaken van een nieuwe staal
	async function nieuweStaal() {
		// Resetten van de errorvelden
		errorVeldenStaal = {
			naam: false,
			voornaam: false,
			geslacht: false,
			geboortedatum: false,
			laborantNaam: true,
			laborantRnummer: true
		};

		// Validatie van de input
		let isValid = true;

		if (!naam) {
			errorVeldenStaal.naam = true;
			isValid = false;
		}
		if (!voornaam) {
			errorVeldenStaal.voornaam = true;
			isValid = false;
		}
		if (!geboortedatum) {
			errorVeldenStaal.geboortedatum = true;
			isValid = false;
		}
		if (!geslacht) {
			errorVeldenStaal.geslacht = true;
			isValid = false;
		}
		// errorMessageStaal tonen indien niet alle velden zijn ingevuld
		if (!isValid) {
			errorMessageStaal = 'Vul alle verplichte velden in.';
			return;
		}

		const geselecteerdeTestsArray: TestCodeWrapper[] = Array.from(geselecteerdeTests).map(
			(testCode) => ({
				test: { testCode: testCode }
			})
		);

		// als er de test met code 'X' is geselecteerd, dan wordt de warning niet getoond
		if (geselecteerdeTestsArray.some((test) => test.test.testCode === 'X')) {
			isWarningAcknowledged = true;
		}

		if (!isWarningAcknowledged) {
			checkWarning(geselecteerdeTestsArray);
			isWarningAcknowledged = true; // Set de warning als al getoond is
			errorMessageStaal = '';
			return; // wachten voor volgende click
		}

		try {
			await fetch(` ${backend_path}/api/createstaal`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					staalCode: nieuweStaalCode,
					patientAchternaam: naam,
					patientVoornaam: voornaam,
					patientGeslacht: geslacht,
					patientGeboorteDatum: geboortedatum,
					laborantNaam: laborantNaam,
					laborantRnummer: laborantRnummer,
					user: {
						id: userId
					},
					registeredTests: geselecteerdeTestsArray
				})
			});
			// doorgeven van aangemaakte staalcode naar volgend scherm
			staalCodeStore.set(nieuweStaalCode);
		} catch (error) {
			console.error('staal kon niet worden aangemaakt: ', error);
		}
		isWarningAcknowledged = false; // Reset de warning zodat de knop geklikt kan worden
		return goto('/stalen/labels');
	}

	// PUT: Aanpassen van een bestaande staal
	async function staalAanpassen() {
		try {
			// Reset error fields
			errorVeldenStaal = {
				naam: false,
				voornaam: false,
				geslacht: false,
				geboortedatum: false,
				laborantNaam: true,
				laborantRnummer: true
			};

			// Required fields to validate
			const requiredFields = { naam, voornaam, geboortedatum, geslacht };
			let isValid = true;

			for (const [field, value] of Object.entries(requiredFields)) {
				if (!value) {
					errorVeldenStaal[field as keyof typeof errorVeldenStaal] = true;
					isValid = false;
				}
			}

			if (!isValid) {
				errorMessageStaal = 'Vul alle verplichte velden in.';
				return;
			}

			// Build test array
			const geselecteerdeTestsArray = Array.from(geselecteerdeTests, (testCode) => ({
				test: { testCode }
			}));

			// Check if warning is needed
			const hasOverrideTest = geselecteerdeTestsArray.some((test) => test.test.testCode === 'X');

			if (!hasOverrideTest && !isWarningAcknowledged) {
				checkWarning(geselecteerdeTestsArray);
				isWarningAcknowledged = true;
				errorMessageStaal = '';
				return; // Wait for next click after showing warning
			}

			// Send update to backend
			await fetch(`${backend_path}/api/updatestaal/${staalId}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${token}`
				},
				body: JSON.stringify({
					staalCode: nieuweStaalCode,
					patientAchternaam: naam,
					patientVoornaam: voornaam,
					patientGeslacht: geslacht,
					patientGeboorteDatum: geboortedatum,
					laborantNaam,
					laborantRnummer,
					user: { id: 2 },
					registeredTests: geselecteerdeTestsArray
				})
			});

			// Store staalcode for next screen
			staalCodeStore.set(nieuweStaalCode);

			// Reset warning for next use
			isWarningAcknowledged = false;

			// Navigate to labels
			return goto('/stalen/labels');
		} catch (error) {
			console.error('Staal kon niet worden aangemaakt:', error);
		}
	}

	// post or put functie, afhankelijk van of er al een staalcode is in de session storage
	function postOrPut() {
		if (sampleCode === '') {
			nieuweStaal();
		} else {
			staalAanpassen();
		}
	}

	let errorMessageCategorie = '';
	// POST: Aanmaken van een nieuwe categorie
	async function nieuweCategorie() {
		errorVeldenCategorie = { categorienaam: false, kleur: false };
		let isValid = true;
		const regex = /^#([0-9A-F]{3}){1,2}$/i;

		if (!categorienaam) {
			errorVeldenCategorie.categorienaam = true;
			isValid = false;
		}
		if (!hex || !regex.test(hex)) {
			errorVeldenCategorie.kleur = true;
			isValid = false;
		}
		// errorMessageStaal tonen indien niet alle velden zijn ingevuld
		if (!isValid) {
			errorMessageCategorie = 'Geef de categorie een naam en een kleur.';
			return;
		}

		try {
			await fetch(`${backend_path}/api/createtestcategorie`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					naam: categorienaam,
					kleur: hex
				})
			});
		} catch (error) {
			console.error('categorie kon niet worden aangemaakt: ', error);
		}
		return ($id = null);
	}

	// crud buttons voor admin
	async function deleteTest(id: number) {
		try {
			await fetch(`${backend_path}/api/deletetest/${id}`, {
				method: 'DELETE',
				headers: {
					Authorization: 'Bearer ' + token
				}
			});
		} catch (error) {
			console.error('Test kon niet worden verwijderd: ', error);
		}
		loadTests();
		return;
	}

	// variabele voor het openen van de modal voor de juiste test
	let openModalTestId: number | null = null;

	let editTestError = {
		testCode: false,
		naam: false,
		eenheid: false,
		testcategorie: false
	};
	let editTestErrorMessage = '';
	// edit de test: PUT request
	async function editTest(test: Test) {
		editTestError = { testCode: false, naam: false, eenheid: false, testcategorie: false };
		let isValid = true;

		if (!test.testCode) {
			editTestError.testCode = true;
			isValid = false;
		}
		if (!test.naam) {
			editTestError.naam = true;
			isValid = false;
		}
		if (!test.eenheid.id) {
			editTestError.eenheid = true;
			isValid = false;
		}
		if (!test.testcategorie.id) {
			editTestError.testcategorie = true;
			isValid = false;
		}
		if (!isValid) {
			editTestErrorMessage = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			const response = await fetch(`${backend_path}/api/updatetest/${test.id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					testCode: test.testCode,
					naam: test.naam,
					eenheid: {
						id: test.eenheid.id
					},
					testcategorie: {
						id: test.testcategorie.id
					}
				})
			});
			if (response.status === 409) {
				editTestErrorMessage = 'De testcode bestaat al.';
			} else {
				return ($id = null);
			}
		} catch (error) {
			console.error('test kon niet worden aangepast: ', error);
			return;
		}
	}
</script>

<!-- popup modal voor het ingeven van de laborantgegevens die automatisch opent onmount van de pagina -->
<!-- loading variabele zorgt ervoor dat de modal pas opent wanneer de data is ingeladen -->
{#if !loading}
	<Modal>
		<ContentWithoutClose>
			<h1 class="font-bold text-xl mb-2">laborantgegevens</h1>
			<div class="flex space-x-4 mb-4">
				<div class="flex flex-col w-1/2">
					<label for="naam">Volledige Naam</label>
					<input
						type="text"
						id="naam"
						name="naam"
						bind:value={laborantNaam}
						class="rounded-lg text-black bg-gray-200 h-12 pl-3 {errorVeldenStaal.laborantNaam
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="flex flex-col w-1/2">
					<label for="r-nummer"
						>R-Nummer <span
							class={errorVeldenStaal.laborantRnummer ? 'text-red-500 inline-block' : 'hidden'}
							>moet in format rXXXXXXX met 7 cijfers</span
						></label
					>
					<input
						type="text"
						id="r-nummer"
						name="r-nummer"
						bind:value={laborantRnummer}
						class="rounded-lg text-black bg-gray-200 h-12 pl-3 {errorVeldenStaal.laborantRnummer
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
			</div>
			<button type="button" on:click={setLaborant}>
				<button
					class="bg-blue-500 text-xl rounded-lg p-3 text-white h-12 w-32 justify-center items-center flex"
					>Start</button
				>
			</button>
		</ContentWithoutClose>
		<AutoTrigger></AutoTrigger>
	</Modal>
{/if}

<!-- navbar -->
<Nav />

<!-- pagina content -->
<div class="px-8">
	<div class="bg-slate-200 w-full h-full rounded-2xl p-5">
		<h1 class="font-bold text-xl mb-2">Patiëntgegevens</h1>
		{#if errorMessageStaal}
			<div class="text-red-500 mb-2">{errorMessageStaal}</div>
		{/if}
		<div class="flex flex-row space-x-4">
			<!-- Invullen patientgegevens -->
			<div class="grid grid-cols-5 bg-white rounded-lg h-20 w-5/6 space-x-2 px-2">
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Code</p>
					<p class="font-bold">{nieuweStaalCode || ''}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Achternaam</p>
					<input
						type="text"
						id="naam"
						name="naam"
						bind:value={naam}
						class="rounded-lg text-black bg-gray-200 h-10 pl-3 {errorVeldenStaal.naam
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Voornaam</p>
					<input
						type="text"
						id="voornaam"
						name="voornaam"
						bind:value={voornaam}
						class="rounded-lg text-black bg-gray-200 h-10 pl-3 {errorVeldenStaal.voornaam
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Geboortedatum</p>
					<input
						type="date"
						id="geboortedatum"
						name="geboortedatum"
						bind:value={geboortedatum}
						class="rounded-lg text-black bg-gray-200 h-10 pl-3 px-3 {errorVeldenStaal.geboortedatum
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="flex flex-col justify-center pl-5">
					<!-- https://svelte.dev/repl/2b143322f242467fbf2b230baccc0484?version=3.23.2 -->
					<p class="text-gray-400">Geslacht</p>
					<div>
						<label
							class="container mr-5 {errorVeldenStaal.geslacht ? 'text-red-500 font-bold' : ''}"
						>
							<input type="radio" name="radio" bind:group={geslacht} value="M" />
							Man
						</label>
						<label class="container {errorVeldenStaal.geslacht ? 'text-red-500 font-bold' : ''}">
							<input type="radio" name="radio" bind:group={geslacht} value="V" />
							Vrouw
						</label>
					</div>
				</div>
			</div>

			<!-- navigatie volgende en terug -->
			<div class="pb-5 flex flex-row space-x-2 justify-end w-3/12">
				<ButtonTerug height="5rem" width="10rem" />
				<ButtonVerder height="5rem" width="10rem" on:click={postOrPut} />
			</div>
		</div>

		<div class="rounded-xl bg-white">
			<div class="bg-white rounded-xl p-3 flex flex-row items-center place-content-between">
				<div class="w-1/4 flex items-center">
					<!-- zoeken op code -->
					<input
						type="text"
						id="searchCode"
						name="searchCode"
						placeholder="zoeken op code"
						bind:value={searchCode}
						on:input={filterTests}
						class="h-12 rounded-l-lg text-black pl-3 bg-gray-200 w-full"
					/>
					<button
						on:click={verwijderZoek}
						class="w-12 h-12 p-4 flex items-center justify-center bg-red-200 rounded-r-lg"
					>
						<GoX />
					</button>
				</div>

				<button
					type="button"
					on:click={verwijderSelectie}
					class="bg-red-500 h-12 w-36 rounded-lg text-white mx-6">Verwijder selectie</button
				>

				<div class="flex items-center w-1/3">
					<!-- dynamisch tonen hoeveel geselecteerde tests -->
					<p class=" text-blue-600">
						<span>{geselecteerdeTests.length}</span> geselecteerd
					</p>
				</div>

				<!-- knoppen en modals voor aanmaken cat & test -->
				<div class="flex flex-row justify-end space-x-2 w-1/3">
					{#if rol === 'admin'}
						<Modal>
							<Content>
								<h1 class="font-bold text-xl mb-4">Categorie Aanmaken</h1>
								{#if errorMessageCategorie}
									<div class="text-red-500 mb-2">{errorMessageCategorie}</div>
								{/if}
								<div class="w-200 flex flex-row place-content-between">
									<div class="flex flex-col w-3/5 place-content-between">
										<div class="flex flex-col">
											<label for="categorienaam">Naam</label>
											<input
												type="text"
												id="categorienaam"
												name="categorienaam"
												bind:value={categorienaam}
												class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                        {errorVeldenCategorie.categorienaam
													? 'border-2 border-red-500'
													: ''}"
											/>
										</div>
										<div>
											<button
												on:click={nieuweCategorie}
												type="button"
												class="bg-green-500 rounded-lg p-3 mb-3 text-black h-12 flex flex-row items-center justify-center flex-grow w-56 font-bold text-lg"
												>Opslaan
												<div class="w-5 h-5 ml-5"><IoMdCheckmarkCircle /></div>
											</button>
										</div>
									</div>
									<div class="flex flex-col">
										<p class="pl-2 {errorVeldenCategorie.kleur ? 'text-red-500 font-bold' : ''}">
											Kleur
										</p>
										<div>
											<ColorPicker
												bind:hex
												components={ChromeVariant}
												sliderDirection="horizontal"
												isDialog={false}
											/>
										</div>
									</div>
								</div>
							</Content>

							<Trigger>
								<button
									class="bg-gray-200 rounded-lg p-3 text-black h-12 flex flex-row items-center justify-center flex-grow"
								>
									<div class="w-3 h-3 mr-2"><FaPlus /></div>
									Categorie aanmaken
								</button>
							</Trigger>
						</Modal>

						<Modal>
							<Content>
								<h1 class="font-bold text-xl mb-4">Test Aanmaken</h1>
								{#if errorMessageTest}
									<div class="text-red-500 mb-2">{errorMessageTest}</div>
								{/if}
								<div class="flex flex-row space-x-4">
									<div class="flex flex-col w-1/2">
										<label for="testCode">Testcode</label>
										<input
											type="text"
											id="testCode"
											name="testCode"
											bind:value={testCode}
											class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                    {errorVeldenTest.testCode ? 'border-2 border-red-500' : ''}"
										/>
									</div>
									<div class="flex flex-col w-1/2">
										<label for="testNaam">Eenheid</label>
										<select
											id="eenheid"
											name="eenheid"
											bind:value={eenheid}
											class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                    {errorVeldenTest.eenheid ? 'border-2 border-red-500' : ''}"
										>
											<option value="" disabled>Selecteer een eenheid</option>
											{#each eenheden as eenheid}
												<option value={eenheid.id}>{eenheid.naam} ({eenheid.afkorting})</option>
											{/each}
										</select>
									</div>
								</div>
								<div class="flex flex-row space-x-4 my-4">
									<div class="flex flex-col w-1/2">
										<label for="naam">Naam</label>
										<input
											type="text"
											id="naam"
											name="naam"
											bind:value={testNaam}
											class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                    {errorVeldenTest.testNaam ? 'border-2 border-red-500' : ''}"
										/>
									</div>
									<!-- https://svelte.dev/repl/16778e290bf548f790dc45d249bed94d?version=3.46.4  -->
									<div class="flex flex-col w-1/2">
										<label for="testcategorie">Categorie</label>
										<select
											id="testcategorie"
											name="testcategorie"
											bind:value={testcategorie}
											class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                    {errorVeldenTest.testcategorie
												? 'border-2 border-red-500'
												: ''}"
										>
											<option value="" disabled>Selecteer een categorie</option>
											{#each testcategorieën as categorie}
												<option value={categorie.id}>{categorie.naam}</option>
											{/each}
										</select>
									</div>
								</div>

								<button
									on:click={nieuweTest}
									type="button"
									class="bg-green-500 rounded-lg p-3 text-black h-12 flex flex-row items-center justify-center flex-grow w-56 font-bold text-lg"
									>Opslaan
									<div class="w-5 h-5 ml-5"><IoMdCheckmarkCircle /></div>
								</button>
							</Content>

							<Trigger>
								<button
									on:click={async () => {
										const fetchedTestcategorieën = await loadTestCategorieën();
										const fetchedEenheden = await loadEenheden();

										if (fetchedTestcategorieën) testcategorieën = fetchedTestcategorieën;
										if (fetchedEenheden) eenheden = fetchedEenheden;
									}}
									class="bg-gray-200 rounded-lg p-3 text-black h-12 flex flex-row items-center justify-center flex-grow"
								>
									<div class="w-3 h-3 mr-2"><FaPlus /></div>
									Test aanmaken
								</button>
							</Trigger>
						</Modal>
					{/if}
				</div>
			</div>

			<!-- tabel met alle tests -->
			{#each testsSorted as test, index}
				<div
					class="grid grid-cols-12 gap-4 h-16 items-center px-3 border-b border-gray-300 {isNaN(
						parseInt(test?.testCode)
					)
						? 'bg-blue-50'
						: ''}"
				>
					<div class="col-span-1">
						<!-- Checkbox for selecting tests -->
						<input
							type="checkbox"
							on:change={() => toggleTestSelectie(test.testCode)}
							checked={geselecteerdeTests.includes(test.testCode)}
							class="w-5 h-5 mt-2 appearance-none border-2 border-gray-300 rounded-md checked:bg-blue-600 checked:border-transparent focus:outline-none"
						/>
					</div>
					<div class="col-span-2">
						<p class="text-gray-400">Testcode</p>
						<p>{test?.testCode || ''}</p>
					</div>
					<div class="col-span-4">
						<p class="text-gray-400">Naam</p>
						<p class="truncate">{test?.naam || ''}</p>
					</div>
					<div class="col-span-2">
						{#if !isNaN(parseInt(test?.testCode))}
							<p class="text-gray-400">Categorie</p>
							<p class="font-bold" style="color:{test?.testcategorie.kleur}">
								{test?.testcategorie.naam || ''}
							</p>
						{/if}
					</div>

					<div class="col-span-2">
						{#if !isNaN(parseInt(test?.testCode))}
							<p class="text-gray-400">Eenheid</p>
							<p class="truncate">
								{test?.eenheid.afkorting || ''}{#if test?.eenheid.naam !== 'not available'}: {test
										?.eenheid.naam || ''}{/if}
							</p>
						{/if}
					</div>

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
											openModalTestId = test.id;

											const fetchedTestcategorieën = await loadTestCategorieën();
											const fetchedEenheden = await loadEenheden();
											if (fetchedTestcategorieën) testcategorieën = fetchedTestcategorieën;
											if (fetchedEenheden) eenheden = fetchedEenheden;
										}}
									>
										<FaRegEdit />
									</button>
								</Trigger>
								{#if openModalTestId === test.id}
									<Content>
										<h1 class="font-bold text-xl mb-4">Test Aanpassen - {test.naam}</h1>
										{#if editTestErrorMessage}
											<div class="text-red-500 mb-2">{editTestErrorMessage}</div>
										{/if}
										<div class="flex flex-row space-x-4">
											<div class="flex flex-col w-1/2">
												<label for="testCode-{test.id}">Testcode</label>
												<input
													type="text"
													id="testCode-{test.id}"
													name="testCode"
													bind:value={test.testCode}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                {editTestError.testCode
														? 'border-2 border-red-500'
														: ''}"
												/>
											</div>
											<div class="flex flex-col w-1/2">
												<label for="eenheid-{test.id}">Eenheid</label>
												<select
													id="eenheid-{test.id}"
													name="eenheid"
													bind:value={test.eenheid.id}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                {editTestError.eenheid
														? 'border-2 border-red-500'
														: ''}"
												>
													<option value="" disabled>Selecteer een eenheid</option>
													{#each eenheden as eenheid}
														<option value={eenheid.id}>{eenheid.naam} ({eenheid.afkorting})</option>
													{/each}
												</select>
											</div>
										</div>
										<div class="flex flex-row space-x-4 my-4">
											<div class="flex flex-col w-1/2">
												<label for="testNaam-{test.id}">Naam</label>
												<input
													type="text"
													id="testNaam-{test.id}"
													name="testNaam"
													bind:value={test.naam}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                {editTestError.naam
														? 'border-2 border-red-500'
														: ''}"
												/>
											</div>
											<div class="flex flex-col w-1/2">
												<label for="testcategorie-{test.id}">Categorie</label>
												<select
													id="testcategorie-{test.id}"
													name="testcategorie"
													bind:value={test.testcategorie.id}
													class="rounded-lg text-black bg-gray-200 h-12 pl-3
                                                {editTestError.testcategorie
														? 'border-2 border-red-500'
														: ''}"
												>
													<option value="" disabled>Selecteer een categorie</option>
													{#each testcategorieën as categorie}
														<option value={categorie.id}>{categorie.naam}</option>
													{/each}
												</select>
											</div>
										</div>

										<button
											type="button"
											class="bg-green-500 rounded-lg p-3 text-black h-12 flex flex-row items-center justify-center flex-grow w-56 font-bold text-lg"
											on:click={async () => await editTest(test)}
										>
											Opslaan
											<div class="w-5 h-5 ml-5"><IoMdCheckmarkCircle /></div>
										</button>
									</Content>
								{/if}
							</Modal>

							<!-- Delete button -->
							{#if test.confirmDelete}
								<button
									type="button"
									on:click={() => deleteTest(test?.id)}
									class="h-10 w-10 bg-red-500 p-2 rounded-lg text-white"
								>
									<FaTrashAlt />
								</button>
							{:else}
								<button
									type="button"
									on:click={() => {
										testsSorted.forEach((t, i) => {
											if (i !== index) t.confirmDelete = false;
										});
										test.confirmDelete = true;
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
	</div>
</div>
