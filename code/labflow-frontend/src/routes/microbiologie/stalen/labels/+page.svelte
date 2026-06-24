<script lang="ts">
	import Nav from '../../../../components/nav.svelte';
	import { goto, invalidateAll } from '$app/navigation';
	import { page } from '$app/stores';
	import { getCookie, formatDate, formatSex } from '$lib/globalFunctions';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import FaArrowRight from 'svelte-icons/fa/FaArrowRight.svelte';
	// @ts-ignore
	import FaCloudDownloadAlt from 'svelte-icons/fa/FaCloudDownloadAlt.svelte';
	// @ts-ignore
	import FaCheck from 'svelte-icons/fa/FaCheck.svelte';
	// @ts-ignore
	import IoIosClose from 'svelte-icons/io/IoIosClose.svelte';
	import { microbiologyStaalIdStore } from '$lib/store';
	import { get } from 'svelte/store';
	import { onDestroy, onMount } from 'svelte';
	import type { MicrobiologyStaal, MicrobiologyVoedingsbodem } from '$lib/types/dbTypes';
	import {
		addMicrobiologyVoedingsbodems,
		confirmMicrobiologyVoedingsbodems,
		fetchMicrobiologyConfirmedVoedingsbodems,
		fetchMicrobiologyStaalById,
		fetchMicrobiologyVoedingsbodems
	} from '$lib/fetchFunctions';
	import * as JSPM from 'jsprintmanager';

	const { ClientPrintJob, DefaultPrinter, InstalledPrinter, JSPrintManager, WSStatus } = JSPM;
	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	let staal: MicrobiologyStaal | null = null;
	let staalId: number | undefined;
	let voedingsbodems: MicrobiologyVoedingsbodem[] = [];
	let lockedVbIds = new Set<number>();
	let selectedVbIds = new Set<number>();
	let newlyAddedVbIds: number[] = [];
	let confirmed = false;
	let pdfUrl = '';
	let token = '';
	let amount = 1;
	let printers: string[] = [];
	let selectedPrinter = '';
	let useDefaultPrinter = false;

	$: addMode = $page.url.searchParams.get('mode') === 'add';

	function authToken(): string {
		return getCookie('authToken') || '';
	}

	function isLockedVb(id: number) {
		return addMode && lockedVbIds.has(id);
	}

	function toggleVb(id: number) {
		if (confirmed) return;
		if (isLockedVb(id)) return;
		const next = new Set(selectedVbIds);
		if (next.has(id)) next.delete(id);
		else next.add(id);
		selectedVbIds = next;
	}

	function newlySelectedIds(): number[] {
		return Array.from(selectedVbIds).filter((id) => !lockedVbIds.has(id));
	}

	function labelQueryParam(ids: number[]): string {
		return ids.length > 0 ? `?voedingsbodemIds=${ids.join(',')}` : '';
	}

	async function terug() {
		if (addMode) {
			goto('/microbiologie/stalen/notitieblok');
			return;
		}
		goto('/microbiologie/stalen/nieuw');
	}

	async function volgende() {
		if (addMode) {
			await invalidateAll();
			goto('/microbiologie/stalen/notitieblok');
			return;
		}
		goto('/microbiologie/stalen/saved');
	}

	async function loadData() {
		token = authToken();
		if (!token) {
			goto('/');
			return;
		}
		const sidRaw = get(microbiologyStaalIdStore);
		const sid = sidRaw ? Number(String(sidRaw).trim()) : NaN;
		if (!Number.isFinite(sid) || sid <= 0) {
			goto('/microbiologie/stalen');
			return;
		}
		staalId = sid;
		const s = (await fetchMicrobiologyStaalById(sid)) as MicrobiologyStaal | null;
		if (!s?.id) {
			goto('/microbiologie/stalen');
			return;
		}
		staal = s;
		const vbs = (await fetchMicrobiologyVoedingsbodems(sid)) as MicrobiologyVoedingsbodem[] | null;
		voedingsbodems = Array.isArray(vbs) ? vbs : [];

		if (addMode) {
			const confirmedVbs =
				((await fetchMicrobiologyConfirmedVoedingsbodems(sid)) as MicrobiologyVoedingsbodem[] | null) ?? [];
			lockedVbIds = new Set(confirmedVbs.map((v) => v.id));
			selectedVbIds = new Set(lockedVbIds);
		} else {
			lockedVbIds = new Set();
			selectedVbIds = new Set(voedingsbodems.map((v) => v.id));
		}
	}

	async function onConfirm() {
		if (staalId == null) return;
		if (confirmed) {
			confirmed = false;
			newlyAddedVbIds = [];
			if (pdfUrl) {
				URL.revokeObjectURL(pdfUrl);
				pdfUrl = '';
			}
			return;
		}

		if (addMode) {
			const toAdd = newlySelectedIds();
			if (toAdd.length === 0) {
				console.error('Selecteer minstens één nieuwe voedingsbodem');
				return;
			}
			const res = await addMicrobiologyVoedingsbodems(staalId, toAdd);
			if (!res?.ok) {
				console.error('Voedingsbodems toevoegen mislukt');
				return;
			}
			newlyAddedVbIds = toAdd;
			lockedVbIds = new Set([...lockedVbIds, ...toAdd]);
			selectedVbIds = new Set(lockedVbIds);
		} else {
			const res = await confirmMicrobiologyVoedingsbodems(staalId, Array.from(selectedVbIds));
			if (!res?.ok) {
				console.error('Confirm voedingsbodems failed');
				return;
			}
			newlyAddedVbIds = [];
		}

		confirmed = true;
		await fetchPdf();
	}

	async function fetchPdf() {
		if (staalId == null || !confirmed) return;
		token = authToken();
		if (!token) return;
		if (pdfUrl) {
			URL.revokeObjectURL(pdfUrl);
			pdfUrl = '';
		}
		const filterIds = addMode ? newlyAddedVbIds : [];
		const response = await fetch(
			`${backend_path}/api/microbiology/pdf/generatelabel/${staalId}${labelQueryParam(filterIds)}`,
			{
				method: 'GET',
				headers: { Authorization: `Bearer ${authToken()}` }
			}
		);
		if (response.ok) {
			const pdfBlob = await response.blob();
			pdfUrl = URL.createObjectURL(pdfBlob);
		}
	}

	async function getPdf(id: number) {
		try {
			const filterIds = addMode ? newlyAddedVbIds : [];
			const response = await fetch(
				`${backend_path}/api/microbiology/pdf/generatelabel/${id}${labelQueryParam(filterIds)}`,
				{
					method: 'GET',
					headers: { Authorization: `Bearer ${authToken()}` }
				}
			);
			if (!response.ok) return;
			const disposition = response.headers.get('X-Filename');
			let filename = `Labels_${staal?.patientAchternaam ?? ''}_${staal?.patientVoornaam ?? ''}`;
			if (disposition && disposition.includes('filename=')) {
				const match = disposition.match(/filename="(.+?)"/);
				if (match?.[1]) filename = match[1];
			}
			const blob = await response.blob();
			const url = window.URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = filename;
			document.body.appendChild(a);
			a.click();
			a.remove();
			window.URL.revokeObjectURL(url);
		} catch (e) {
			console.error(e);
		}
	}

	async function printLabels(id: number, copies: number) {
		try {
			const filterIds = addMode ? newlyAddedVbIds : [];
			const response = await fetch(
				`${backend_path}/api/microbiology/printer/labels/${id}/${copies}${labelQueryParam(filterIds)}`,
				{
					method: 'GET',
					headers: { Authorization: `Bearer ${authToken()}` }
				}
			);
			if (!response.ok) return;
			const zplCode = await response.text();
			if (!jspmWSStatus()) return;
			const cpj = new ClientPrintJob();
			cpj.clientPrinter = useDefaultPrinter
				? new DefaultPrinter()
				: new InstalledPrinter(selectedPrinter);
			cpj.printerCommands = zplCode.trim();
			cpj.sendToClient();
		} catch (e) {
			console.error(e);
		}
	}

	function jspmWSStatus() {
		const status = JSPrintManager.websocket_status;
		return status === WSStatus.Open;
	}

	function fetchPrinters() {
		JSPrintManager.getPrinters().then((value) => {
			const printerList = Array.isArray(value) ? (value as string[]) : [];
			printers = printerList;
			if (printers.length > 0) selectedPrinter = printers[0];
		});
	}

	onMount(async () => {
		await loadData();
		JSPrintManager.auto_reconnect = true;
		JSPrintManager.start();
		if (JSPrintManager.WS) {
			JSPrintManager.WS.onStatusChanged = () => {
				if (jspmWSStatus()) fetchPrinters();
			};
		}
	});

	onDestroy(() => {
		if (pdfUrl) URL.revokeObjectURL(pdfUrl);
	});

	$: labelCountText = (() => {
		if (addMode) {
			if (confirmed) {
				return `${newlyAddedVbIds.length} nieuwe label${newlyAddedVbIds.length === 1 ? '' : 's'}`;
			}
			const available = voedingsbodems.filter((v) => !lockedVbIds.has(v.id)).length;
			return `${lockedVbIds.size} bestaand, ${available} beschikbaar om toe te voegen`;
		}
		return confirmed
			? `${1 + selectedVbIds.size} voedingsbodems`
			: `${1 + voedingsbodems.length} voedingsbodems (max)`;
	})();

	$: canConfirm = addMode ? newlySelectedIds().length > 0 : selectedVbIds.size > 0;
</script>

<Nav />
<main class="px-8">
	<div class="h-full rounded-2xl bg-gray-200 p-4">
		<div class="flex flex-row space-x-4">
			<div class="grid h-20 w-5/6 grid-cols-5 gap-2 rounded-lg bg-white px-2">
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Code</p>
					<p class="font-bold">{staal?.staalCode ?? '…'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Achternaam</p>
					<p class="font-bold">{staal?.patientAchternaam || '…'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Voornaam</p>
					<p class="font-bold">{staal?.patientVoornaam || '…'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Geboortedatum</p>
					<p class="font-bold">
						{staal?.patientGeboorteDatum ? formatDate(staal.patientGeboorteDatum) : '…'}
					</p>
				</div>
				<div class="flex flex-col justify-center pl-4">
					<p class="text-gray-400">Geslacht</p>
					<p class="font-bold">
						{staal?.patientGeslacht ? formatSex(staal.patientGeslacht) : '…'}
					</p>
				</div>
			</div>

			<div class="flex w-3/12 flex-row justify-end space-x-2 pb-5">
				<button
					type="button"
					class="flex h-20 w-1/2 flex-row items-center justify-center rounded-lg bg-gray-400 p-3 text-xl text-white"
					on:click={terug}
				>
					<div class="mr-2 h-5 w-5"><FaArrowLeft /></div>
					Terug
				</button>
				<button
					type="button"
					class="flex h-20 w-1/2 flex-row items-center justify-center rounded-lg bg-blue-600 p-3 text-xl text-white disabled:cursor-not-allowed disabled:bg-gray-300"
					disabled={!confirmed}
					on:click={volgende}
				>
					{addMode ? 'Terug naar notitieblok' : 'Volgende'}
					<div class="ml-2 h-5 w-5"><FaArrowRight /></div>
				</button>
			</div>
		</div>

		<div class="flex h-full space-x-4">
			<div class="flex h-[75vh] w-1/3 flex-col rounded-xl bg-white p-4">
				<p class="text-blue-500">{labelCountText}</p>
				{#if !addMode}
					<div class="my-3 flex items-center justify-between rounded-xl border border-gray-200 p-4">
						<p class="text-lg font-bold">
							{staal?.patientVoornaam ?? ''} {staal?.patientAchternaam ?? ''}
						</p>
						<p class="rounded-full bg-blue-500 px-8 py-3 text-white">standaard</p>
					</div>
				{:else}
					<p class="my-3 rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm text-blue-800">
						Bestaande voedingsbodems blijven geselecteerd. Kies extra voedingsbodems om toe te voegen.
						Alleen de nieuwe labels worden afgedrukt.
					</p>
				{/if}

				<div class="min-h-0 flex-1 space-y-2 overflow-auto">
					{#each voedingsbodems as vb}
						{@const isSelected = selectedVbIds.has(vb.id)}
						{@const isLocked = isLockedVb(vb.id)}
						<button
							type="button"
							disabled={confirmed || isLocked}
							class="flex w-full items-center justify-between gap-3 rounded-xl border border-gray-200 p-4 text-left hover:bg-gray-50 disabled:cursor-default disabled:opacity-90 {isLocked
								? 'border-green-200 bg-green-50'
								: ''}"
							on:click={() => toggleVb(vb.id)}
						>
							<div class="flex items-center gap-3">
								<div
									class="flex h-12 items-center justify-center rounded-full text-white"
									style="background-color: {isSelected ? '#23E22C' : '#E3E3E3'};"
								>
									{#if isSelected}
										<div class="h-12 rounded-full p-3 text-white"><FaCheck /></div>
									{:else}
										<div class="h-12 rounded-full text-white"><IoIosClose /></div>
									{/if}
								</div>
								<span class="font-semibold">{vb.naam}</span>
								{#if isLocked}
									<span class="text-sm text-green-700">(reeds toegevoegd)</span>
								{/if}
							</div>
						</button>
					{/each}
				</div>

				<div class="mt-3 shrink-0">
					<button
						type="button"
						class="w-full rounded-lg bg-blue-600 py-2 text-white disabled:cursor-not-allowed disabled:bg-gray-300"
						disabled={!confirmed && !canConfirm}
						on:click={onConfirm}
					>
						{confirmed ? 'Wijzigen' : addMode ? 'Toevoegen' : 'Bevestigen'}
					</button>
				</div>
			</div>

			<div class="flex w-2/3 flex-col justify-between space-y-4">
				<div class="h-4/5 w-full">
					{#if confirmed && pdfUrl}
						<iframe
							title="pdf label preview"
							src={pdfUrl + '#toolbar=0'}
							width="100%"
							class="h-full rounded-xl"
						></iframe>
					{:else}
						<div
							class="flex h-full min-h-[320px] flex-col items-center justify-center rounded-xl bg-white p-8 text-center text-gray-600"
						>
							<p class="mb-2 text-lg font-medium">Voorbeeld nog niet beschikbaar</p>
							<p>
								{#if addMode}
									Selecteer extra voedingsbodems en druk op <strong>Toevoegen</strong> om het
									PDF-voorbeeld van de nieuwe labels te laden.
								{:else}
									Selecteer de gewenste voedingsbodems en druk op <strong>Bevestigen</strong> om
									het PDF-voorbeeld te laden.
								{/if}
							</p>
						</div>
					{/if}
				</div>

				<div class="flex h-1/5 w-full items-baseline justify-between bg-slate-200">
					<div class="mt-auto w-1/4">
						<button
							type="button"
							disabled={!confirmed || staalId == null}
							class="flex h-20 w-full flex-row items-center justify-center rounded-lg bg-blue-600 p-3 text-xl text-white disabled:cursor-not-allowed disabled:bg-gray-300"
							on:click={() => staalId != null && printLabels(staalId, amount)}
						>
							afdrukken
						</button>
					</div>
					<div class="mt-auto w-1/4">
						<button
							type="button"
							disabled={!confirmed || staalId == null}
							class="ml-4 flex h-20 w-20 flex-row items-center justify-center rounded-lg bg-gray-400 p-3 text-white disabled:cursor-not-allowed disabled:bg-gray-300"
							on:click={() => staalId != null && getPdf(staalId)}
						>
							<div class="h-5"><FaCloudDownloadAlt /></div>
						</button>
					</div>
					<div class="mt-auto flex w-2/4 justify-between">
						<div class="w-1/2">
							<p>hoeveelheid</p>
							<input
								type="number"
								bind:value={amount}
								disabled={!confirmed}
								class="h-20 w-11/12 rounded-lg border border-gray-400 bg-white p-3 text-xl disabled:bg-gray-200"
							/>
						</div>
						<div class="w-1/2">
							<p>printer</p>
							<select
								bind:value={selectedPrinter}
								disabled={!confirmed}
								class="h-20 w-full rounded-lg border border-gray-400 bg-white p-3 text-xl disabled:bg-gray-200"
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
