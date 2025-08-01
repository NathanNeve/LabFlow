<script lang="ts">
	import Button from './Button.svelte';
	import Input from './Input.svelte';
	import { goto } from '$app/navigation'; // https://www.okupter.com/blog/sveltekit-goto
	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	let email = '';
	let wachtwoord = '';

	const handleSubmit = async (event: SubmitEvent) => {
		// SubmitEvent is type of event
		event.preventDefault();
		const response = await fetch(`${backend_path}/login`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			credentials: 'include',
			body: JSON.stringify({ email, wachtwoord })
		});

		if (response.ok) {
			goto('stalen');
		} else {
			displayError = true;
			console.error('Login failed:', response.status, response.statusText);
			return;
		}
	};

	let displayError = false;
</script>

<form class="w-4/6 bg-gray-100 rounded-lg p-8 flex flex-col" on:submit={handleSubmit}>
	<div class="flex justify-between">
		<h1 class="text-gray-900 text-xl font-bold title-font mb-5">Log in met je gegevens</h1>
	</div>
	{#if displayError === true}
		<p class="text-red-500">Deze combinatie van email en wachtwoord bestaat niet.</p>
	{/if}

	<Input label="Email" type="text" required bind:value={email} autocomplete="username" />
	<Input
		label="Wachtwoord"
		type="password"
		required
		bind:value={wachtwoord}
		autocomplete="current-password"
	/>
	<Button type="submit">Aanmelden</Button>
</form>
