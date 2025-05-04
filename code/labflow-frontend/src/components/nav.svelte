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
		// https://stackoverflow.com/questions/2144386/how-to-delete-a-cookie
		document.cookie = 'authToken=;expires=' + new Date(0).toUTCString();
	}

	function logout() {
		goto('/');
	}
</script>

<div class="py-5 px-10 flex justify-between items-center">
	<img src={logoLabflow} alt="logo Labflow & Thomas More" class="h-10" />
	<div class="w-28 h-10 flex justify-between items-center">
		<div class="flex flex-col justify-between h-full">
			{#if rol === 'student'}
				<p class="text-sm">student</p>
			{/if}
			{#if rol === 'admin'}
				<p class="text-sm">admin</p>
			{/if}
			<button
				class="text-xs underline"
				on:click={() => {
					logout();
					eraseCookie();
				}}
				type="button"
			>
				Uitloggen
			</button>
		</div>
		<div class="w-8 h-8 p-1 bg-slate-400 rounded-full flex items-center justify-center mr-1">
			{#if rol === 'student'}
				<GoPerson />
			{/if}
			{#if rol === 'admin'}
				<GoOrganization />
			{/if}
		</div>
	</div>
</div>
