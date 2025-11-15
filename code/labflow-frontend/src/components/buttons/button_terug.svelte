<script lang="ts">
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	import { goto } from '$app/navigation';
	export let label: string = '/stalen';
    export let text: string = 'Terug';
	export let width: string = '8rem';
	export let height: string = '3rem';
</script>

<button
	type="button"
	on:click={() => goto(label)}
	style="width: {width}; height: {height};"
	class="bg-gray-400 text-xl rounded-lg p-3 text-white justify-center items-center flex"
>
	<div class="w-4 h-4 mr-2"><FaArrowLeft /></div>
	<p>{text}</p>
</button>
