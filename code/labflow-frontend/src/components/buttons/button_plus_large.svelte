<script lang="ts">
	// @ts-ignore
	import GoPlus from 'svelte-icons/go/GoPlus.svelte';
	import { staalCodeStore } from '$lib/store';
	import { goto } from '$app/navigation';

	export let label: string = 'Nieuwe staal';

	function setStore(staalCode: string) {
		staalCodeStore.set(staalCode);
		goto('stalen/nieuw');
	}
</script>

<a
	on:click={() => setStore('')}
	href="/stalen/nieuw"
	class="bg-blue-400 flex flex-col items-center justify-center w-56 h-56 rounded-2xl hover:bg-blue-500 transition"
>
	<div class="w-28 h-28 text-white flex items-center justify-center">
		<GoPlus />
	</div>
	<p class="text-white text-2xl text-center mt-2">{label}</p>
</a>
