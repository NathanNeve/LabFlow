<script>
	import Nav from '../../../components/nav.svelte';
	// @ts-ignore
	import GiHypodermicTest from 'svelte-icons/gi/GiHypodermicTest.svelte';
	// @ts-ignore
	import FaWrench from 'svelte-icons/fa/FaWrench.svelte';
	// @ts-ignore
	import GiTestTubes from 'svelte-icons/gi/GiTestTubes.svelte';
	// @ts-ignore
	import FaClipboardList from 'svelte-icons/fa/FaClipboardList.svelte';
	import TestsBeheren from '../../../components/Microbiologie/Instellingen/TestsBeheren.svelte';
	import AntibioticaBeheren from '../../../components/Microbiologie/Instellingen/AntibioticaBeheren.svelte';
	import VoedingsbodemsBeheren from '../../../components/Microbiologie/Instellingen/VoedingsbodemsBeheren.svelte';
	import StaalTypenBeheren from '../../../components/Microbiologie/Instellingen/StaalTypenBeheren.svelte';

	let navElement = 'Test';
</script>

<Nav />
<div class="px-8 flex flex-row">
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
			on:click={() => (navElement = 'Antibiotica')}
			class="p-2 text-center text-md {navElement == 'Antibiotica'
				? 'bg-blue-500'
				: 'bg-blue-400'} flex flex-col items-center justify-center w-32 h-32 rounded-2xl text-white"
		>
			<div class="w-12 h-12 mb-3"><FaWrench /></div>
			Antibiotica
		</button>
		<button
			type="button"
			on:click={() => (navElement = 'Voedingsbodem')}
			class="p-2 text-center text-md {navElement == 'Voedingsbodem'
				? 'bg-blue-500'
				: 'bg-blue-400'} flex flex-col items-center justify-center w-32 h-32 rounded-2xl text-white"
		>
			<div class="w-12 h-12 mb-3"><GiTestTubes /></div>
			Voedingsbodems
		</button>
		<button
			type="button"
			on:click={() => (navElement = 'Staaltype')}
			class="p-2 text-center text-md {navElement == 'Staaltype'
				? 'bg-blue-500'
				: 'bg-blue-400'} flex flex-col items-center justify-center w-32 h-32 rounded-2xl text-white"
		>
			<div class="w-12 h-12 mb-3"><FaClipboardList /></div>
			Staaltypen
		</button>

		<div class="mb-10"></div>
	</div>

	{#if navElement === 'Test'}
		<TestsBeheren />
	{:else if navElement === 'Antibiotica'}
		<AntibioticaBeheren />
	{:else if navElement === 'Voedingsbodem'}
		<VoedingsbodemsBeheren />
	{:else if navElement === 'Staaltype'}
		<StaalTypenBeheren />
	{/if}
</div>
