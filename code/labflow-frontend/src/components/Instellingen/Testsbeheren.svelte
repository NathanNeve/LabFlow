<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { fetchTests } from '$lib/fetchFunctions';
	import { fetchTestcategorieën } from '$lib/fetchFunctions';
	import { fetchEenheden } from '$lib/fetchFunctions';
	import { fetchReferentiewaarden } from '$lib/fetchFunctions';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';
	// @ts-ignore
	import GoLink from 'svelte-icons/go/GoLink.svelte';
	// @ts-ignore
	import FaSave from 'svelte-icons/fa/FaSave.svelte';
	import { getCookie } from '$lib/globalFunctions';
	import Modal from './modalReferentiewaarden/Modal.svelte';
	import { writable, get } from 'svelte/store';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;
	// types
	import type { Eenheid, Referentiewaarde, Test, TestCategorie } from '$lib/types/dbTypes';

	let showModal = writable(false);

	let token: string = '';

	let tests: Test[] = [];
	let testsSorted: Test[] = [];
	let searchCode = '';
	let testcategorieën: TestCategorie[] = [];
	let eenheden: Eenheid[] = [];
	let referentiewaardes: Referentiewaarde[] = [];
	let waarden: Referentiewaarde[] = [{ id: 9999999, waarde: 'dummy', label: 'dummy' }]; // dummy zorgt ervoor dat bij het laden van de pagina de multiselect niet leeg is (geeft anders error in console)

	// volgorde is belangrijk, eerst eenheden en categorieën ophalen, daarna tests
	onMount(async () => {
		token = getCookie('authToken') || '';
		const fetchedEenheden = await fetchEenheden();
		if (fetchedEenheden) {
			eenheden = fetchedEenheden;
		}
		const fetchedTestcategorieën = await fetchTestcategorieën();
		if (fetchedTestcategorieën) {
			testcategorieën = fetchedTestcategorieën;
		}
		const fetchedTests = await fetchTests();
		if (fetchedTests) {
			[tests, testsSorted] = [fetchedTests, fetchedTests];
		}
		const fetchedReferentiewaardes = await fetchReferentiewaarden();
		// mappen van referentiewaarden overeenkomstig de waarden die in de multiselect moeten komen
		if (fetchedReferentiewaardes) {
			referentiewaardes = fetchedReferentiewaardes;
			// we willen geen dubbele referentiewaarden, dus we gebruiken een Map om unieke waarden te bewaren
			const uniqueWaardenMap = new Map();				
			for (const item of referentiewaardes) {
				if (!uniqueWaardenMap.has(item.waarde)) {
					uniqueWaardenMap.set(item.waarde, {
						id: item.id,
						label: item.waarde,
						waarde: item.waarde
					});
				}
			}
			waarden = Array.from(uniqueWaardenMap.values());
		}
	});

	function filterTests() {
		testsSorted = tests.filter((test) => {
			const codeMatch =
				test.naam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				test.testCode.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				test.testcategorie.naam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				test.eenheid.afkorting.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				test.eenheid.naam.toString().toLowerCase().includes(searchCode.toLowerCase());
			return codeMatch;
		});
	}

	function verwijderZoek() {
		searchCode = '';
		testsSorted = tests;
	}

	///// DELETE test /////
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
		const result = await fetchTests();
		if (result) {
			[tests, testsSorted] = [result, result];
		}
	}

	///// POST test /////
	let testCode = '';
	let naam = '';
	let eenheid = '';
	let testcategorie = '';
	let referentiewaardesPOST = writable([]);

	let errorVeldenTestPOST = {
		testCode: false,
		naam: false,
		eenheid: false,
		testcategorie: false
	};

	let errorMessageTestPOST = '';
	async function nieuweTest() {
		// Resetten van de errorvelden
		errorVeldenTestPOST = {
			testCode: false,
			naam: false,
			eenheid: false,
			testcategorie: false
		};
		let isValid = true;
		if (!testCode) {
			errorVeldenTestPOST.testCode = true;
			isValid = false;
		}
		if (!naam) {
			errorVeldenTestPOST.naam = true;
			isValid = false;
		}
		if (!eenheid) {
			errorVeldenTestPOST.eenheid = true;
			isValid = false;
		}
		if (!testcategorie) {
			errorVeldenTestPOST.testcategorie = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessageTestPOST = 'Vul alle verplichte velden in.';
			return;
		}

		// Mappen van de referentiewaardes naar een array van objecten (get de geselecteerde waarden uit de store en map ze)
		interface Referentiewaarde {
			id: number;
			label: string;
			waarde: string;
		}

		const referentiewaardesPOSTMapped = get(referentiewaardesPOST).map(
			(value: Referentiewaarde) => ({
				waarde: value.waarde // Alleen waarde is nodig voor onze POST
			})
		);

		try {
			const response = await fetch(`${backend_path}/api/createtest`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					testCode: testCode,
					naam: naam,
					eenheid: {
						id: eenheid
					},
					testcategorie: {
						id: testcategorie
					},
					referentiewaardes: referentiewaardesPOSTMapped
				})
			});
			testCode = '';
			naam = '';
			eenheid = '';
			testcategorie = '';
			referentiewaardesPOST = writable([]);
			errorMessageTestPOST = '';
			const result = await fetchTests();
			if (result) {
				[tests, testsSorted] = [result, result];
			}
			if (response.status === 409) {
				errorMessageTestPOST = 'Testcode bestaat al.';
			}
		} catch (error) {
			console.error('Test kon niet worden aangemaakt: ', error);
		}
		return;
	}

	// helper functie openen juiste modal PUT
	let selectedTestIdPUT: number = 0;
	let showModalPUT = writable(false);

	function setModalPUT(testId: number) {
		selectedTestIdPUT = testId;
		showModalPUT.set(true);
	}

	// helper functie sluiten juiste modal PUT
	function closeModalPUT() {
		// updaten van test
		selectedTestIdPUT = 0;
		showModalPUT.set(false);
	}

	///// PUT test /////
	let errorVeldenTestPUT = {
		testCode: false,
		naam: false,
		eenheid: false,
		testcategorie: false,
		referentiewaardes: false
	};

	let referentiewaardesPUT = writable([]);
	let errorMessageStaalPUT = '';

	async function updateTest(id: number) {
		const test = tests.find((t) => t.id === id);
		if (!test) return;

		let isValid = true;

		errorVeldenTestPUT = {
			testCode: false,
			naam: false,
			eenheid: false,
			testcategorie: false,
			referentiewaardes: false
		};

		if (!test.testCode) {
			errorVeldenTestPUT.testCode = true;
			isValid = false;
		}
		if (!test.naam) {
			errorVeldenTestPUT.naam = true;
		}
		if (!test.eenheid) {
			errorVeldenTestPUT.eenheid = true;
			isValid = false;
		}
		if (!test.testcategorie) {
			errorVeldenTestPUT.testcategorie = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessageStaalPUT = 'Vul alle verplichte velden in.';
			return;
		}

		const referentiewaardesPUTMapped = get(referentiewaardesPUT).map(
			(value: { waarde: string }) => ({
				waarde: value.waarde // waarde extraheren uit referentiewaardesPUT store en mappen naar een array van objecten
			})
		);

		try {
			await fetch(`${backend_path}/api/updatetest/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					id: test.id,
					testCode: test.testCode,
					naam: test.naam,
					eenheid: { id: test.eenheid.id },
					testcategorie: { id: test.testcategorie.id },
					referentiewaardes: referentiewaardesPUTMapped
				})
			});
		} catch (error) {
			console.error('Error during PUT request:', error);
		}
		return;
	}
</script>

<div class="flex flex-col w-full ml-5">
	<div class="flex flex-row justify-between w-full h-14 mb-5">
		<h1 class="font-bold text-3xl">Testen beheren</h1>
		<button
			type="button"
			on:click={async () => {
				await goto('/stalen');
			}}
			class="bg-gray-400 text-xl rounded-lg p-3 text-white h-12 w-32 justify-center items-center flex"
		>
			<div class="w-4 h-4 mr-2"><FaArrowLeft /></div>
			<p>Terug</p>
		</button>
	</div>

	<div class="bg-slate-200 w-full h-full rounded-2xl p-5">
		<div class="flex mb-5 w-full">
			<input
				type="text"
				id="searchCode"
				name="searchCode"
				placeholder="zoeken"
				bind:value={searchCode}
				on:input={filterTests}
				class=" h-12 rounded-l-lg text-black pl-3 w-2/5"
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
					<p>Testcode</p>
				</div>
				<div class="col-span-2 text-left">
					<p>Naam</p>
				</div>
				<div class="col-span-2 text-left">
					<p>Categorie</p>
				</div>
				<div class="col-span-2 text-left">
					<p>Eenheid</p>
				</div>
				<div class="col-span-1 text-left">
					<p>referentiewaardes</p>
				</div>
				<div class="col-span-1 text-right">
					<p>Acties</p>
				</div>
			</div>

			<!-- POST testen -->
			{#if errorMessageTestPOST}
				<div class="text-red-500 mb-2">{errorMessageTestPOST}</div>
			{/if}

			<div class="grid grid-cols-9 space-x-3 bg-white rounded-lg h-20 items-center px-3 shadow-md">
				<div class="col-span-1">
					<input
						type="text"
						id="testCode"
						bind:value={testCode}
						placeholder="Testcode"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenTestPOST.testCode ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-2">
					<input
						type="text"
						id="patientAchternaam"
						bind:value={naam}
						placeholder="Naam van de test"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenTestPOST.naam ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-2">
					<select
						bind:value={testcategorie}
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenTestPOST.testcategorie
							? 'border-2 border-red-500'
							: ''}"
					>
						<option value="" disabled selected hidden>Categorie</option>

						{#each testcategorieën as categorie}
							<option value={categorie.id}>{categorie.naam}</option>
						{/each}
					</select>
				</div>
				<div class="col-span-2">
					<select
						bind:value={eenheid}
						placeholder="Eenheid"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenTestPOST.eenheid
							? 'border-2 border-red-500'
							: ''}"
					>
						<option value="" disabled selected hidden>Eenheid</option>

						{#each eenheden as eenheid}
							<option value={eenheid.id}>{eenheid.afkorting}: {eenheid.naam}</option>
						{/each}
					</select>
				</div>

				<!-- Referentiewaardes linken POST -->
				<button
					on:click={() => showModal.set(true)}
					class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"><GoLink /></button
				>

				<Modal bind:showModal={$showModal} {waarden} bind:selectedValues={referentiewaardesPOST} />

				<!-- Acties -->
				<div class="col-span-1 flex justify-end">
					<button
						type="button"
						class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"
						on:click={nieuweTest}
						aria-label="Nieuwe categorie toevoegen"
					>
						<FaPlus />
					</button>
				</div>
			</div>
			<div class="space-y-3">
				{#each testsSorted as test, index}
					<div
						class="grid grid-cols-9 bg-white rounded-lg h-20 items-center px-3 shadow-md space-x-3"
					>
						<div class="col-span-1">
							<input
								type="text"
								on:blur={() => updateTest(test.id)}
								id="test-{test?.id}"
								bind:value={test.testCode}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>
						<div class="col-span-2">
							<input
								type="text"
								on:blur={() => updateTest(test.id)}
								id="test-{test?.id}"
								bind:value={test.naam}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>
						<div class="col-span-2">
							<select
								on:blur={() => updateTest(test.id)}
								bind:value={test.testcategorie.id}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							>
								{#each testcategorieën as categorie}
									<option value={categorie.id}>{categorie.naam}</option>
								{/each}
							</select>
						</div>
						<div class="col-span-2">
							<select
								bind:value={test.eenheid.id}
								class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
							>
								{#each eenheden as eenheid}
									<option value={eenheid.id}>{eenheid.afkorting}: {eenheid.naam}</option>
								{/each}
							</select>
						</div>
						<button
							on:click={() => setModalPUT(test.id)}
							class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"><GoLink /></button
						>
						{#if $showModalPUT && selectedTestIdPUT === test.id}
							<Modal
								bind:showModal={$showModalPUT}
								on:close={() => {
									closeModalPUT();
								}}
								{waarden}
								bind:selectedValues={referentiewaardesPUT}
							/>
						{/if}

						<!-- Acties -->
						<div class="col-span-1 flex justify-end">
							<button
								type="button"
								class="h-10 w-10 p-2 rounded-lg text-white mr-2 bg-green-500 hover:bg-green-700 transition duration-500"
								on:click={() => updateTest(test.id)}
								aria-label="Save test"
							>
								<FaSave />
							</button>
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
										tests.forEach((c, i) => {
											if (i !== index) c.confirmDelete = false;
										});
										test.confirmDelete = true;
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
