<script lang="ts">
	import Nav from '../../../../components/nav.svelte';
	import { goto, afterNavigate } from '$app/navigation';
	import { getCookie, formatDate, formatSex } from '$lib/globalFunctions';
	import { microbiologyStaalIdStore } from '$lib/store';
	import { get } from 'svelte/store';
	import { onMount } from 'svelte';
	import type {
		MicrobiologyNotebookResponse,
		MicrobiologyNotebookSection,
		MicrobiologyVoedingsbodemLogEntry
	} from '$lib/types/dbTypes';
	import {
		fetchMicrobiologyNotebook,
		patchMicrobiologyStaalCommentaar,
		patchMicrobiologySectionVoltooid,
		patchMicrobiologyStaalKlaar,
		updateMicrobiologyStaalTest,
		patchMicrobiologyVoedingsbodemCommentaar,
		syncMicrobiologyVoedingsbodemLogs,
		updateMicrobiologyGramkleuring,
		updateMicrobiologyAntibiogram
	} from '$lib/fetchFunctions';
	import NotebookTabAlgemeneTesten from './NotebookTabAlgemeneTesten.svelte';
	import NotebookTabVoedingsbodems from './NotebookTabVoedingsbodems.svelte';
	import NotebookTabGramkleuring from './NotebookTabGramkleuring.svelte';
	import NotebookTabAntibiogram from './NotebookTabAntibiogram.svelte';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import FaCheck from 'svelte-icons/fa/FaCheck.svelte';
	// @ts-ignore
	import IoIosClose from 'svelte-icons/io/IoIosClose.svelte';
	// @ts-ignore
	import FaCloudDownloadAlt from 'svelte-icons/fa/FaCloudDownloadAlt.svelte';

	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	type TabDef = {
		id: MicrobiologyNotebookSection;
		label: string;
		voltooidKey: keyof MicrobiologyNotebookResponse;
	};

	const TABS: TabDef[] = [
		{ id: 'algemene-testen', label: 'Algemene testen', voltooidKey: 'voltooidAlgemeneTesten' },
		{ id: 'voedingsbodems', label: 'Cultuur', voltooidKey: 'voltooidVoedingsbodems' },
		{ id: 'gramkleuring', label: 'Gramkleuring', voltooidKey: 'voltooidGramkleuring' },
		{ id: 'antibiogram', label: 'Antibiogram', voltooidKey: 'voltooidAntibiogram' }
	];

	let staalId: number | undefined;
	let notebook: MicrobiologyNotebookResponse | null = null;
	let selectedTab: MicrobiologyNotebookSection = 'algemene-testen';
	let generalComment = '';
	let token = '';
	let showAfrondenConfirm = false;

	function authToken(): string {
		return getCookie('authToken') || '';
	}

	function ensureSelectedTabVisible() {
		if (!notebook?.activeSections?.length) return;
		if (!notebook.activeSections.includes(selectedTab)) {
			selectedTab = notebook.activeSections[0];
		}
	}

	$: visibleTabs = notebook
		? TABS.filter((t) => (notebook!.activeSections ?? []).includes(t.id))
		: TABS;

	$: voltooidFlags = notebook
		? {
				'algemene-testen': notebook.voltooidAlgemeneTesten,
				voedingsbodems: notebook.voltooidVoedingsbodems,
				gramkleuring: notebook.voltooidGramkleuring,
				antibiogram: notebook.voltooidAntibiogram
			}
		: ({} as Record<MicrobiologyNotebookSection, boolean>);

	$: allVoltooid =
		notebook != null && visibleTabs.length > 0 && visibleTabs.every((tab) => voltooidFlags[tab.id]);

	$: currentTab = visibleTabs.find((t) => t.id === selectedTab) ?? visibleTabs[0];
	$: tabVoltooid = voltooidFlags[selectedTab] ?? false;
	$: notebookLocked = notebook?.status === 'KLAAR';
	$: sectionLocked = tabVoltooid || notebookLocked;

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
		const data = (await fetchMicrobiologyNotebook(sid)) as MicrobiologyNotebookResponse | null;
		if (!data?.id) {
			goto('/microbiologie/stalen');
			return;
		}
		notebook = data;
		generalComment = data.commentaar ?? '';
		initVoedingsbodemLogs();
		ensureSelectedTabVisible();
	}

	function initVoedingsbodemLogs() {
		if (!notebook) return;
		for (const vb of notebook.voedingsbodems) {
			if (!vb.logs || vb.logs.length === 0) {
				vb.logs = [{ organisme: '', beoordeling: '', commentaar: '' }];
			} else {
				const last = vb.logs[vb.logs.length - 1];
				const hasContent =
					(last.organisme && last.organisme.trim()) ||
					(last.beoordeling && last.beoordeling.trim()) ||
					(last.commentaar && last.commentaar.trim());
				if (hasContent) {
					vb.logs.push({ organisme: '', beoordeling: '', commentaar: '' });
				}
			}
		}
	}

	async function saveGeneralComment() {
		if (staalId == null || !notebook) return;
		await patchMicrobiologyStaalCommentaar(staalId, generalComment);
		notebook.commentaar = generalComment;
	}

	async function toggleVoltooid() {
		if (staalId == null || !notebook) return;
		const next = !tabVoltooid;
		const res = await patchMicrobiologySectionVoltooid(staalId, selectedTab, next);
		if (res?.ok && currentTab) {
			notebook = { ...notebook, [currentTab.voltooidKey]: next };
		}
	}

	async function handleTestSave(
		event: CustomEvent<{
			staalTestId: number;
			waarde?: string;
			commentaar?: string;
			failed?: boolean;
		}>
	) {
		if (staalId == null || !notebook) return;
		const { staalTestId, waarde, commentaar, failed } = event.detail;
		const res = await updateMicrobiologyStaalTest(staalId, staalTestId, event.detail);
		if (!res?.ok) return;
		const nextFailed =
			failed ?? notebook.algemeneTesten.find((t) => t.id === staalTestId)?.failed ?? false;
		notebook = {
			...notebook,
			algemeneTesten: notebook.algemeneTesten.map((t) =>
				t.id === staalTestId
					? {
							...t,
							waarde: nextFailed ? '' : waarde !== undefined ? waarde : t.waarde,
							commentaar: commentaar !== undefined ? commentaar : t.commentaar,
							failed: nextFailed
						}
					: t
			)
		};
	}

	async function handleVbComment(event: CustomEvent<{ linkId: number; commentaar: string }>) {
		if (staalId == null) return;
		await patchMicrobiologyVoedingsbodemCommentaar(
			staalId,
			event.detail.linkId,
			event.detail.commentaar
		);
	}

	async function handleVbLogs(
		event: CustomEvent<{ linkId: number; logs: MicrobiologyVoedingsbodemLogEntry[] }>
	) {
		if (staalId == null || !notebook) return;
		const res = await syncMicrobiologyVoedingsbodemLogs(
			staalId,
			event.detail.linkId,
			event.detail.logs
		);
		if (!res?.ok) return;
		const data = (await fetchMicrobiologyNotebook(staalId)) as MicrobiologyNotebookResponse | null;
		if (!data) return;
		const savedLogs = data.voedingsbodems.find((v) => v.linkId === event.detail.linkId)?.logs ?? [];
		notebook = {
			...notebook,
			voedingsbodems: notebook.voedingsbodems.map((vb) =>
				vb.linkId === event.detail.linkId ? { ...vb, logs: [...savedLogs] } : vb
			)
		};
		initVoedingsbodemLogs();
	}

	async function handleGramSave(
		event: CustomEvent<{
			commentaar?: string | null;
			rows: { bepaling: string; score: string; commentaar: string }[];
		}>
	) {
		if (staalId == null) return;
		await updateMicrobiologyGramkleuring(staalId, {
			commentaar: event.detail.commentaar ?? undefined,
			rows: event.detail.rows
		});
	}

	async function handleAbSave(event: CustomEvent<{ antibioticaId: number; beoordeling: string }>) {
		if (staalId == null) return;
		await updateMicrobiologyAntibiogram(staalId, [event.detail]);
	}

	async function markStaalDone() {
		if (staalId == null || !notebook || !allVoltooid || notebookLocked) return;
		await patchMicrobiologyStaalCommentaar(staalId, generalComment);
		const res = await patchMicrobiologyStaalKlaar(staalId);
		if (res?.ok) {
			showAfrondenConfirm = false;
			microbiologyStaalIdStore.set('');
			goto('/microbiologie/stalen/done');
		}
	}

	function requestAfronden() {
		if (!allVoltooid || notebookLocked) return;
		showAfrondenConfirm = true;
	}

	async function downloadResults() {
		if (staalId == null || (!allVoltooid && !notebookLocked)) return;
		try {
			const response = await fetch(
				`${backend_path}/api/microbiology/pdf/generateresults/${staalId}`,
				{
					method: 'GET',
					headers: { Authorization: `Bearer ${authToken()}` }
				}
			);
			if (!response.ok) return;

			const disposition = response.headers.get('X-Filename');
			let filename = `Resultaten_${notebook?.patientAchternaam}_${notebook?.patientVoornaam}.pdf`;
			if (disposition?.includes('filename=')) {
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
			console.error('PDF download mislukt:', e);
		}
	}

	onMount(loadData);

	afterNavigate(({ from }) => {
		if (from?.url?.pathname === '/microbiologie/stalen/labels') {
			loadData();
		}
	});
</script>

<Nav />
<main class="box-border h-[calc(100vh-4rem)] overflow-hidden px-8 pb-4">
	<div class="flex h-full min-h-0 flex-col rounded-2xl bg-gray-200 p-4">
		<div class="mb-4 flex shrink-0 flex-row space-x-4">
			<div class="grid h-20 w-5/6 grid-cols-5 space-x-2 rounded-lg bg-white px-2">
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Code</p>
					<p class="font-bold">{notebook?.staalCode ?? '...'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Achternaam</p>
					<p class="font-bold">{notebook?.patientAchternaam ?? '...'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Voornaam</p>
					<p class="font-bold">{notebook?.patientVoornaam ?? '...'}</p>
				</div>
				<div class="flex flex-col justify-center">
					<p class="text-gray-400">Geboortedatum</p>
					<p class="font-bold">
						{notebook?.patientGeboorteDatum ? formatDate(notebook.patientGeboorteDatum) : '...'}
					</p>
				</div>
				<div class="flex flex-col justify-center pl-5">
					<p class="text-gray-400">Geslacht</p>
					<p class="font-bold">
						{notebook?.patientGeslacht ? formatSex(notebook.patientGeslacht) : '...'}
					</p>
				</div>
			</div>
			<div class="flex w-3/12 flex-row justify-end gap-3 pb-5">
				<button
					type="button"
					on:click={() => goto('/microbiologie/stalen')}
					class="flex h-20 flex-1 flex-row items-center justify-center rounded-lg bg-gray-400 p-3 text-xl text-white"
				>
					<div class="mr-2 h-5 w-5"><FaArrowLeft /></div>
					Sluiten
				</button>
				<button
					type="button"
					on:click={requestAfronden}
					disabled={!allVoltooid || notebookLocked}
					class="flex h-20 flex-1 flex-row items-center justify-center rounded-lg bg-blue-600 p-3 text-xl text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300"
				>
					<div class="mr-2 h-5 w-5"><FaCheck /></div>
					{notebookLocked ? 'Afgerond' : 'Afronden'}
				</button>
			</div>
		</div>

		<div class="flex min-h-0 flex-1 space-x-4">
			<div class="flex w-1/3 min-h-0 flex-col space-y-4">
				<div class="min-h-0 flex-1 overflow-y-auto rounded-xl bg-white p-4">
					<p class="text-blue-500">
						{visibleTabs.length}
						{visibleTabs.length === 1 ? 'sectie' : 'secties'}
					</p>
					{#each visibleTabs as tab (tab.id)}
						<button
							type="button"
							on:click={() => (selectedTab = tab.id)}
							class="my-3 flex w-full cursor-pointer items-center justify-between rounded-xl border border-gray-200 p-4 transition hover:scale-[101%] hover:bg-gray-100 {selectedTab ===
							tab.id
								? 'ring-2 ring-blue-400'
								: ''}"
						>
							<p class="ml-1 text-lg font-bold">{tab.label}</p>
							{#if voltooidFlags[tab.id]}
								<div
									class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full text-white"
									style="background-color: #23E22C;"
								>
									<div class="h-5 w-5"><FaCheck /></div>
								</div>
							{:else}
								<div
									class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full text-white"
									style="background-color: #E3E3E3;"
								>
									<div class="h-6 w-6"><IoIosClose /></div>
								</div>
							{/if}
						</button>
					{/each}
				</div>

				<div>
					<p class="mb-2 font-semibold text-gray-700">Algemene commentaar</p>
					<textarea
						bind:value={generalComment}
						disabled={notebookLocked}
						on:blur={saveGeneralComment}
						class="h-28 w-full rounded-lg border border-gray-400 bg-white p-3 disabled:cursor-not-allowed disabled:bg-gray-200"
						placeholder="Algemene commentaar over de staal..."
					></textarea>
				</div>

				<button
					type="button"
					on:click={downloadResults}
					disabled={!allVoltooid && !notebookLocked}
					class="flex h-20 w-full shrink-0 items-center justify-center rounded-lg bg-blue-600 p-3 text-xl text-white disabled:cursor-not-allowed disabled:bg-gray-300"
				>
					<div class="h-6 px-4"><FaCloudDownloadAlt /></div>
					<p class="font-bold">Download Resultaten</p>
				</button>
			</div>

			<div class="flex min-h-0 w-2/3 flex-col">
				<div class="flex min-h-0 flex-1 flex-col overflow-hidden rounded-xl bg-white">
					<div class="flex shrink-0 items-center justify-between border-b border-gray-200 p-4">
						<div class="flex items-center">
							{#if tabVoltooid}
								<div
									class="mr-3 flex h-12 w-12 shrink-0 items-center justify-center rounded-full text-white"
									style="background-color: #23E22C;"
								>
									<div class="h-5 w-5"><FaCheck /></div>
								</div>
							{:else}
								<div
									class="mr-3 flex h-12 w-12 shrink-0 items-center justify-center rounded-full text-white"
									style="background-color: #E3E3E3;"
								>
									<div class="h-6 w-6"><IoIosClose /></div>
								</div>
							{/if}
							<p class="text-xl font-bold">{currentTab?.label ?? ''}</p>
						</div>
						<button
							type="button"
							on:click={toggleVoltooid}
							disabled={notebookLocked}
							class="rounded-lg px-4 py-2 text-white disabled:cursor-not-allowed disabled:bg-gray-400 {tabVoltooid
								? 'bg-red-600 hover:bg-red-700'
								: 'bg-blue-600 hover:bg-blue-700'}"
						>
							{tabVoltooid ? 'Ongedaan maken' : 'Voltooi'}
						</button>
					</div>

					<div class="min-h-0 flex-1 overflow-y-auto">
						{#if notebook}
							{#if selectedTab === 'algemene-testen'}
								<NotebookTabAlgemeneTesten
									tests={notebook.algemeneTesten}
									locked={sectionLocked}
									on:save={handleTestSave}
								/>
							{:else if selectedTab === 'voedingsbodems'}
								<NotebookTabVoedingsbodems
									voedingsbodems={notebook.voedingsbodems}
									locked={sectionLocked}
									on:saveComment={handleVbComment}
									on:saveLogs={handleVbLogs}
								/>
							{:else if selectedTab === 'gramkleuring'}
								<NotebookTabGramkleuring
									gramkleuring={notebook.gramkleuring}
									locked={sectionLocked}
									on:save={handleGramSave}
								/>
							{:else}
								<NotebookTabAntibiogram
									entries={notebook.antibiogram}
									locked={sectionLocked}
									on:save={handleAbSave}
								/>
							{/if}
						{/if}
					</div>
				</div>
			</div>
		</div>
	</div>
</main>

{#if showAfrondenConfirm}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
		<div class="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">
			<h2 class="mb-3 text-2xl font-bold text-gray-900">Staal afronden?</h2>
			<p class="mb-2 text-gray-700">
				Je staat op het punt dit staal af te ronden. Dit kan maar één keer gebeuren.
			</p>
			<p class="mb-6 text-gray-700">
				Nadien kan je de gegevens niet meer wijzigen. Je kan het staal nog wel bekijken en de PDF
				downloaden.
			</p>
			<div class="flex justify-end gap-3">
				<button
					type="button"
					class="rounded-lg bg-gray-300 px-5 py-3 font-semibold text-gray-800 hover:bg-gray-400"
					on:click={() => (showAfrondenConfirm = false)}
				>
					Annuleren
				</button>
				<button
					type="button"
					class="rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white hover:bg-blue-700"
					on:click={markStaalDone}
				>
					Definitief afronden
				</button>
			</div>
		</div>
	</div>
{/if}
