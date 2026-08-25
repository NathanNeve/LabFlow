<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { fetchMicrobiologyAntibiotica } from '$lib/fetchFunctions';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';
	import { getCookie } from '$lib/globalFunctions';
	import type { MicrobiologyAntibiotica } from '$lib/types/dbTypes';

	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	let token: string = '';
	let searchCode = '';
	let items: MicrobiologyAntibiotica[] = [];
	let itemsSorted: MicrobiologyAntibiotica[] = [];
	let naam = '';
	let deleteError = '';
	let errorMessagePOST = '';
	let errorVeldenPOST = { naam: false };
	let errorMessagePUT = '';

	onMount(async () => {
		token = getCookie('authToken') || '';
		await reload();
	});

	async function reload() {
		const result = await fetchMicrobiologyAntibiotica();
		if (result) {
			[items, itemsSorted] = [result, result];
		}
	}

	function filterItems() {
		itemsSorted = items.filter((item) =>
			item.naam.toString().toLowerCase().includes(searchCode.toLowerCase())
		);
	}

	function verwijderZoek() {
		searchCode = '';
		itemsSorted = items;
	}

	async function deleteItem(id: number) {
		try {
			const response = await fetch(`${backend_path}/api/microbiology/antibiotica/${id}`, {
				method: 'DELETE',
				headers: {
					Authorization: 'Bearer ' + token
				}
			});
			if (response.ok) {
				deleteError = '';
				await reload();
			} else {
				deleteError =
					'Antibiotica kon niet worden verwijderd omdat deze gelinked is aan één of meerdere antibiogrammen.';
			}
		} catch (error) {
			console.error('Antibiotica kon niet worden verwijderd: ', error);
		}
	}

	async function nieuwItem() {
		errorVeldenPOST = { naam: false };
		if (!naam) {
			errorVeldenPOST.naam = true;
			errorMessagePOST = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			const response = await fetch(`${backend_path}/api/microbiology/antibiotica`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({ naam })
			});
			if (response.status === 409) {
				errorMessagePOST = 'Naam bestaat al.';
				return;
			}
			if (!response.ok) {
				errorMessagePOST = 'Antibiotica kon niet worden aangemaakt.';
				return;
			}
			naam = '';
			errorMessagePOST = '';
			await reload();
		} catch (error) {
			console.error('Antibiotica kon niet worden aangemaakt: ', error);
		}
	}

	async function updateItem(id: number) {
		const item = items.find((i) => i.id === id);
		if (!item) return;
		if (!item.naam) {
			errorMessagePUT = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			const response = await fetch(`${backend_path}/api/microbiology/antibiotica/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({ naam: item.naam })
			});
			if (response.status === 409) {
				errorMessagePUT = 'Naam bestaat al.';
				return;
			}
			errorMessagePUT = '';
		} catch (error) {
			console.error('Antibiotica kon niet worden aangepast: ', error);
		}
	}
</script>

<div class="flex flex-col w-full ml-5">
	<div class="flex flex-row justify-between w-full h-14 mb-5">
		<h1 class="font-bold text-3xl">Antibiotica beheren</h1>
		<button
			type="button"
			on:click={async () => {
				await goto('/microbiologie/stalen');
			}}
			class="bg-gray-400 text-xl rounded-lg p-3 text-white h-12 w-32 justify-center items-center flex"
		>
			<div class="w-4 h-4 mr-2"><FaArrowLeft /></div>
			<p>Terug</p>
		</button>
	</div>

	<div class="bg-slate-200 w-full h-full rounded-2xl p-5">
		<div class="flex mb-5 w-full">
			<input
				type="text"
				placeholder="zoeken"
				bind:value={searchCode}
				on:input={filterItems}
				class="w-2/5 h-12 rounded-l-lg text-black pl-3"
			/>
			<button
				on:click={verwijderZoek}
				class="w-12 h-12 p-4 flex items-center justify-center bg-red-500 hover:bg-red-600 text-white rounded-r-lg"
			>
				<GoX />
			</button>
		</div>
		<div class="space-y-3">
			{#if deleteError}
				<div class="text-red-500 mb-2">{deleteError}</div>
			{/if}
			<div class="grid grid-cols-7 bg-gray-300 rounded-lg h-10 items-center px-3 font-bold space-x-3">
				<div class="col-span-6 text-left">
					<p>Naam</p>
				</div>
				<div class="col-span-1 text-right">
					<p>Acties</p>
				</div>
			</div>
		</div>

		{#if errorMessagePOST}
			<div class="text-red-500 mb-2">{errorMessagePOST}</div>
		{/if}
		<div class="grid grid-cols-7 space-x-3 my-3 bg-white rounded-lg h-20 items-center px-3 shadow-md">
			<div class="col-span-6">
				<input
					type="text"
					bind:value={naam}
					placeholder="Naam van de antibiotica"
					class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenPOST.naam
						? 'border-2 border-red-500'
						: ''}"
				/>
			</div>
			<div class="col-span-1 flex justify-end">
				<button
					type="button"
					class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"
					on:click={nieuwItem}
					aria-label="Nieuwe antibiotica toevoegen"
				>
					<FaPlus />
				</button>
			</div>
		</div>
		{#if errorMessagePUT}
			<div class="text-red-500 mb-2">{errorMessagePUT}</div>
		{/if}
		<div class="space-y-3">
			{#each itemsSorted as item, index}
				<div class="grid grid-cols-7 bg-white rounded-lg h-20 items-center px-3 shadow-md space-x-3">
					<div class="col-span-6">
						<input
							type="text"
							on:blur={() => updateItem(item.id)}
							bind:value={item.naam}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						/>
					</div>
					<div class="col-span-1 flex justify-end">
						{#if item.confirmDelete}
							<button
								type="button"
								on:click={() => deleteItem(item.id)}
								class="h-10 w-10 bg-red-500 p-2 rounded-lg text-white"
							>
								<FaTrashAlt />
							</button>
						{:else}
							<button
								type="button"
								on:click={() => {
									items.forEach((c, i) => {
										if (i !== index) c.confirmDelete = false;
									});
									item.confirmDelete = true;
								}}
								class="h-10 w-10 bg-red-300 p-2 rounded-lg text-white"
							>
								<GoX />
							</button>
						{/if}
					</div>
				</div>
			{/each}
		</div>
	</div>
</div>
