<script lang="ts">
	import { goto } from '$app/navigation';
	import logoLabflow from '$lib/assets/labflowLogoTM.svg';
	// @ts-ignore
	import GoPerson from 'svelte-icons/go/GoPerson.svelte';
	// @ts-ignore
	import GoOrganization from 'svelte-icons/go/GoOrganization.svelte';
	import { getRolNaam_FromToken } from '$lib/globalFunctions';

	const rol = getRolNaam_FromToken();

	function eraseCookie() {
		document.cookie = 'authToken=;expires=' + new Date(0).toUTCString();
	}

	function logout() {
		goto('/');
	}
</script>

<div class="py-5 px-10 flex justify-between items-center">
	<!-- logo links -->
	<img src={logoLabflow} alt="logo Labflow & Thomas More" class="h-10" />

	<!-- navigatie-items rechts -->
	<div class="flex items-center space-x-4">
		<a
			href="/menu"
			class="bg-blue-500 hover:bg-blue-600 text-white text-sm font-medium px-4 py-2 rounded-lg transition"
		>
			Menu
		</a>
		<button
			on:click={() => {
				logout();
				eraseCookie();
			}}
			class="bg-blue-500 hover:bg-blue-600 text-white text-sm font-medium px-4 py-2 rounded-lg transition"
		>
			Uitloggen
		</button>

		<!-- rol -->
		<div class="flex items-center space-x-2 bg-slate-100 rounded-lg px-3 py-1">
			<p class="text-sm font-medium text-gray-700 capitalize">{rol}</p>
			<div class="w-8 h-8 bg-slate-400 rounded-full flex items-center justify-center text-white">
				<div class="scale-75">
					{#if rol === 'student'}
						<GoPerson />
					{/if}
					{#if rol === 'admin'}
						<GoOrganization />
					{/if}
				</div>
			</div>
		</div>
	</div>
</div>
