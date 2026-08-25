<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import {
		fetchMicrobiologyCatalogTests,
		fetchMicrobiologyCatalogVoedingsbodems,
		fetchMicrobiologyStaalTypes
	} from '$lib/fetchFunctions';
	// @ts-ignore
	import FaArrowLeft from 'svelte-icons/fa/FaArrowLeft.svelte';
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	// @ts-ignore
	import FaTrashAlt from 'svelte-icons/fa/FaTrashAlt.svelte';
	// @ts-ignore
	import FaPlus from 'svelte-icons/fa/FaPlus.svelte';
	// @ts-ignore
	import FaSave from 'svelte-icons/fa/FaSave.svelte';
	import VoedingsbodemPicker from './VoedingsbodemPicker.svelte';
	import { getCookie } from '$lib/globalFunctions';
	import type {
		MicrobiologySettingsTest,
		MicrobiologyStaalType,
		MicrobiologyTestType,
		MicrobiologyVoedingsbodem
	} from '$lib/types/dbTypes';

	type VoedingsbodemOption = MicrobiologyVoedingsbodem & { label: string; [key: string]: unknown };
	type SettingsTestRow = Omit<MicrobiologySettingsTest, 'voedingsbodems'> & {
		voedingsbodems: VoedingsbodemOption[];
	};

	const backend_path = import.meta.env.VITE_BACKEND_PATH;
	const testTypeOptions: { value: MicrobiologyTestType; label: string }[] = [
		{ value: 'GRAMKLEURING', label: 'Gramkleuring' },
		{ value: 'ANTIBIOGRAM', label: 'Antibiogram' },
		{ value: 'CULTUUR', label: 'Cultuur' },
		{ value: 'EXTRA_TEST', label: 'Extra test' }
	];

	let token: string = '';
	let searchCode = '';
	let tests: SettingsTestRow[] = [];
	let testsSorted: SettingsTestRow[] = [];
	let staalTypes: MicrobiologyStaalType[] = [];
	let voedingsbodemOptions: VoedingsbodemOption[] = [];

	let testCode = '';
	let naam = '';
	let staalTypeId: number | '' = '';
	let testType: MicrobiologyTestType | '' = '';
	let extraTest = false;
	let nieuweVoedingsbodems: VoedingsbodemOption[] = [];

	let openSelectId: string | null = null;
	let deleteError = '';
	let errorMessagePOST = '';
	let errorVeldenPOST = {
		testCode: false,
		naam: false,
		staalTypeId: false,
		testType: false,
		voedingsbodems: false
	};
	let errorMessagePUT = '';

	function withLabels(items: MicrobiologyVoedingsbodem[] = []): VoedingsbodemOption[] {
		return items.map((item) => ({ ...item, label: item.label || item.naam }));
	}

	function mapTests(result: MicrobiologySettingsTest[]): SettingsTestRow[] {
		return result.map((test) => ({
			...test,
			staalType: test.staalType ?? { id: 0, naam: '' },
			voedingsbodems: withLabels(test.voedingsbodems ?? []),
			confirmDelete: false
		}));
	}

	function isCultuur(type: MicrobiologyTestType | string | '' | undefined) {
		return String(type) === 'CULTUUR';
	}

	function onCreateTypeChange(event: Event) {
		const value = (event.currentTarget as HTMLSelectElement).value as MicrobiologyTestType | '';
		testType = value;
		if (!isCultuur(value)) {
			nieuweVoedingsbodems = [];
			errorVeldenPOST.voedingsbodems = false;
		}
	}

	function onRowTypeChange(test: SettingsTestRow, event: Event) {
		const value = (event.currentTarget as HTMLSelectElement).value as MicrobiologyTestType;
		test.testType = value;
		if (!isCultuur(value)) {
			test.voedingsbodems = [];
		}
		updateTest(test.id);
	}

	function voedingsbodemIdsForType(
		type: MicrobiologyTestType | '' | undefined,
		items: VoedingsbodemOption[]
	) {
		if (!isCultuur(type)) return [];
		return items.map((item) => item.id);
	}

	function setSelectOpen(id: string, isOpen: boolean) {
		openSelectId = isOpen ? id : openSelectId === id ? null : openSelectId;
	}

	onMount(async () => {
		token = getCookie('authToken') || '';
		const fetchedTypes = await fetchMicrobiologyStaalTypes();
		if (fetchedTypes) staalTypes = fetchedTypes;
		const fetchedVoedingsbodems = await fetchMicrobiologyCatalogVoedingsbodems();
		if (fetchedVoedingsbodems) voedingsbodemOptions = withLabels(fetchedVoedingsbodems);
		await reloadTests();
	});

	async function reloadTests() {
		const result = await fetchMicrobiologyCatalogTests();
		if (result) {
			const mapped = mapTests(result);
			[tests, testsSorted] = [mapped, mapped];
		}
	}

	function filterTests() {
		const query = searchCode.toLowerCase();
		testsSorted = tests.filter((test) => {
			return (
				test.testCode.toString().toLowerCase().includes(query) ||
				test.naam.toString().toLowerCase().includes(query) ||
				(test.staalType?.naam ?? '').toLowerCase().includes(query) ||
				(test.testType ?? '').toLowerCase().includes(query) ||
				(test.voedingsbodems ?? []).some((vb) => vb.naam.toLowerCase().includes(query))
			);
		});
	}

	function verwijderZoek() {
		searchCode = '';
		testsSorted = tests;
	}

	async function deleteTest(id: number) {
		try {
			const response = await fetch(`${backend_path}/api/microbiology/catalog/tests/${id}`, {
				method: 'DELETE',
				headers: {
					Authorization: 'Bearer ' + token
				}
			});
			if (response.ok) {
				deleteError = '';
				await reloadTests();
			} else {
				deleteError =
					'Test kon niet worden verwijderd omdat deze gelinked is aan één of meerdere stalen.';
			}
		} catch (error) {
			console.error('Test kon niet worden verwijderd: ', error);
		}
	}

	async function nieuweTest() {
		errorVeldenPOST = {
			testCode: false,
			naam: false,
			staalTypeId: false,
			testType: false,
			voedingsbodems: false
		};
		let isValid = true;
		if (!testCode) {
			errorVeldenPOST.testCode = true;
			isValid = false;
		}
		if (!naam) {
			errorVeldenPOST.naam = true;
			isValid = false;
		}
		if (!staalTypeId) {
			errorVeldenPOST.staalTypeId = true;
			isValid = false;
		}
		if (!testType) {
			errorVeldenPOST.testType = true;
			isValid = false;
		}
		if (isCultuur(testType) && nieuweVoedingsbodems.length === 0) {
			errorVeldenPOST.voedingsbodems = true;
			isValid = false;
		}
		if (!isValid) {
			errorMessagePOST = isCultuur(testType) && nieuweVoedingsbodems.length === 0
				? 'Selecteer minstens één voedingsbodem voor een cultuurtest.'
				: 'Vul alle verplichte velden in.';
			return;
		}

		try {
			const response = await fetch(`${backend_path}/api/microbiology/catalog/tests`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					testCode,
					naam,
					extraTest,
					testType,
					staalTypeId,
					voedingsbodemIds: voedingsbodemIdsForType(testType, nieuweVoedingsbodems)
				})
			});
			if (response.status === 409) {
				errorMessagePOST = 'Testcode bestaat al.';
				return;
			}
			if (!response.ok) {
				errorMessagePOST = 'Test kon niet worden aangemaakt.';
				return;
			}
			testCode = '';
			naam = '';
			staalTypeId = '';
			extraTest = false;
			nieuweVoedingsbodems = [];
			openSelectId = null;
			testType = '';
			errorMessagePOST = '';
			await reloadTests();
		} catch (error) {
			console.error('Test kon niet worden aangemaakt: ', error);
		}
	}

	async function updateTest(id: number) {
		const test = tests.find((t) => t.id === id);
		if (!test) return;

		if (!test.testCode || !test.naam || !test.staalType?.id || !test.testType) {
			errorMessagePUT = 'Vul alle verplichte velden in.';
			return;
		}
		if (isCultuur(test.testType) && (test.voedingsbodems ?? []).length === 0) {
			errorMessagePUT = 'Selecteer minstens één voedingsbodem voor een cultuurtest.';
			return;
		}

		try {
			const response = await fetch(`${backend_path}/api/microbiology/catalog/tests/${id}`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + token
				},
				body: JSON.stringify({
					testCode: test.testCode,
					naam: test.naam,
					extraTest: test.extraTest,
					testType: test.testType,
					staalTypeId: test.staalType.id,
					voedingsbodemIds: voedingsbodemIdsForType(test.testType, test.voedingsbodems ?? [])
				})
			});
			if (response.status === 409) {
				errorMessagePUT = 'Testcode bestaat al.';
				return;
			}
			if (!response.ok) {
				errorMessagePUT = 'Test kon niet worden aangepast.';
				return;
			}
			errorMessagePUT = '';
		} catch (error) {
			console.error('Test kon niet worden aangepast: ', error);
		}
	}
</script>

<div class="flex flex-col w-full ml-5">
	<div class="flex flex-row justify-between w-full h-14 mb-5">
		<h1 class="font-bold text-3xl">Testen beheren</h1>
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
				on:input={filterTests}
				class="h-12 rounded-l-lg text-black pl-3 w-2/5"
			/>
			<button
				on:click={verwijderZoek}
				class="w-12 h-12 p-4 flex items-center justify-center bg-red-500 hover:bg-red-600 text-white rounded-r-lg"
			>
				<GoX />
			</button>
		</div>
		<div class="space-y-3 overflow-visible">
			{#if deleteError}
				<div class="text-red-500 mb-2">{deleteError}</div>
			{/if}
			<div
				class="grid grid-cols-12 bg-gray-300 rounded-lg h-10 items-center px-3 font-bold space-x-3"
			>
				<div class="col-span-1 text-left"><p>Testcode</p></div>
				<div class="col-span-2 text-left"><p>Naam</p></div>
				<div class="col-span-2 text-left"><p>Staaltype</p></div>
				<div class="col-span-2 text-left"><p>Type</p></div>
				<div class="col-span-4 text-left"><p>Voedingsbodems</p></div>
				<div class="col-span-1 text-right"><p>Acties</p></div>
			</div>

			{#if errorMessagePOST}
				<div class="text-red-500 mb-2">{errorMessagePOST}</div>
			{/if}

			<div
				class="grid grid-cols-12 space-x-3 bg-white rounded-lg min-h-20 items-center px-3 shadow-md py-2 overflow-visible relative {openSelectId ===
				'new'
					? 'z-40'
					: 'z-0'}"
			>
				<div class="col-span-1">
					<input
						type="text"
						bind:value={testCode}
						placeholder="Testcode"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenPOST.testCode
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="col-span-2">
					<input
						type="text"
						bind:value={naam}
						placeholder="Naam van de test"
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenPOST.naam
							? 'border-2 border-red-500'
							: ''}"
					/>
				</div>
				<div class="col-span-2">
					<select
						bind:value={staalTypeId}
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenPOST.staalTypeId
							? 'border-2 border-red-500'
							: ''}"
					>
						<option value="" disabled selected hidden>Staaltype</option>
						{#each staalTypes as type}
							<option value={type.id}>{type.naam}</option>
						{/each}
					</select>
				</div>
				<div class="col-span-2">
					<select
						bind:value={testType}
						on:change={onCreateTypeChange}
						class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full {errorVeldenPOST.testType
							? 'border-2 border-red-500'
							: ''}"
					>
						<option value="" disabled selected hidden>Type</option>
						{#each testTypeOptions as option}
							<option value={option.value}>{option.label}</option>
						{/each}
					</select>
				</div>
				<div class="col-span-4 relative">
					{#if isCultuur(testType)}
						<VoedingsbodemPicker
							options={voedingsbodemOptions}
							bind:value={nieuweVoedingsbodems}
							placeholder="Voedingsbodems"
							invalid={errorVeldenPOST.voedingsbodems}
							open={openSelectId === 'new'}
							openChange={(isOpen) => setSelectOpen('new', isOpen)}
						/>
					{:else}
						<p class="text-gray-400">—</p>
					{/if}
				</div>
				<div class="col-span-1 flex justify-end">
					<button
						type="button"
						class="h-10 w-10 bg-green-500 p-2 rounded-lg text-white"
						on:click={nieuweTest}
						aria-label="Nieuwe test toevoegen"
					>
						<FaPlus />
					</button>
				</div>
			</div>

			{#if errorMessagePUT}
				<div class="text-red-500 mb-2">{errorMessagePUT}</div>
			{/if}

			{#each testsSorted as test, index (test.id)}
				<div
					class="grid grid-cols-12 bg-white rounded-lg min-h-20 items-center px-3 shadow-md space-x-3 py-2 overflow-visible relative {openSelectId ===
					String(test.id)
						? 'z-40'
						: 'z-0'}"
				>
					<div class="col-span-1">
						<input
							type="text"
							on:blur={() => updateTest(test.id)}
							bind:value={test.testCode}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						/>
					</div>
					<div class="col-span-2">
						<input
							type="text"
							on:blur={() => updateTest(test.id)}
							bind:value={test.naam}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						/>
					</div>
					<div class="col-span-2">
						<select
							on:change={() => updateTest(test.id)}
							bind:value={test.staalType.id}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						>
							{#each staalTypes as type}
								<option value={type.id}>{type.naam}</option>
							{/each}
						</select>
					</div>
					<div class="col-span-2">
						<select
							on:change={(event) => onRowTypeChange(test, event)}
							bind:value={test.testType}
							class="bg-gray-100 rounded-lg h-14 text-lg pl-3 w-full"
						>
							{#each testTypeOptions as option}
								<option value={option.value}>{option.label}</option>
							{/each}
						</select>
					</div>
					<div class="col-span-4 relative">
						{#if isCultuur(test.testType)}
							<VoedingsbodemPicker
								options={voedingsbodemOptions}
								bind:value={test.voedingsbodems}
								placeholder="Voedingsbodems"
								open={openSelectId === String(test.id)}
								openChange={(isOpen) => setSelectOpen(String(test.id), isOpen)}
								selectionChange={() => updateTest(test.id)}
							/>
						{:else}
							<p class="text-gray-400">—</p>
						{/if}
					</div>
					<div class="col-span-1 flex justify-end">
						<button
							type="button"
							class="h-10 w-10 p-2 rounded-lg text-white mr-2 bg-green-500 hover:bg-green-700 transition duration-500"
							on:click={() => updateTest(test.id)}
							aria-label="Save test"
						>
							<FaSave />
						</button>
						{#if test.confirmDelete}
							<button
								type="button"
								on:click={() => deleteTest(test.id)}
								class="h-10 w-10 bg-red-500 p-2 rounded-lg text-white"
							>
								<FaTrashAlt />
							</button>
						{:else}
							<button
								type="button"
								on:click={() => {
									tests.forEach((c, i) => {
										if (i !== index) c.confirmDelete = false;
									});
									test.confirmDelete = true;
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
