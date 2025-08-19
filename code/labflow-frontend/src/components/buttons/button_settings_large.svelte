<script lang="ts">
	import { getRolNaam_FromToken } from '$lib/globalFunctions';
	// @ts-ignore
	import IoMdSettings from 'svelte-icons/io/IoMdSettings.svelte';
	let bgColor = 'bg-blue-400';
	let pointerEvent = 'pointer-events-auto';
	const rol = getRolNaam_FromToken();
	if (rol !== 'admin') {
		bgColor = 'bg-gray-400';
		pointerEvent = 'pointer-events-none';
	}

	export let label: string = 'Instellingen';
</script>

<a
	href={rol === 'admin' ? '/instellingen' : '#'}
	class="{bgColor} flex flex-col items-center justify-center w-56 h-56 rounded-2xl {pointerEvent}"
>
	<div class="w-28 h-28 text-white flex items-center justify-center">
		<IoMdSettings />
	</div>
	<p class="text-white text-2xl text-center mt-2">{label}</p>
</a>
