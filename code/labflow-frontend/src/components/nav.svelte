<script lang="ts">
	import { goto } from '$app/navigation';
	import logoLabflow from '$lib/assets/labflowLogo.svg';
	// @ts-ignore
	import GoPerson from 'svelte-icons/go/GoPerson.svelte';
	// @ts-ignore
	import GoOrganization from 'svelte-icons/go/GoOrganization.svelte';
	import { getRolNaam_FromToken } from '$lib/globalFunctions';
	import { onMount } from 'svelte';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	let rol: string;

	onMount(() => {
		rol = getRolNaam_FromToken();
		console.log('Rol:', rol);
	});

	const logout = async () => {
		// request logout -> sends expired cookie to delete session cookie
		await fetch(`${backend_path}/logout-user`, {
			method: 'POST',
			credentials: 'include' // include cookies in the request
		})
			.then(() => {
				// remove user data from session storage
				sessionStorage.removeItem('Role');
				sessionStorage.removeItem('UserId');

				// redirect to home page
				goto('/');
			})
			.catch((error) => {
				console.error('Logout failed:', error);
			});
	};
</script>

<div class="py-5 px-10 flex justify-between items-center">
	<img src={logoLabflow} alt="logo Labflow & Thomas More" class="h-10" />
	<div class="w-28 h-10 flex justify-between items-center">
		<div class="flex flex-col justify-between h-full">
			{#if rol == '"student"'}
				<p class="text-sm">student</p>
			{/if}
			{#if rol == '"admin"'}
				<p class="text-sm">admin</p>
			{/if}
			<button class="text-xs underline" on:click={logout} type="button"> Uitloggen </button>
		</div>
		<div class="w-8 h-8 p-1 bg-slate-400 rounded-full flex items-center justify-center mr-1">
			{#if rol == '"student"'}
				<GoPerson />
			{/if}
			{#if rol == '"admin"'}
				<GoOrganization />
			{/if}
		</div>
	</div>
</div>
