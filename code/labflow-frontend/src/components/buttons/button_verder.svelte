<script lang="ts">
	// @ts-ignore
	import FaArrowRight from 'svelte-icons/fa/FaArrowRight.svelte';
	import { createEventDispatcher } from 'svelte';
	import { goto } from '$app/navigation';

	export let width: string = '8rem';
	export let height: string = '3rem';
	export let disabled: boolean = false;
	export let text: string = 'Volgende';
	export let label: string = '';

	const dispatch = createEventDispatcher();

	function handleClick(event: MouseEvent) {
		if (label) {
			goto(label);
		}
		dispatch('click', event);
	}
</script>

<button
	class="bg-blue-600 text-xl rounded-lg p-3 text-white flex flex-row items-center justify-center"
	style="width: {width}; height: {height};"
	{...$$restProps}
	{disabled}
	on:click={handleClick}
>
	{text}
	<div class="w-4 h-4 ml-2"><FaArrowRight /></div>
</button>