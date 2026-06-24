<script lang="ts">
	import Nav from '../../../../components/nav.svelte';
	import { goto } from '$app/navigation';
	import { microbiologyStaalIdStore } from '$lib/store';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import FaBook from 'svelte-icons/fa/FaBook.svelte';
	// @ts-ignore
	import FaSave from 'svelte-icons/fa/FaSave.svelte';
</script>

<Nav />
<main class="flex h-[85vh] flex-col items-center justify-center">
	<div class="text-blue-500">
		<FaSave />
	</div>
	<h1 class="mb-5 text-6xl font-bold text-blue-500 sm:text-8xl">Klaar met registratie</h1>
	<p class="max-w-2xl px-4 text-center text-xl text-gray-600">
		Je staal en labels zijn verwerkt. Je vindt het staal terug op het microbiologie-dashboard.
	</p>
	<div class="my-8 flex flex-wrap justify-center gap-4">
		<button
			type="button"
			class="flex h-14 w-56 flex-row items-center justify-center rounded-lg bg-gray-400 text-lg text-white"
			on:click={() => {
				microbiologyStaalIdStore.set('');
				goto('/microbiologie/stalen');
			}}
		>
			<div class="mr-2 h-5 w-5"><FaArrowLeft /></div>
			Terug naar dashboard
		</button>
		<button
			type="button"
			class="flex h-14 w-56 flex-row items-center justify-center rounded-lg bg-blue-600 text-lg text-white"
			on:click={() => goto('/microbiologie/stalen/notitieblok')}
		>
			<div class="mr-2 h-5 w-5"><FaBook /></div>
			Notitieblok
		</button>
	</div>
</main>
