<script lang="ts">
	import { onDestroy, onMount } from 'svelte';
	import type { MicrobiologyVoedingsbodem } from '$lib/types/dbTypes';

	type Option = MicrobiologyVoedingsbodem & { label?: string };

	export let options: Option[] = [];
	export let value: Option[] = [];
	export let placeholder = 'Voedingsbodems';
	export let invalid = false;
	export let open = false;
	export let selectionChange: (() => void) | undefined = undefined;
	export let openChange: ((isOpen: boolean) => void) | undefined = undefined;

	let root: HTMLDivElement | undefined;
	$: selectedIdSet = new Set((value ?? []).map((item) => item.id));

	function labelFor(item: Option) {
		return item.label || item.naam;
	}

	function setOpen(isOpen: boolean) {
		if (open === isOpen) return;
		openChange?.(isOpen);
	}

	function toggleOpen() {
		setOpen(!open);
	}

	function toggleOption(option: Option) {
		if (selectedIdSet.has(option.id)) {
			value = (value ?? []).filter((item) => item.id !== option.id);
		} else {
			value = [...(value ?? []), option];
		}
		selectionChange?.();
	}

	function onDocumentMouseDown(event: MouseEvent) {
		if (!open || !root) return;
		if (!root.contains(event.target as Node)) {
			setOpen(false);
		}
	}

	function onKeyDown(event: KeyboardEvent) {
		if (event.key === 'Escape') setOpen(false);
	}

	onMount(() => {
		document.addEventListener('mousedown', onDocumentMouseDown);
		document.addEventListener('keydown', onKeyDown);
	});

	onDestroy(() => {
		document.removeEventListener('mousedown', onDocumentMouseDown);
		document.removeEventListener('keydown', onKeyDown);
	});
</script>

<div bind:this={root} class="relative w-full">
	<button
		type="button"
		on:click={toggleOpen}
		class="flex min-h-14 w-full flex-wrap items-center gap-1 rounded-lg bg-gray-100 px-3 py-2 text-left text-lg {invalid
			? 'border-2 border-red-500'
			: 'border border-transparent'}"
	>
		{#if (value ?? []).length === 0}
			<span class="text-gray-400">{placeholder}</span>
		{:else}
			{#each value as item (item.id)}
				<span class="rounded bg-gray-200 px-2 py-0.5 text-sm">{labelFor(item)}</span>
			{/each}
		{/if}
	</button>
	{#if open}
		<ul
			class="absolute left-0 right-0 top-full z-50 mt-1 max-h-60 overflow-auto rounded-lg border border-gray-200 bg-white py-1 shadow-lg"
		>
			{#each options as option (option.id)}
				<li>
					<button
						type="button"
						class="flex w-full items-center px-3 py-2 text-left text-sm hover:bg-gray-100 {selectedIdSet.has(
							option.id
						)
							? 'bg-blue-50 font-medium'
							: ''}"
						on:click={() => toggleOption(option)}
					>
						<span
							class="mr-2 inline-flex h-4 w-4 items-center justify-center rounded border {selectedIdSet.has(
								option.id
							)
								? 'border-blue-600 bg-blue-600 text-white'
								: 'border-gray-400 bg-white'}"
							aria-hidden="true"
						>
							{#if selectedIdSet.has(option.id)}✓{/if}
						</span>
						{labelFor(option)}
					</button>
				</li>
			{/each}
			{#if options.length === 0}
				<li class="px-3 py-2 text-sm text-gray-400">Geen voedingsbodems</li>
			{/if}
		</ul>
	{/if}
</div>
