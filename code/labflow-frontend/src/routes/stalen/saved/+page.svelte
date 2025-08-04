<script lang="ts">
	import Nav from '../../../components/nav.svelte';
	// @ts-ignore
	import FaArrowRight from 'svelte-icons/fa/FaArrowRight.svelte';
	// @ts-ignore
	import FaSave from 'svelte-icons/fa/FaSave.svelte';
	import { goto } from '$app/navigation';
	import { staalCodeStore } from '$lib/store';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	async function setStatusStaal() {
		let sampleCode: string | undefined;
		staalCodeStore.subscribe((value) => {
			sampleCode = value;
		});

		// leave normal fetch as is, generalFetch does not support PATCH method
		await fetch(`${backend_path}/api/updatestaalstatus/GEREGISTREERD/${sampleCode}`, {
			method: 'PATCH',
			credentials: 'include'
		});
	}
</script>

<Nav />
<main class="flex flex-col justify-center items-center h-[85vh]">
	<!-- Big icon -->
	<div class="text-blue-500">
		<FaSave />
	</div>
	<!-- text -->
	<h1 class=" text-8xl mb-5 text-blue-500">Analyseer Je Stalen</h1>
	<p class="text-2xl text-gray-600">
		Je testen zijn geregistreerd. Je kan ze terugvinden op het beginscherm.
	</p>
	<!-- navigation buttons -->
	<div class="flex justify-center space-x-4 items-center my-6">
		<button
			on:click={() => {
				setStatusStaal();
				goto('/stalen/registreren');
			}}
			class="bg-gray-400 text-lg rounded-lg p-3 w-56 text-white h-14 flex flex-row items-center justify-center"
		>
			Waarden Registreren
		</button>
		<!-- async functie omdat de status van de staal moet worden aangepast voordat de gebruiker naar de homepagina wordt geleid -->
		<button
			on:click={async () => {
				await setStatusStaal();
				goto('/stalen');
			}}
			class="bg-blue-600 text-lg rounded-lg w-56 p-3 text-white h-14 flex flex-row items-center justify-center"
		>
			Home
			<div class="w-5 h-5 ml-2"><FaArrowRight /></div>
		</button>
	</div>
</main>
