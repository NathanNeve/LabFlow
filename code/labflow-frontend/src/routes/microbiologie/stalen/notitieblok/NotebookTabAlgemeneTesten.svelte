<script lang="ts">
	import { slide } from 'svelte/transition';
	// @ts-ignore
	import IoMdText from 'svelte-icons/io/IoMdText.svelte';
	import type { MicrobiologyStaalTestDto } from '$lib/types/dbTypes';
	import { createEventDispatcher } from 'svelte';

	export let tests: MicrobiologyStaalTestDto[] = [];
	export let locked = false;

	const dispatch = createEventDispatcher<{
		save: { staalTestId: number; waarde?: string; commentaar?: string; failed?: boolean };
	}>();

	let openNoteId: number | null = null;

	function toggleNote(id: number) {
		openNoteId = openNoteId === id ? null : id;
	}

	function saveTest(test: MicrobiologyStaalTestDto, waarde?: string, commentaar?: string, failed?: boolean) {
		dispatch('save', {
			staalTestId: test.id,
			waarde: waarde !== undefined ? waarde : test.waarde ?? undefined,
			commentaar: commentaar !== undefined ? commentaar : test.commentaar ?? undefined,
			failed: failed !== undefined ? failed : test.failed
		});
	}

	function handleFailedChange(test: MicrobiologyStaalTestDto, nextFailed: boolean) {
		const waarde = nextFailed ? '' : (test.waarde ?? '');
		saveTest(test, waarde, test.commentaar ?? undefined, nextFailed);
	}
</script>

<div class="p-4">
	{#each tests as test (test.id)}
		<div class="my-4 w-full rounded-xl border border-gray-200 bg-white p-4">
			<div class="grid grid-cols-[1fr_3fr_auto_auto_1fr] items-center gap-2">
				<div class="flex flex-col items-start">
					<span class="text-sm text-gray-500">Code</span>
					<span class="text-xl font-semibold">{test.testCode}</span>
				</div>
				<div class="col-span-1 flex flex-col items-start">
					<span class="text-sm text-gray-500">Test</span>
					<span class="text-xl font-semibold">{test.testNaam}</span>
				</div>
				<div class="flex flex-col items-start">
					<span class="text-sm text-gray-500">Waarde</span>
					<input
						type="text"
						value={test.failed ? '' : (test.waarde ?? '')}
						disabled={locked || test.failed}
						on:input={(e) => {
							if (!test.failed) test.waarde = e.currentTarget.value;
						}}
						on:blur={() => saveTest(test)}
						class="h-10 rounded-lg border border-gray-400 bg-gray-200 px-1 disabled:cursor-not-allowed disabled:border-0 disabled:bg-gray-300"
					/>
				</div>
				<div class="flex flex-col items-center">
					<span class="text-sm text-gray-500">Gefaald</span>
					<input
						type="checkbox"
						checked={test.failed}
						disabled={locked}
						on:change={(e) => handleFailedChange(test, e.currentTarget.checked)}
						class="mt-3 h-5 w-5 rounded border-gray-300 text-blue-500 focus:ring-2 focus:ring-blue-500"
					/>
				</div>
				<div class="flex flex-col items-center">
					<span class="text-sm text-gray-500">Nota</span>
					<button
						type="button"
						on:click={() => toggleNote(test.id)}
						class="h-12 rounded-lg bg-blue-500 p-3 text-white"
					>
						<IoMdText />
					</button>
				</div>
			</div>
			{#if openNoteId === test.id}
				<div transition:slide class="mt-4 p-4">
					<span class="text-sm text-gray-500">Nota</span>
					<input
						type="text"
						bind:value={test.commentaar}
						disabled={locked}
						on:blur={() => saveTest(test)}
						class="h-20 w-full resize-none rounded-lg border border-gray-400 bg-gray-200 p-2 disabled:cursor-not-allowed disabled:bg-gray-300"
						placeholder="Voeg een nota toe..."
					/>
				</div>
			{/if}
		</div>
	{:else}
		<p class="text-gray-500">Geen algemene testen voor dit staal.</p>
	{/each}
</div>
