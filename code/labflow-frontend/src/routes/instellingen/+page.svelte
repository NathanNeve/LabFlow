<script>
	import Nav from '../../components/nav.svelte';
	// @ts-ignore
	import FaClipboardList from 'svelte-icons/fa/FaClipboardList.svelte';
	// @ts-ignore
	import GiTestTubes from 'svelte-icons/gi/GiTestTubes.svelte';
	// @ts-ignore
	import GiHypodermicTest from 'svelte-icons/gi/GiHypodermicTest.svelte';
	// @ts-ignore
	import FaWrench from 'svelte-icons/fa/FaWrench.svelte';
	import CategorieënBeheren from '../../components/Instellingen/CategorieënBeheren.svelte';
	import StalenBeheren from '../../components/Instellingen/StalenBeheren.svelte';
	import TestsBeheren from '../../components/Instellingen/Testsbeheren.svelte';
	import EenhedenBeheren from '../../components/Instellingen/EenhedenBeheren.svelte';

	// default gerenderde component
	let navElement = 'Test';
</script>

<Nav />
<div class="px-8 flex flex-row">
	<!-- 4 knoppen -->
	<div class="flex flex-col space-y-3 w-32 mt-[74px]">
		<button
			type="button"
			on:click={() => (navElement = 'Test')}
			class="p-2 text-center text-md {navElement == 'Test'
				? 'bg-blue-500'
				: 'bg-blue-400'} flex flex-col items-center justify-center w-32 h-32 rounded-2xl text-white"
		>
			<div class="w-12 h-12 mb-3"><GiHypodermicTest /></div>
			Testen
		</button>
		<button
			type="button"
			on:click={() => (navElement = 'Staal')}
			class="p-2 text-center text-md {navElement == 'Staal'
				? 'bg-blue-500'
				: 'bg-blue-400'} flex flex-col items-center justify-center w-32 h-32 rounded-2xl text-white"
		>
			<div class="w-12 h-12 mb-3"><GiTestTubes /></div>
			Stalen
		</button>
		<button
			type="button"
			on:click={() => (navElement = 'Categorie')}
			class="p-2 text-center text-md {navElement == 'Categorie'
				? 'bg-blue-500'
				: 'bg-blue-400'} flex flex-col items-center justify-center w-32 h-32 rounded-2xl text-white"
		>
			<div class="w-12 h-12 mb-3"><FaClipboardList /></div>
			Categorieën
		</button>
		<button
			type="button"
			on:click={() => (navElement = 'Eenheid')}
			class="p-2 text-center text-md {navElement == 'Eenheid'
				? 'bg-blue-500'
				: 'bg-blue-400'} flex flex-col items-center justify-center w-32 h-32 rounded-2xl text-white"
		>
			<div class="w-12 h-12 mb-3"><FaWrench /></div>
			Eenheden
		</button>

		<!-- spacing -->
		<div class="mb-10"></div>
	</div>

	<!-- content -->
	{#if navElement === 'Categorie'}
		<CategorieënBeheren />
	{:else if navElement === 'Staal'}
		<StalenBeheren />
	{:else if navElement === 'Test'}
		<TestsBeheren />
	{:else if navElement === 'Eenheid'}
		<EenhedenBeheren />
	{/if}
</div>
