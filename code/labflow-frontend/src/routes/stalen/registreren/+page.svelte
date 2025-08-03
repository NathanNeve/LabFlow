<script lang="ts">
	import Nav from '../../../components/nav.svelte';
	import { goto } from '$app/navigation';
	import { formatDate, formatSex, generalFetch } from '$lib/globalFunctions';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import FaArrowRight from 'svelte-icons/fa/FaArrowRight.svelte';
	// @ts-ignore
	import FaCheck from 'svelte-icons/fa/FaCheck.svelte';
	// @ts-ignore
	import IoIosClose from 'svelte-icons/io/IoIosClose.svelte';
	// @ts-ignore
	import IoMdText from 'svelte-icons/io/IoMdText.svelte';
	// @ts-ignore
	import FaCloudDownloadAlt from 'svelte-icons/fa/FaCloudDownloadAlt.svelte';

	import { staalCodeStore } from '$lib/store';
	import { onMount } from 'svelte';
	import { slide } from 'svelte/transition';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	// neem de id
	let sampleCode: string | undefined;

	let tests: any[] = [];
	let openNoteId: string | null = null;
	let staal: any = {};
	let staalId: string = '';
	let testCategories: any[] = [];
	let selectedCategory: any = {};
	let downloaded: boolean = false;

	// update variables
	let updateValue: string = '';
	let updateNote: string = '';
	let status: boolean = false;
	let allDone: boolean = false;

	// alle tests categorieën ophalen die bij de testen horen
	async function loadData() {
		try {
			staal = await generalFetch('GET', 'staal', true, sampleCode);
			// assign id to staalId
			staalId = staal.id;
			// Extract unique test categories
			extractUniqueTestCategories(staal.registeredTests);
			// put all tests assigned to a 'staal' in the tests array
			tests = staal.registeredTests;

			// check completion status after loading data
			checkAllDoneForCategory(selectedCategory.id);
			checkAllTestsDone();
		} catch (error) {
			console.error('data kon niet gefetched worden:', error);
		}
	}

	// Get unique test categories based on their id
	function extractUniqueTestCategories(registeredTests: any[]) {
		const categoryMap = new Map();

		registeredTests.forEach((testItem) => {
			const category = testItem.test.testcategorie;
			if (!categoryMap.has(category.id)) {
				categoryMap.set(category.id, category);
			}
		});

		// Convert Map to an array of unique categories
		testCategories = Array.from(categoryMap.values());
		if (selectedCategory.id == undefined) {
			selectedCategory = testCategories[0];
		}
	}

	function toggleNoteInput(testId: string) {
		openNoteId = openNoteId == testId ? null : testId;
	}

	function setCategory(id: string) {
		selectedCategory = testCategories.find((category) => category.id == id);
	}

	// update function voor resultaat
	async function updateResult(value: string, testId: string, note: string) {
		updateValue = value;
		try {
			let body;

			if (status != true) {
				body = {
					result: updateValue,
					note: note,
					failed: tests.find((test) => test.test.id == testId).failed
				};
			} else {
				body = {
					result: '',
					note: note,
					failed: tests.find((test) => test.test.id == testId).failed
				};
			}

			/*const response = await gen(`${backend_path}/api/updatestaaltest/${staalId}/${testId}`, {
				method: 'PUT',
				headers: headers,
				body: JSON.stringify(body)
			});
			if (!response.ok) {
				throw new Error(`Error: ${response.statusText}`);
			}
			const data = await response;
			loadData();
			return data; */

			const response = await generalFetch(
				'PUT',
				`updatestaaltest/${staalId}/${testId}`,
				true,
				undefined,
				body
			);
			if (!response.ok) {
				throw new Error(`Error: ${response.statusText}`);
			}
			const data = await response.json();
			loadData();
			return data;
		} catch (error) {
			console.error('update error: ', error);
			loadData();
			throw error;
		}
	}

	// update function voor de note
	async function updateNoteValue(value: string, testId: string, result: string) {
		updateNote = value;
		try {
			const body = {
				result: result,
				note: updateNote,
				failed: tests.find((test) => test.test.id == testId).failed
			};

			const response = await generalFetch(
				'PUT',
				`updatestaaltest/${staalId}/${testId}`,
				true,
				undefined,
				body
			);

			if (!response.ok) {
				throw new Error(`Error: ${response.statusText}`);
			}

			const data = await response.json();
			loadData();
			return data;
		} catch (error) {
			console.error('update error: ', error);
			throw error;
		}
	}

	async function handleCheckboxChange(test: any) {
		// Toggle the failed status
		const isChecked = !test.failed;
		test.failed = isChecked;

		// If failed, disable the input and clear the result, else enable the input
		if (isChecked) {
			test.result = null; // Set result to null when checked
		} else {
			test.result = ''; // Set result to empty string when unchecked
		}

		// Update the result and failed status in the database
		try {
			const body = {
				result: test.result, // null if failed, otherwise empty string or input value
				note: test.note || '', // Keep the note as it is or empty string
				failed: test.failed // Update the failed status
			};

			const response = await generalFetch(
				'PUT',
				`updatestaaltest/${staalId}/${test.test.id}`,
				true,
				undefined,
				body
			);

			if (!response.ok) {
				throw new Error(`Error: ${response.statusText}`);
			}

			const data = await response.json();
			loadData();
			return data;
		} catch (error) {
			loadData();
			console.error('Failed to update test status:', error);
		}
	}

	function checkAllDoneForCategory(testcategorie: any): boolean {
		// for each test with the same testcategory as the parameter testcategorie, check if all tests are done
		const testsForCategory = tests.filter((test) => test.test.testcategorie.id == testcategorie.id);
		return testsForCategory.every((test) => test.result || test.failed);
	}

	function checkAllTestsDone() {
		allDone = allDone = tests.every((test) => test.result || test.failed);
	}

	onMount(() => {
		staalCodeStore.subscribe((value) => {
			sampleCode = value;
		});
		loadData().then(() => checkAllTestsDone());
	});

	// pdf downloaden
	async function getPdf(staalId: string) {
		try {
			const response = await fetch(`${backend_path}/api/pdf/generateresults/${staalId}`, {
				method: 'GET',
				credentials: 'include'
			});

			if (!response.ok) {
				console.error('Failed to fetch PDF:', response.statusText);
				return;
			}

			// Extract the filename from the Content-Disposition header
			const disposition = response.headers.get('X-Filename');
			let filename = `Resultaten_${staal?.patientAchternaam}_${staal?.patientVoornaam}`; // Default als de header er niet is van de backend

			if (disposition && disposition.includes('filename=')) {
				const match = disposition.match(/filename="(.+?)"/);
				if (match && match[1]) {
					filename = match[1];
				}
			}

			// Convert response to Blob and download it
			const blob = await response.blob();
			const url = window.URL.createObjectURL(blob);

			// Create a download link
			const a = document.createElement('a');
			a.href = url;
			a.download = filename; // Use the extracted filename
			document.body.appendChild(a);
			a.click();
			a.remove();

			// Clean up the Blob URL
			window.URL.revokeObjectURL(url);

			// set download variable to true
			downloaded = true;
		} catch (error) {
			console.error('Error while downloading PDF:', error);
		}
	}
</script>

<Nav />
<main class="px-8">
	<div class="bg-gray-200 rounded-2xl h-full p-4">
		<!-- top section -->
		<div class="flex flex-row space-x-4">
			<!-- Invullen patientgegevens -->
			<div class="grid grid-cols-5 bg-white rounded-lg h-20 w-5/6 space-x-2 px-2">
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Code</p>
					<p class="font-bold">{sampleCode || 'loading...'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Achternaam</p>
					<p class="font-bold">{staal?.patientAchternaam || 'loading...'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Voornaam</p>
					<p class="font-bold">{staal?.patientVoornaam || 'loading...'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Geboortedatum</p>
					<p class="font-bold">
						{staal.patientGeboorteDatum ? formatDate(staal.patientGeboorteDatum) : 'loading...'}
					</p>
				</div>
				<div class="flex flex-col justify-center pl-5">
					<p class="text-gray-400">Geslacht</p>
					<p class="font-bold">
						{staal.patientGeslacht ? formatSex(staal.patientGeslacht) : 'loading...'}
					</p>
				</div>
			</div>

			<!-- navigatie volgende en terug -->
			<div class="pb-5 flex flex-row space-x-2 justify-end w-3/12">
				<button
					on:click={() => {
						goto('/stalen');
					}}
					class="bg-gray-400 text-xl rounded-lg p-3 text-white h-20 w-1/2 flex flex-row items-center justify-center"
				>
					<div class="w-5 h-5 mr-2"><FaArrowLeft /></div>
					Home
				</button>
				<!-- staat tijdelijk naar volgende pagina omdat ik nog niet weet hoe César zijn pagina heet -->
				<button
					on:click={() => {
						if (downloaded) {
							goto('/stalen/done');
						} else {
							alert('Download eerst de resultaten');
						}
					}}
					class="bg-blue-600 text-xl rounded-lg p-3 text-white h-20 w-1/2 flex flex-row items-center justify-center disabled:bg-gray-300 disabled:cursor-not-allowed"
					disabled={!allDone}
				>
					Registreren
					<div class="w-5 h-5 ml-2"><FaArrowRight /></div>
				</button>
			</div>
		</div>
		<!-- bottom section -->
		<div class="flex space-x-4 h-full">
			<!-- left section -->
			<div class="w-1/3 h-[75vh] flex flex-col space-y-4">
				<div class="bg-white h-full rounded-xl p-4">
					<p class="text-blue-500">{testCategories.length} labels</p>
					{#each testCategories as testcategorie}
						<button
							on:click={() => setCategory(testcategorie.id)}
							class="border border-gray-200 rounded-xl w-full flex justify-between items-center p-4 my-3 hover:bg-gray-100 hover:scale-[101%] transition cursor-pointer"
						>
							<div class="flex justify-start items-center">
								<div
									class="px-1 py-6 rounded-full"
									style={`background-color: ${testcategorie.kleur || '#000'};`}
								></div>
								{#if testcategorie?.id == 7}
									<p class="font-bold text-lg ml-3">{testcategorie ? 'Notitie' : 'loading...'}</p>
								{:else}
									<p class="font-bold text-lg ml-3">{testcategorie?.naam || 'loading...'}</p>
								{/if}
							</div>
							{#if checkAllDoneForCategory(testcategorie)}
								<div class={`p-3 rounded-full text-white h-12`} style="background-color: #23E22C;">
									<FaCheck />
								</div>
							{:else}
								<div class={`rounded-full text-white h-12`} style="background-color: #E3E3E3;">
									<IoIosClose />
								</div>
							{/if}
						</button>
					{/each}
				</div>
				<div>
					<button
						on:click={() => getPdf(staalId)}
						class="bg-blue-600 text-xl rounded-lg p-3 text-white h-20 w-full flex items-center justify-center disabled:bg-gray-300 disabled:cursor-not-allowed"
						disabled={!allDone}
					>
						<div class="h-6 px-4">
							<FaCloudDownloadAlt />
						</div>
						<p class="font-bold">Download Resultaten</p>
					</button>
				</div>
			</div>

			<!-- right section -->
			<div class="w-2/3 flex flex-col justify-between space-y-4">
				<!-- white box -->
				<div class="w-full h-full bg-white rounded-xl">
					<!-- header-->
					<div class="h-1/6 flex justify-between mx-6">
						<!-- left -->
						<div class="flex justify-start items-center">
							<div
								class={`p-8 rounded-full text-white`}
								style={`background-color: ${selectedCategory?.kleur || '#000'};`}
							></div>
							{#if selectedCategory?.id == 7}
								<h2 class="ml-8 font-bold text-lg">
									{selectedCategory ? 'Notitie' : 'loading...'}
								</h2>
							{:else}
								<h2 class="ml-8 font-bold text-lg">{selectedCategory?.naam || 'loading...'}</h2>
							{/if}
						</div>
					</div>
					<!-- List-->
					<div class="h-5/6 mx-6">
						{#each tests.filter((test) => test.test.testcategorie.id == selectedCategory.id) as test}
							{#if selectedCategory?.id == 7}
								<div class="w-full bg-white border border-gray-200 rounded-xl my-4 p-4">
									<div class="grid grid-cols-[1fr_3fr_auto_1fr] items-center">
										<!-- Test Section -->
										<div class="flex flex-col items-start col-span-2">
											<span class="text-sm text-gray-500">Test</span>
											<span class="text-xl font-semibold"
												>{test.test.naam.length > 20
													? test.test.naam.slice(0, 20) + '...'
													: test.test.naam}</span
											>
										</div>

										<!-- Value Section -->
										<div class="flex flex-col items-start">
											<span class="text-sm text-gray-500">Titel</span>
											<input
												on:blur={() => updateResult(test.result, test.test.id, test.note)}
												bind:value={test.result}
												type="text"
												class="bg-gray-200 h-10 w-full rounded-lg border border-gray-400 px-1 disabled:border-0 disabled:bg-gray-300 disabled:cursor-not-allowed"
												placeholder={test.result}
												disabled={test.failed}
											/>
										</div>

										<!-- Note Section -->
										<div class="flex flex-col items-center">
											<span class="text-sm text-gray-500">Nota</span>
											<button
												on:click={() => toggleNoteInput(test.test.id)}
												class="text-white bg-blue-500 h-12 p-3 rounded-lg"
											>
												<IoMdText />
											</button>
										</div>
									</div>
									{#if openNoteId == test.test.id}
										<div>
											<div transition:slide class="mt-4 p-4">
												<span class="text-sm text-gray-500">Nota</span>
												<input
													type="text"
													on:blur={() => updateNoteValue(test.note, test.test.id, test.result)}
													bind:value={test.note}
													class="w-full h-20 p-2 rounded-lg border bg-gray-200 border-gray-400 resize-none"
													placeholder="Voeg een nota toe..."
												/>
											</div>
										</div>
									{/if}
								</div>
							{:else}
								<div class="w-full bg-white border border-gray-200 rounded-xl my-4 p-4">
									<div class="grid grid-cols-[1fr_3fr_auto_auto_2fr_1fr_1fr] items-center">
										<!-- Code Section -->
										<div class="flex flex-col items-start">
											<span class="text-sm text-gray-500">Code</span>
											<span class="text-xl font-semibold">{test.test.testCode}</span>
										</div>

										<!-- Test Section -->
										<div class="flex flex-col items-start col-span-2">
											<span class="text-sm text-gray-500">Test</span>
											<span class="text-xl font-semibold"
												>{test.test.naam.length > 20
													? test.test.naam.slice(0, 20) + '...'
													: test.test.naam}</span
											>
										</div>

										<!-- Value Section -->
										<div class="flex flex-col items-start">
											<span class="text-sm text-gray-500">Waarde</span>
											<input
												on:blur={() => updateResult(test.result, test.test.id, test.note)}
												bind:value={test.result}
												type="text"
												class="bg-gray-200 h-10 rounded-lg border border-gray-400 px-1 disabled:border-0 disabled:bg-gray-300 disabled:cursor-not-allowed"
												placeholder={test.result}
												disabled={test.failed}
											/>
										</div>

										<!-- Unit Section -->
										<div class="flex flex-col items-start ml-3">
											<span class="text-sm text-gray-500">Eenheid</span>
											<span class="text-xl font-semibold">{test.test.eenheid.afkorting}</span>
										</div>

										<!-- failed -->
										<div class="flex flex-col items-center">
											<span class="text-sm text-gray-500">Gefaald</span>
											<input
												type="checkbox"
												class="w-5 h-5 mt-3 text-blue-500 border-gray-300 rounded focus:ring-2 focus:ring-blue-500"
												checked={test.failed}
												on:change={() => handleCheckboxChange(test)}
											/>
										</div>

										<!-- Note Section -->
										<div class="flex flex-col items-center">
											<span class="text-sm text-gray-500">Nota</span>
											<button
												on:click={() => toggleNoteInput(test.test.id)}
												class="text-white bg-blue-500 h-12 p-3 rounded-lg"
											>
												<IoMdText />
											</button>
										</div>
									</div>
									{#if openNoteId == test.test.id}
										<div>
											<div transition:slide class="mt-4 p-4">
												<span class="text-sm text-gray-500">Nota</span>
												<input
													type="text"
													on:blur={() => updateNoteValue(test.note, test.test.id, test.result)}
													bind:value={test.note}
													class="w-full h-20 p-2 rounded-lg border bg-gray-200 border-gray-400 resize-none"
													placeholder="Voeg een nota toe..."
												/>
											</div>
										</div>
									{/if}
								</div>
							{/if}
						{/each}
					</div>
				</div>
			</div>
		</div>
	</div>
</main>
