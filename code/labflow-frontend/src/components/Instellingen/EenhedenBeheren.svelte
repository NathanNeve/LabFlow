<script lang="ts">
	import { goto } from '$app/navigation';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	import { fetchEenheden } from '$lib/fetchFunctions';
	import { onMount } from 'svelte';
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;
	// types
	import type { Eenheid } from '$lib/types/dbTypes';
	import { generalFetch } from '$lib/globalFunctions';

	let errorMessageEenheid = '';
	let searchCode = '';
	let eenheden: Eenheid[] = [];
	let eenhedenSorted: Eenheid[] = [];

	onMount(async () => {
		const fetchedEenheden = await fetchEenheden();
		if (fetchedEenheden) {
			[eenheden, eenhedenSorted] = [fetchedEenheden, fetchedEenheden];
		}
	});

	function filterEenheden() {
		eenhedenSorted = eenheden.filter((eenheid) => {
			const codeMatch =
				eenheid.naam.toString().toLowerCase().includes(searchCode.toLowerCase()) ||
				eenheid.afkorting.toString().toLowerCase().includes(searchCode.toLowerCase());
			return codeMatch;
		});
	}

	function verwijderZoek() {
		searchCode = '';
		eenhedenSorted = eenheden;
	}

	///// DELETE eenheid /////
	let deleteError = '';
	async function deleteEenheid(id: number) {
		try {
			await generalFetch('DELETE', 'deleteeenheid', true, id);

			deleteError = '';
			const result = await fetchEenheden();
			if (result) {
				[eenheden, eenhedenSorted] = [result, result];
			}
		} catch (error) {
			console.error('Eenheid kon niet worden verwijderd: ', error);
		}
	}

	///// POST eenheid /////
	let naam = '';
	let afkorting = '';

	let errorVeldenEenheidPOST = {
		naam: false,
		afkorting: false
	};

	let errorMessageTestPOST = '';
	async function nieuweEenheid() {
		// Resetten van de errorvelden
		errorVeldenEenheidPOST = {
			naam: false,
			afkorting: false
		};
		let isValid = true;
		if (!naam) {
			errorVeldenEenheidPOST.naam = true;
			isValid = false;
		}
		if (!afkorting) {
			errorVeldenEenheidPOST.afkorting = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessageTestPOST = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			await generalFetch('POST', 'createeenheid', true, undefined, {
				naam: naam,
				afkorting: afkorting
			});
			naam = '';
			afkorting = '';
			errorMessageTestPOST = '';
			const result = await fetchEenheden();
			if (result) {
				[eenheden, eenhedenSorted] = [result, result];
			}
		} catch (error) {
			console.error('Eenheid kon niet worden aangemaakt: ', error);
		}
		return;
	}

	///// PUT eenheid /////
	let errorVeldenEenheidPUT = {
		naam: false,
		afkorting: false
	};

	let errorMessageEenheidPUT = '';

	async function updateEenheid(id: number) {
		const eenheid = eenheden.find((e) => e.id === id);
		if (!eenheid) return;

		let isValid = true;

		errorVeldenEenheidPUT = {
			naam: false,
			afkorting: false
		};

		if (!eenheid.naam) {
			errorVeldenEenheidPUT.naam = true;
			isValid = false;
		}
		if (!eenheid.afkorting) {
			errorVeldenEenheidPUT.afkorting = true;
		}
		if (!isValid) {
			errorMessageEenheidPUT = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			// await fetch(`${backend_path}/api/updateeenheid/${id}`, {
			// 	method: 'PUT',
			// 	headers: {
			// 		'Content-Type': 'application/json'
			// 	},
			// 	credentials: 'include',
			// 	body: JSON.stringify({
			// 		naam: eenheid.naam,
			// 		afkorting: eenheid.afkorting
			// 	})
			// });
			await generalFetch('PUT', 'updateeenheid', true, id, {
				naam: eenheid.naam,
				afkorting: eenheid.afkorting
			});
			errorMessageEenheidPUT = '';
		} catch (error) {
			console.error('Eenheid kon niet worden aangepast: ', error);
		}
		return;
	}
</script>

<div class="flex flex-col w-full ml-5">
	<div class="flex flex-row justify-between w-full h-14 mb-5">
		<h1 class="font-bold text-3xl">Eenheden beheren</h1>
		<button
			type="button"
			on:click={async () => {
				await goto('/stalen');
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
				id="searchCode"
				name="searchCode"
				placeholder="zoeken"
				bind:value={searchCode}
				on:input={filterEenheden}
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
			<!-- Header -->
			<div
				class="grid grid-cols-7 bg-gray-300 rounded-lg h-10 items-center px-3 font-bold space-x-3"
			>
				<div class="col-span-3 text-left">
					<p>Naam</p>
				</div>
				<div class="col-span-3 text-left">
					<p>Afkorting</p>
				</div>
				<div class="col-span-1 text-right">
					<p>Acties</p>
				</div>
			</div>
			{#if errorMessageEenheid}
				<div class="text-red-500 mb-2">{errorMessageEenheid}</div>
			{/if}
		</div>

		{#if errorMessageTestPOST}
			<div class="text-red-500 mb-2">{errorMessageTestPOST}</div>
		{/if}
		<div
			class="grid grid-cols-7 space-x-3 my-3 bg-white rounded-lg h-20 items-center px-3 shadow-md"
		>
			<div class="col-span-3">
				<input
					type="text"
					id="naam"
					bind:value={naam}
					placeholder="Naam van de eenheid"
					class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
				{errorVeldenEenheidPOST.naam ? 'border-2 border-red-500' : ''}"
				/>
			</div>
			<div class="col-span-3">
				<input
					type="text"
					id="afkorting"
					bind:value={afkorting}
					placeholder="Afkorting van de eenheid"
					class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full
					{errorVeldenEenheidPOST.afkorting ? 'border-2 border-red-500' : ''}"
				/>
			</div>

			<!-- Add button -->
			<div class="col-span-1 flex justify-end">
				<button
					type="button"
					class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"
					on:click={nieuweEenheid}
					aria-label="Nieuwe categorie toevoegen"
				>
					<FaPlus />
				</button>
			</div>
		</div>
		<div class="space-y-3">
			{#each eenhedenSorted as eenheid, index}
				<div
					class="grid grid-cols-7 bg-white rounded-lg h-20 items-center px-3 shadow-md space-x-3"
				>
					<div class="col-span-3">
						<input
							type="text"
							on:blur={() => updateEenheid(eenheid.id)}
							id="eenheid-{eenheid?.id}"
							bind:value={eenheid.naam}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						/>
					</div>
					<div class="col-span-3">
						<input
							type="text"
							on:blur={() => updateEenheid(eenheid.id)}
							id="eenheid-{eenheid?.id}"
							bind:value={eenheid.afkorting}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						/>
					</div>

					<!-- Acties -->
					<div class="col-span-1 flex justify-end">
						{#if eenheid.confirmDelete}
							<button
								type="button"
								on:click={() => deleteEenheid(eenheid?.id)}
								class="h-10 w-10 bg-red-500 p-2 rounded-lg text-white"
							>
								<FaTrashAlt />
							</button>
						{:else}
							<button
								type="button"
								on:click={() => {
									eenheden.forEach((c, i) => {
										if (i !== index) c.confirmDelete = false;
									});
									eenheid.confirmDelete = true;
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
