<script lang="ts">
	import Nav from '../../../components/nav.svelte';
	import LabelCart from './../../../components/LabelCart.svelte';
	import { goto } from '$app/navigation';
	import { formatDate, formatSex, generalFetch } from '$lib/globalFunctions';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import FaArrowRight from 'svelte-icons/fa/FaArrowRight.svelte';
	// @ts-ignore
	import FaCloudDownloadAlt from 'svelte-icons/fa/FaCloudDownloadAlt.svelte';
	import { staalCodeStore } from '$lib/store';
	import { onDestroy, onMount } from 'svelte';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	// types & factories
	import type { Test, TestCategorie, Staal } from '$lib/types/dbTypes';
	import { createDefaultStaal } from '$lib/factories/staalfactory';

	import * as JSPM from 'jsprintmanager';

	const { ClientPrintJob, DefaultPrinter, InstalledPrinter, JSPrintManager, WSStatus } = JSPM;

	// neem de id
	let sampleCode: string | undefined;
	staalCodeStore.subscribe((value) => {
		sampleCode = value;
	});

	// instantiëren van een leeg staalobject
	let staal: Staal = createDefaultStaal();

	let staalId: number;
	let testCategories: TestCategorie[] = [];

	// alle tests categorieën ophalen die bij de testen horen
	async function loadData() {
		try {
			staal = await generalFetch('GET', `staal`, true, sampleCode);
			// assign id to staalId
			staalId = staal.id;
			// Extract unique test categories
			await extractUniqueTestCategories(staal.registeredTests);
		} catch (error) {
			console.error('data kon niet gefetched worden:', error);
		}
	}

	// Get unique test categories based on their id
	async function extractUniqueTestCategories(registeredTests: Test[]) {
		const categoryMap = new Map();

		registeredTests.forEach((testItem: Test) => {
			const category = testItem.test.testcategorie;
			if (category.id !== 7 && !categoryMap.has(category.id)) {
				categoryMap.set(category.id, category);
			}
		});

		// Convert Map to an array of unique categories
		testCategories = Array.from(categoryMap.values());
	}

	// fetch labels pdf
	let pdfUrl = ''; // URL to display the PDF in the iframe

	async function fetchPdf() {
		const response = await fetch(`${backend_path}/api/pdf/generatelabel/${staalId}`, {
			method: 'GET',
			credentials: 'include'
		});

		if (response.ok) {
			const pdfBlob = await response.blob();
			pdfUrl = URL.createObjectURL(pdfBlob); // Create a blob URL for the PDF
		} else {
			console.error('Failed to fetch PDF');
		}
	}

	// pdf labels downloaden
	async function getPdf(staalId: number) {
		try {
			const response = await fetch(`${backend_path}/api/pdf/generatelabel/${staalId}`, {
				method: 'GET',
				credentials: 'include'
			});

			if (!response.ok) {
				console.error('Failed to fetch PDF:', response.statusText);
				return;
			}

			// Extract the filename from the Content-Disposition header
			const disposition = response.headers.get('X-Filename');
			let filename = `Labels_${staal?.patientAchternaam}_${staal?.patientVoornaam}`; // Default als de header er niet is van de backend

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
		} catch (error) {
			console.error('Error while downloading PDF:', error);
		}
	}

	// get the zpl code for the labels
	let zplCode: String = '';
	let amount: number = 1;

	async function printLabels(staalId: number, amount: number) {
		try {
			const response = await fetch(`${backend_path}/api/printer/labels/${staalId}/${amount}`, {
				method: 'GET',
				credentials: 'include'
			});

			if (!response.ok) {
				console.error('Failed to fetch ZPL:', response.statusText);
				return;
			}

			zplCode = await response.text();

			if (!jspmWSStatus()) return;

			const cpj = new ClientPrintJob();

			if (useDefaultPrinter) {
				cpj.clientPrinter = new DefaultPrinter();
			} else {
				cpj.clientPrinter = new InstalledPrinter(selectedPrinter);
			}

			cpj.printerCommands = zplCode.trim();
			cpj.sendToClient();
		} catch (error) {
			console.error('Error while downloading ZPL:', error);
		}
	}

	// connection with printer
	let printers: any = [];
	let selectedPrinter = '';
	let useDefaultPrinter = false;

	function jspmWSStatus() {
		const status = JSPrintManager.websocket_status;
		if (status == WSStatus.Open) {
			return true;
		} else if (status == WSStatus.Closed) {
			return false;
		} else if (status == WSStatus.Blocked) {
			return false;
		}
	}

	function fetchPrinters() {
		JSPrintManager.getPrinters().then((printerList) => {
			printers = printerList;
			if (printers.length > 0) selectedPrinter = printers[0];
		});
	}

	onMount(async () => {
		await loadData();
		await fetchPdf();

		JSPrintManager.auto_reconnect = true;
		JSPrintManager.start();
		if (JSPrintManager.WS) {
			JSPrintManager.WS.onStatusChanged = () => {
				if (jspmWSStatus()) {
					fetchPrinters();
				}
			};
		}
	});

	onDestroy(() => {
		if (pdfUrl) URL.revokeObjectURL(pdfUrl);
	});
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
					Annuleren
				</button>
				<!-- staat tijdelijk naar volgende pagina omdat ik nog niet weet hoe César zijn pagina heet -->
				<button
					on:click={() => {
						goto('/stalen/saved');
					}}
					class="bg-blue-600 text-xl rounded-lg p-3 text-white h-20 w-1/2 flex flex-row items-center justify-center"
				>
					Volgende
					<div class="w-5 h-5 ml-2"><FaArrowRight /></div>
				</button>
			</div>
		</div>
		<!-- bottom section -->
		<div class="flex space-x-4 h-full">
			<!-- left section -->
			<div class="bg-white w-1/3 h-[75vh] rounded-xl p-4">
				<p class="text-blue-500">{testCategories.length + 1} labels</p>
				<!-- standaard label -->
				<div class=" border border-gray-200 rounded-xl flex justify-between items-center p-4 my-3">
					<p class="font-bold text-lg">César Van Leuffelen</p>
					<p class="py-3 px-8 rounded-full text-white bg-blue-500">standaard</p>
				</div>
				<!-- loop labels -->
				{#each testCategories as testcategorie}
					<LabelCart labelName={testcategorie.naam} color={testcategorie.kleur} />
				{/each}
			</div>
			<!-- right section -->
			<div class="w-2/3 flex flex-col justify-between space-y-4">
				<!-- pdf previewer -->
				<div class="w-full h-4/5">
					<iframe
						src={pdfUrl + '#toolbar=0'}
						title="pdf label preview"
						width="100%"
						class="h-full rounded-xl"
					/>
				</div>
				<!-- bedienings knoppen -->
				<div class="w-full h-1/5 bg-slate-200 flex justify-between items-baseline">
					<!-- left button-->
					<div class="w-1/4 mt-auto">
						<button
							on:click={() => printLabels(staalId, amount)}
							class="bg-blue-600 text-xl rounded-lg p-3 text-white h-20 w-full flex flex-row items-center justify-center"
						>
							afdrukken
						</button>
					</div>
					<div class="w-1/4 mt-auto">
						<button
							on:click={() => getPdf(staalId)}
							class="bg-gray-400 text-xl rounded-lg ml-4 p-3 text-white w-20 h-20 flex flex-row items-center justify-center"
						>
							<div class="h-5">
								<FaCloudDownloadAlt />
							</div>
						</button>
					</div>
					<!-- right selects -->
					<div class=" w-2/4 mt-auto flex justify-between items-center">
						<div class="w-1/2">
							<p>hoeveelheid</p>
							<input
								type="number"
								bind:value={amount}
								class="rounded-lg text-xl p-3 h-20 w-11/12 bg-white border border-gray-400"
							/>
						</div>
						<div class="w-1/2">
							<p>printer</p>
							<select
								bind:value={selectedPrinter}
								class="rounded-lg text-xl p-3 h-20 bg-white w-full border border-gray-400"
							>
								{#each printers as printer}
									<option value={printer}>{printer}</option>
								{/each}
							</select>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</main>
