<script lang="ts">
	import type {
		MicrobiologyVoedingsbodemLogEntry,
		MicrobiologyVoedingsbodemNotebookDto
	} from '$lib/types/dbTypes';
	import { createEventDispatcher } from 'svelte';
	import { goto } from '$app/navigation';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';
	// @ts-ignore
	import FaClock from 'svelte-icons/fa/FaClock.svelte';

	export let voedingsbodems: MicrobiologyVoedingsbodemNotebookDto[] = [];
	export let locked = false;

	const dispatch = createEventDispatcher<{
		saveComment: { linkId: number; commentaar: string };
		saveLogs: { linkId: number; logs: MicrobiologyVoedingsbodemLogEntry[] };
	}>();

	const BEOORDELING_OPTIONS = ['+', '++', '+++', '++++'];

	let selectedLinkId: number | null = null;
	let draftRowKeys: string[] = [];
	let draftKeyCounter = 0;

	$: if (voedingsbodems.length > 0 && selectedLinkId == null) {
		selectedLinkId = voedingsbodems[0].linkId;
	}

	$: selected = voedingsbodems.find((v) => v.linkId === selectedLinkId) ?? null;

	$: if (selected) {
		syncDraftKeys(selected.logs);
	}

	function emptyRow(): MicrobiologyVoedingsbodemLogEntry {
		return { organisme: '', beoordeling: '', commentaar: '' };
	}

	function ensureTrailingEmptyRow(logs: MicrobiologyVoedingsbodemLogEntry[]) {
		if (logs.length === 0) {
			logs.push(emptyRow());
			draftRowKeys = ['draft-0'];
			return;
		}
		const last = logs[logs.length - 1];
		if (hasContent(last)) {
			logs.push(emptyRow());
			draftRowKeys = [...draftRowKeys, `draft-${++draftKeyCounter}`];
		}
	}

	function syncDraftKeys(logs: MicrobiologyVoedingsbodemLogEntry[]) {
		draftRowKeys = logs.map((row, i) =>
			row.id != null ? `saved-${row.id}` : (draftRowKeys[i] ?? `draft-${++draftKeyCounter}`)
		);
	}

	function rowKey(row: MicrobiologyVoedingsbodemLogEntry, index: number): string {
		if (row.id != null) return `saved-${row.id}`;
		return draftRowKeys[index] ?? `draft-${index}`;
	}

	function hasContent(row: MicrobiologyVoedingsbodemLogEntry) {
		return (
			(row.organisme && row.organisme.trim() !== '') ||
			(row.beoordeling && row.beoordeling.trim() !== '') ||
			(row.commentaar && row.commentaar.trim() !== '')
		);
	}

	function isSaveableRow(row: MicrobiologyVoedingsbodemLogEntry) {
		return (
			(row.organisme?.trim() ?? '') !== '' &&
			(row.beoordeling?.trim() ?? '') !== '' &&
			BEOORDELING_OPTIONS.includes(row.beoordeling.trim())
		);
	}

	function formatUpdatedAt(iso: string | undefined): string {
		if (!iso) return 'Onbekend';
		const d = new Date(iso);
		if (Number.isNaN(d.getTime())) return iso;
		return d.toLocaleString('nl-BE', {
			day: '2-digit',
			month: '2-digit',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}

	function onAddVoedingsbodem() {
		if (locked) return;
		goto('/microbiologie/stalen/labels?mode=add');
	}

	function onSelectVoedingsbodem() {
		if (selectedLinkId == null) return;
		const vb = voedingsbodems.find((v) => v.linkId === selectedLinkId);
		if (vb) {
			syncDraftKeys(vb.logs);
			ensureTrailingEmptyRow(vb.logs);
		}
	}

	function onLogCellBlur() {
		if (!selected || locked) return;
		ensureTrailingEmptyRow(selected.logs);

		const hasIncomplete = selected.logs.some((r) => hasContent(r) && !isSaveableRow(r));
		if (hasIncomplete) return;

		const toSave = selected.logs.filter((r) => isSaveableRow(r));
		dispatch('saveLogs', { linkId: selected.linkId, logs: toSave });
	}
</script>

<div class="space-y-6 p-4">
	{#if voedingsbodems.length === 0}
		<div class="space-y-4">
			<p class="text-gray-500">Geen cultuur geregistreerd voor dit staal.</p>
			<button
				type="button"
				disabled={locked}
				on:click={onAddVoedingsbodem}
				class="flex h-12 items-center gap-2 rounded-lg bg-blue-600 px-4 text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300"
			>
				<div class="h-4 w-4"><FaPlus /></div>
				Voedingsbodem toevoegen
			</button>
		</div>
	{:else}
		<section>
			<label for="vb-select" class="mb-2 block font-semibold text-gray-700">Selecteer cultuur</label
			>
			<div class="flex max-w-md items-center gap-2">
				<select
					id="vb-select"
					bind:value={selectedLinkId}
					on:change={onSelectVoedingsbodem}
					class="h-12 min-w-0 flex-1 rounded-lg border border-gray-400 bg-gray-200 px-3"
				>
					{#each voedingsbodems as vb (vb.linkId)}
						<option value={vb.linkId}>{vb.voedingsbodemNaam}</option>
					{/each}
				</select>
				<button
					type="button"
					disabled={locked}
					on:click={onAddVoedingsbodem}
					class="flex h-12 shrink-0 items-center justify-center rounded-lg bg-blue-600 px-4 text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300"
					title="Voedingsbodem toevoegen"
					aria-label="Voedingsbodem toevoegen"
				>
					<div class="h-4 w-4"><FaPlus /></div>
				</button>
			</div>
		</section>

		<hr class="border-gray-300" />

		{#if selected}
			<section>
				<h3 class="mb-2 font-semibold text-gray-700">Commentaar voor protocol</h3>
				<textarea
					bind:value={selected.commentaar}
					disabled={locked}
					on:blur={() =>
						dispatch('saveComment', {
							linkId: selected.linkId,
							commentaar: selected.commentaar ?? ''
						})}
					class="h-32 w-full rounded-lg border border-gray-400 bg-gray-200 p-3 disabled:cursor-not-allowed disabled:bg-gray-300"
					placeholder="Commentaar over deze cultuur..."
				></textarea>
			</section>

			<hr class="border-gray-300" />

			<section class="overflow-visible">
				<h3 class="mb-2 font-semibold text-gray-700">Isolaten</h3>
				<table class="w-full table-fixed border-collapse overflow-visible">
					<thead>
						<tr class="border-b border-gray-300 text-left text-sm text-gray-500">
							<th class="w-[8%] p-2"></th>
							<th class="w-[20%] p-2">Organisme</th>
							<th class="w-[14%] p-2">Beoordeling</th>
							<th class="w-[58%] p-2">Commentaar</th>
						</tr>
					</thead>
					<tbody>
						{#each selected.logs as row, i (rowKey(row, i))}
							<tr class="border-b border-gray-200 align-top">
								<td class="overflow-visible p-2 text-center">
									{#if (row.updatedAt ?? row.createdAt) && isSaveableRow(row)}
										{@const timestamp = row.updatedAt ?? row.createdAt}
										<div class="group relative inline-flex">
											<span
												class="inline-flex h-8 w-8 cursor-default items-center justify-center text-gray-500"
												aria-label="Laatst bijgewerkt: {formatUpdatedAt(timestamp)}"
											>
												<div class="h-4 w-4"><FaClock /></div>
											</span>
											<div
												class="pointer-events-none absolute left-full top-1/2 z-20 ml-2 -translate-y-1/2 whitespace-nowrap rounded-md bg-gray-800 px-2.5 py-1.5 text-xs text-white opacity-0 shadow-md transition-opacity group-hover:opacity-100"
												role="tooltip"
											>
												Laatst bijgewerkt: {formatUpdatedAt(timestamp)}
											</div>
										</div>
									{/if}
								</td>
								<td class="p-2">
									<input
										type="text"
										bind:value={row.organisme}
										disabled={locked}
										on:blur={onLogCellBlur}
										class="w-full rounded-lg border border-gray-400 bg-gray-200 px-2 py-1 disabled:bg-gray-300"
									/>
								</td>
								<td class="p-2">
									<select
										bind:value={row.beoordeling}
										disabled={locked}
										on:change={onLogCellBlur}
										class="w-full rounded-lg border border-gray-400 bg-gray-200 px-2 py-1 disabled:bg-gray-300"
									>
										<option value="">—</option>
										{#each BEOORDELING_OPTIONS as opt}
											<option value={opt}>{opt}</option>
										{/each}
									</select>
								</td>
								<td class="p-2">
									<textarea
										bind:value={row.commentaar}
										disabled={locked}
										on:blur={onLogCellBlur}
										rows="2"
										class="box-border min-h-[2.5rem] w-full resize-y overflow-x-hidden break-words rounded-lg border border-gray-400 bg-gray-200 px-2 py-1 disabled:bg-gray-300"
									></textarea>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</section>
		{/if}
	{/if}
</div>
