<script lang="ts">
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';
	import { loadTestCategorieën } from '../../lib/fetchFunctions';
	import { onMount } from 'svelte';
	import ColorPicker, { ChromeVariant } from 'svelte-awesome-color-picker';
	import { getCookie } from '../../lib/globalFunctions';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;
	// types
	import type { TestCategorie } from '$lib/types/dbTypes';
	import ButtonTerug from '../buttons/button_terug.svelte';


	let searchCode = '';
	let token: string = '';
	let categorieën: TestCategorie[] = [];
	let categorieënSorted: TestCategorie[] = [];

	onMount(async () => {
		token = getCookie('authToken') || '';
		const result = await loadTestCategorieën();
		if (result) {
			categorieën = result;
			categorieënSorted = categorieën;
		}
	});

	function filterCategorien() {
		categorieënSorted = categorieën.filter((categorie) => {
			const codeMatch = categorie.naam.toString().toLowerCase().includes(searchCode.toLowerCase());
			return codeMatch;
		});
	}

	function verwijderZoek() {
		searchCode = '';
		categorieënSorted = categorieën;
	}

	///// DELETE verwijderen van een categorie /////
	let deleteError = '';
	async function deleteCategorie(id: number) {
		try {
			const response = await fetch(`${backend_path}/api/testcategorie/${id}`, {
				method: 'DELETE',
				headers: {
					Authorization: 'Bearer ' + token
				}
			});

			if (response.ok) {
				// If deletion is successful, reset error message and reload categories
				deleteError = '';
				const result = await loadTestCategorieën();
				if (result) {
					[categorieën, categorieënSorted] = [result, result];
				}
			} else {
				// If the server responds with an error (e.g., cannot delete because of linked tests)
				const errorMessage = await response.text();
				deleteError =
					'Categorie kon niet worden verwijderd omdat deze gelinked is aan één of meerdere tests.';
			}
		} catch (error) {
			console.error('Categorie kon niet worden verwijderd: ', error);
		}
	}

	///// POST aanmaken van een categorie /////
	let categorienaam = '';
	// dit geeft een warning in console en geeft de kleur transparant weer (dit is wat ik wil)
	let hex = '';
	let kleurnaam = '';

	let errorVeldenCategoriePOST = {
		categorienaam: false,
		kleur: false,
		kleurnaam: false
	};

	let errorMessageCategorie = '';
	async function nieuweCategorie() {
		errorVeldenCategoriePOST = { categorienaam: false, kleur: false, kleurnaam: false };
		let isValid = true;
		const regex = /^#([0-9A-F]{3}){1,2}$/i;

		if (!categorienaam) {
			errorVeldenCategoriePOST.categorienaam = true;
			isValid = false;
		}
		if (!hex || !regex.test(hex)) {
			errorVeldenCategoriePOST.kleur = true;
			isValid = false;
		}
		if (!kleurnaam) {
			errorVeldenCategoriePOST.kleurnaam = true;
			isValid = false;
		}
		// errorMessageStaal tonen indien niet alle velden zijn ingevuld
		if (!isValid) {
			errorMessageCategorie = 'Geef de categorie een naam en een kleur.';
			return;
		}

		try {
			const response = await fetch(`${backend_path}/api/createtestcategorie`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					naam: categorienaam,
					kleur: hex,
					kleurnaam: kleurnaam
				})
			});
			categorienaam = '';
			hex = '';
			kleurnaam = '';
			errorMessageCategorie = '';
			if (response.status === 409) {
				errorMessageCategorie = 'Deze categorie bestaat al.';
			}
		} catch (error) {
			console.error('categorie kon niet worden aangemaakt: ', error);
		}
		const result = await loadTestCategorieën();
		if (result) {
			[categorieën, categorieënSorted] = [result, result];
		}
		return;
	}

	///// PUT aanpassen van een categorie /////

	let errorVeldenCategoriePUT = {
		categorienaam: false,
		kleur: false,
		kleurnaam: false
	};

	let errorMessageCategoriePUT = '';

	async function updateCategorie(id: number) {
		const categorie = categorieën.find((c) => c.id === id);
		if (!categorie) return;

		errorVeldenCategoriePUT = { categorienaam: false, kleur: false, kleurnaam: false };
		let isValid = true;

		if (!categorie.naam) {
			errorVeldenCategoriePUT.categorienaam = true;
			isValid = false;
		}
		if (!categorie.kleur) {
			errorVeldenCategoriePUT.kleur = true;
		}
		if (!categorie.kleurnaam) {
			errorVeldenCategoriePUT.kleurnaam = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessageCategoriePUT = 'Vul alle verplichte velden in.';
			return;
		}
		try {
			await fetch(`${backend_path}/api/testCategorieen/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					naam: categorie.naam,
					kleur: categorie.kleur,
					kleurnaam: categorie.kleurnaam
				})
			});
		} catch (error) {
			console.error('Categorie kon niet worden aangepast: ', error);
		}
		errorMessageCategoriePUT = '';

		return;
	}
</script>

<div class="flex flex-col w-full ml-5 mb-10">
	<div class="flex flex-row justify-between w-full h-14 mb-5">
		<h1 class="font-bold text-3xl">Categorieën beheren</h1>
		<ButtonTerug />
	</div>

	<div class="bg-slate-100 w-full h-full rounded-2xl p-5">
		<div class="flex mb-5 w-full">
			<input
				type="text"
				id="searchCode"
				name="searchCode"
				placeholder="zoeken"
				bind:value={searchCode}
				on:input={filterCategorien}
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
			<div class="text-red-500 mb-2">{deleteError}</div>
			<!-- Header -->
			<div class="grid grid-cols-12 bg-gray-300 rounded-lg h-10 items-center px-3 font-bold">
				<div class="col-span-3">
					<p>Naam categorie</p>
				</div>
				<div class="col-span-3 text-center">
					<p>Kleur</p>
				</div>
				<div class="col-span-3 text-center">
					<p>Naam kleur</p>
				</div>
				<div class="col-span-3 text-right">
					<p>Acties</p>
				</div>
			</div>
			{#if errorMessageCategorie}
				<div class="text-red-500 mb-2">{errorMessageCategorie}</div>
			{/if}
			{#if errorMessageCategoriePUT}
				<div class="text-red-500 mb-2">{errorMessageCategoriePUT}</div>
			{/if}
			<div class="grid grid-cols-12 gap-4 bg-white rounded-lg h-20 items-center px-3 shadow-md">
				<!-- Naam -->
				<div class="col-span-3">
					<input
						type="text"
						id="nieuwecategorie"
						bind:value={categorienaam}
						placeholder="Naam van de categorie"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenCategoriePOST.categorienaam
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>

				<!-- Kleur Picker -->
				<div class="col-span-3 flex justify-center items-center">
					<div
						class={errorVeldenCategoriePOST.categorienaam
							? 'border-2 border-red-500 rounded-lg'
							: ''}
					>
						<ColorPicker
							bind:hex
							label="Kies een kleur"
							components={ChromeVariant}
							sliderDirection="horizontal"
						/>
					</div>
				</div>

				<!-- Naam kleur -->
				<div class="col-span-3">
					<input
						type="text"
						id="kleurnaam"
						bind:value={kleurnaam}
						placeholder="Naam van de kleur"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenCategoriePOST.categorienaam
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>

				<!-- Acties -->
				<div class="col-span-3 flex justify-end">
					<button
						type="submit"
						class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"
						on:click={nieuweCategorie}
						aria-label="Nieuwe categorie toevoegen"
					>
						<FaPlus />
					</button>
				</div>
			</div>

			{#each categorieënSorted as categorie, index}
				<div class="grid grid-cols-12 gap-4 bg-white rounded-lg h-20 items-center px-3 shadow-md">
					<!-- Naam -->
					<div class="col-span-3">
						<input
							type="text"
							on:blur={() => updateCategorie(categorie.id)}
							id="categorie-{categorie?.id}"
							bind:value={categorie.naam}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						/>
					</div>

					<!-- Kleur Picker -->
					<div class="col-span-3 flex justify-center items-center">
						<div class="custom-color-picker">
							<ColorPicker
								label="Kies een kleur"
								components={ChromeVariant}
								sliderDirection="horizontal"
								bind:hex={categorie.kleur}
								on:input={() => updateCategorie(categorie.id)}
							/>
						</div>
					</div>

					<!-- Naam kleur -->
					<div class="col-span-3">
						<input
							type="text"
							on:blur={() => updateCategorie(categorie.id)}
							id="categorie-{categorie?.id}"
							bind:value={categorie.kleurnaam}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						/>
					</div>

					<!-- Acties -->
					<div class="col-span-3 flex justify-end">
						{#if categorie.confirmDelete}
							<button
								type="button"
								on:click={() => deleteCategorie(categorie?.id)}
								class="h-10 w-10 bg-red-500 p-2 rounded-lg text-white"
							>
								<FaTrashAlt />
							</button>
						{:else}
							<button
								type="button"
								on:click={() => {
									categorieën.forEach((c, i) => {
										if (i !== index) c.confirmDelete = false;
									});
									categorie.confirmDelete = true;
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
