<script lang="ts">
	import ButtonTerug from '../buttons/button_terug.svelte';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	import { onMount } from 'svelte';
	import { fetchUsers } from '$lib/fetchFunctions';
	import { getCookie } from '$lib/globalFunctions';
	import { fetchRollen } from '$lib/fetchFunctions';
	import { getUserId } from '$lib/globalFunctions';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;
	// types
	import type { Rol, User } from '$lib/types/dbTypes';

	let token: string = '';

	let users: User[] = [];
	let usersSorted: User[] = [];
	let searchCode = '';
	let rollen: Rol[] = [];
	const userId = getUserId();

	onMount(async () => {
		token = getCookie('authToken') || '';
		const resultUsers = await fetchUsers();
		if (resultUsers) {
			[users, usersSorted] = [resultUsers, resultUsers].map((userList: User[]) =>
				userList.map((user) => {
					user.confirmDelete = false;
					return user;
				})
			);
		}
		const resultRollen = await fetchRollen();
		if (resultRollen) {
			rollen = resultRollen;
		}
	});

	function filterUsers() {
		usersSorted = users.filter((user) => {
			const codeMatch =
				user.fullName.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				user.email.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				user.rol.naam.toString().toLowerCase().includes(searchCode.toLowerCase());
			return codeMatch;
		});
	}

	function verwijderZoek() {
		searchCode = '';
		usersSorted = users;
	}

	///// DELETE verwijderen van een gebruiker /////
	let errorMessageGebruikerDELETE = '';
	async function deleteUser(id: string) {
		if (id === '1' || id === '2') {
			errorMessageGebruikerDELETE = 'Deze gebruiker kan niet worden verwijderd.';
			return;
		} else {
			try {
				const response = await fetch(`${backend_path}/deleteuser/${id}`, {
					method: 'DELETE',
					headers: {
						'Content-Type': 'application/json',
						Authorization: 'Bearer ' + token
					}
				});
				const result = await fetchUsers();
				if (result) {
					[users, usersSorted] = [result, result];
				}
			} catch (error) {
				console.error('Gebruiker kon niet worden verwijderd: ', error);
			}
		}
	}

	///// POST aanmaken van een gebruiker /////
	let voornaam = '';
	let achternaam = '';
	let email = '';
	let wachtwoord = '';
	let rol = '';

	let errorVeldenGebruikerPOST = {
		voornaam: false,
		achternaam: false,
		email: false,
		wachtwoord: false,
		rol: false
	};

	let errorMessageGebruikerPOST = '';

	async function nieuweGebruiker() {
		errorVeldenGebruikerPOST = {
			voornaam: false,
			achternaam: false,
			email: false,
			wachtwoord: false,
			rol: false
		};
		let isValid = true;
		if (!voornaam) {
			errorVeldenGebruikerPOST.voornaam = true;
			isValid = false;
		}
		if (!achternaam) {
			errorVeldenGebruikerPOST.achternaam = true;
			isValid = false;
		}
		if (!email) {
			errorVeldenGebruikerPOST.email = true;
			isValid = false;
		}
		if (!wachtwoord) {
			errorVeldenGebruikerPOST.wachtwoord = true;
			isValid = false;
		}
		if (!rol) {
			errorVeldenGebruikerPOST.rol = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessageGebruikerPOST = 'Vul alle verplichte velden in.';
			return;
		}

		try {
			await fetch(`${backend_path}/register`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					voorNaam: voornaam,
					achterNaam: achternaam,
					email: email,
					wachtwoord: wachtwoord,
					rol: {
						id: rol
					}
				})
			});
			voornaam = '';
			achternaam = '';
			email = '';
			wachtwoord = '';
			rol = '';
		} catch (error) {
			console.error('Gebruiker kon niet worden aangemaakt: ', error);
		}
		const result = await fetchUsers();
		if (result) {
			[users, usersSorted] = [result, result];
		}
		return;
	}

	///// PUT aanpassen van een gebruiker /////
	let errorMessageGebruikerPUT = '';

	let errorVeldenGebruikerPUT = {
		voornaam: false,
		achternaam: false,
		email: false,
		wachtwoord: false,
		rol: false
	};

	async function updateGebruiker(id: number, newWachtwoord: string | null | undefined) {
		const user = users.find((u) => u.id === id);
		if (!user) return;

		let isValid = true;
		errorVeldenGebruikerPUT = {
			voornaam: false,
			achternaam: false,
			email: false,
			wachtwoord: false,
			rol: false
		};

		if (!user.voorNaam) {
			errorVeldenGebruikerPUT.voornaam = true;
			isValid = false;
		}
		if (!user.achterNaam) {
			errorVeldenGebruikerPUT.achternaam = true;
			isValid = false;
		}
		if (!user.email) {
			errorVeldenGebruikerPUT.email = true;
			isValid = false;
		}
		if (!user.rol) {
			errorVeldenGebruikerPUT.rol = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessageGebruikerPUT = 'Vul alle verplichte velden in.';
			return;
		}

		try {
			// If newWachtwoord is provided and not empty, update with new password
			if (newWachtwoord && newWachtwoord.trim() !== '') {
				await fetch(`${backend_path}/updateuser/${id}`, {
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json',
						Authorization: 'Bearer ' + token
					},
					body: JSON.stringify({
						wachtwoord: newWachtwoord,
						email: user.email,
						voorNaam: user.voorNaam,
						achterNaam: user.achterNaam,
						rol: {
							id: user.rol.id
						}
					})
				});
			} else {
				// If no new password, update other fields without changing the password
				await fetch(`${backend_path}/updateuserwithoutpassword/${id}`, {
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json',
						Authorization: 'Bearer ' + token
					},
					body: JSON.stringify({
						email: user.email,
						voorNaam: user.voorNaam,
						achterNaam: user.achterNaam,
						rol: {
							id: user.rol.id
						}
					})
				});
			}

			errorMessageGebruikerPUT = '';
			if (user) {
				user.newWachtwoord = null;
			}
			const result = await fetchUsers();
			if (result) {
				[users, usersSorted] = [result, result];
			}
		} catch (error) {
			console.error('Gebruiker kon niet aangepast: ', error);
		}
	}
</script>

<div class="flex flex-col w-full ml-5 mb-10">
	<div class="flex flex-row justify-between w-full h-14 mb-5">
		<h1 class="font-bold text-3xl">Gebruikers beheren</h1>
		<ButtonTerug />
	</div>

	<div class="bg-slate-200 w-full h-full rounded-2xl p-5">
		<div class="space-y-3">
			<div class="flex mb-5 w-full">
				<input
					type="text"
					id="searchCode"
					name="searchCode"
					placeholder="zoeken"
					bind:value={searchCode}
					on:input={filterUsers}
					class="w-2/5 h-12 rounded-l-lg text-black pl-3"
				/>
				<button
					on:click={verwijderZoek}
					class="w-12 h-12 p-4 flex items-center justify-center bg-red-500 hover:bg-red-600 text-white rounded-r-lg"
				>
					<GoX />
				</button>
			</div>
			<!-- Header -->
			<div class="grid grid-cols-12 gap-4 bg-gray-300 rounded-lg h-10 items-center px-3 font-bold">
				<div class="col-span-2">
					<p>Voornaam</p>
				</div>
				<div class="col-span-2 text-left">
					<p>Achternaam</p>
				</div>
				<div class="col-span-3 text-left">
					<p>Email</p>
				</div>
				<div class="col-span-2 text-left">
					<p>Wachtwoord</p>
				</div>
				<div class="col-span-2 text-left">
					<p>Rol</p>
				</div>
				<div class="col-span-1 text-right">
					<p>Acties</p>
				</div>
			</div>

			{#if errorMessageGebruikerPOST}
				<div class="text-red-500 mb-2">{errorMessageGebruikerPOST}</div>
			{/if}
			{#if errorMessageGebruikerDELETE}
				<div class="text-red-500 mb-2">{errorMessageGebruikerDELETE}</div>
			{/if}
			<form
				class="grid grid-cols-12 bg-white rounded-lg h-20 items-center px-3 shadow-md space-x-3"
			>
				<div class="col-span-2">
					<input
						type="text"
						id="voornaam"
						bind:value={voornaam}
						placeholder="Voornaam gebruiker"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenGebruikerPOST.voornaam ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-2">
					<input
						type="text"
						id="achternaam"
						bind:value={achternaam}
						placeholder="Achternaam gebruiker"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenGebruikerPOST.achternaam ? 'border-2 border-red-500' : ''}"
					/>
				</div>
				<div class="col-span-3">
					<input
						type="text"
						id="email"
						bind:value={email}
						placeholder="Email gebruiker"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenGebruikerPOST.email ? 'border-2 border-red-500' : ''}"
						autocomplete="username"
					/>
				</div>
				<div class="col-span-2">
					<input
						type="password"
						id="paswoord"
						bind:value={wachtwoord}
						placeholder="Wachtwoord gebruiker"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
                    {errorVeldenGebruikerPOST.wachtwoord ? 'border-2 border-red-500' : ''}"
						autocomplete="current-password"
					/>
				</div>
				<div class="flex flex-col justify-center col-span-2">
					<div>
						{#each rollen as rolItem (rolItem.id)}
							<label
								class="container mr-5 {errorVeldenGebruikerPOST.rol
									? 'text-red-500 font-bold'
									: ''}"
							>
								<input type="radio" name="radio" bind:group={rol} value={rolItem.id} />
								{rolItem.naam}
							</label>
						{/each}
					</div>
				</div>
				<!-- Acties -->
				<div class="col-span-1 flex justify-end">
					<button
						type="button"
						class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"
						on:click={nieuweGebruiker}
						aria-label="Nieuwe categorie toevoegen"
					>
						<FaPlus />
					</button>
				</div>
			</form>
			{#if errorMessageGebruikerPUT}
				<div class="text-red-500 mb-2">{errorMessageGebruikerPUT}</div>
			{/if}
			<div class="space-y-3">
				{#each usersSorted as user, index}
					<form
						class="grid grid-cols-12 bg-white rounded-lg h-20 items-center px-3 shadow-md space-x-3"
					>
						<div class="col-span-2">
							<input
								type="text"
								on:blur={() => updateGebruiker(user.id, user.newWachtwoord)}
								bind:value={user.voorNaam}
								disabled={user?.id === 1}
								class="{user?.id === 1
									? 'bg-white'
									: 'bg-gray-100'} rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>
						<div class="col-span-2">
							<input
								type="text"
								on:blur={() => updateGebruiker(user.id, user.newWachtwoord)}
								bind:value={user.achterNaam}
								disabled={user?.id === 1}
								class="{user?.id === 1
									? 'bg-white'
									: 'bg-gray-100'} rounded-lg h-14 text-lg pl-3 w-full"
							/>
						</div>
						<div class="col-span-3">
							<input
								type="text"
								on:blur={() => updateGebruiker(user.id, user.newWachtwoord)}
								bind:value={user.email}
								disabled={user?.id === 1}
								class="{user?.id === 1
									? 'bg-white'
									: 'bg-gray-100'} rounded-lg h-14 text-lg pl-3 w-full"
								autocomplete="username"
							/>
						</div>
						<div class="col-span-2">
							{#if userId === '1'}
								<input
									type="password"
									on:blur={() => updateGebruiker(user.id, user.newWachtwoord)}
									bind:value={user.newWachtwoord}
									disabled={user?.id === 1}
									class="{user?.id === 1
										? 'bg-gray-50'
										: 'bg-gray-100'} rounded-lg h-14 text-lg pl-3 w-full"
									autocomplete="current-password"
								/>
							{/if}
							{#if userId !== '1'}
								<input
									type="password"
									on:blur={() => updateGebruiker(user.id, user.newWachtwoord)}
									bind:value={user.newWachtwoord}
									disabled={user?.id === 1 || user?.id === 2 || user?.id === 3}
									class="{user?.id === 1 || user?.id === 2 || user?.id === 3
										? 'bg-gray-50'
										: 'bg-gray-100'} rounded-lg h-14 text-lg pl-3 w-full"
									autocomplete="current-password"
								/>
							{/if}
						</div>
						<div class="col-span-2">
							<div>
								{#each rollen as rolItem (rolItem.id)}
									<label
										class="container mr-5 {errorVeldenGebruikerPUT.rol
											? 'text-red-500 font-bold'
											: ''}"
									>
										<!-- value komt van de rollen en de bind group is zoals andere imputs in de form gebind aan de user -->
										<input
											type="radio"
											disabled={user?.id === 1 || user?.id === 2 || user?.id === 3}
											name={`rol-${user.id}`}
											bind:group={user.rol.id}
											value={rolItem.id}
											on:blur={() => updateGebruiker(user.id, user.newWachtwoord)}
										/>
										{rolItem.naam}
									</label>
								{/each}
							</div>
						</div>

						<!-- Acties -->
						<div class="col-span-1 flex justify-end">
							{#if user.confirmDelete}
								<!-- parsen van id als string voor deleteUser -->
								<button
									type="button"
									on:click={() => deleteUser(String(user?.id))}
									class="h-10 w-10 bg-red-500 p-2 rounded-lg text-white
									{user?.id === 1 || user?.id === 2 || user?.id === 3 ? 'hidden' : ''}"
								>
									<FaTrashAlt />
								</button>
							{:else}
								<button
									type="button"
									on:click={() => {
										users.forEach((u, i) => {
											if (i !== index) u.confirmDelete = false;
										});
										user.confirmDelete = true;
									}}
									class="h-10 w-10 bg-red-300 p-2 rounded-lg text-white
									{user?.id === 1 || user?.id === 2 || user?.id === 3 ? 'hidden' : ''}"
								>
									<GoX />
								</button>
							{/if}
						</div>
					</form>
				{/each}
			</div>
		</div>
	</div>
</div>
