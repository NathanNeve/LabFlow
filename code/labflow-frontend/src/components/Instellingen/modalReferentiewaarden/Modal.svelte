<script lang="ts">
	// @ts-ignore
	import GoX from 'svelte-icons/go/GoX.svelte';
	export let showModal = false;
	import { writable } from 'svelte/store';
	import MultiSelect from 'svelte-multiselect';
	const backend_path = import.meta.env.VITE_BACKEND_PATH;

	let dialog: HTMLDialogElement;

	$: if (showModal && dialog) dialog.showModal();

	// Sluit de dialog en set showmodal op false
	function closeDialog() {
		dialog.close();
		showModal = false;
	}

	export let waarden: any[] = [];
	export let selectedValues = writable([]);

	let waarde = '';
	let errorWaarde = false;

	async function nieuweReferentiewaarde() {
		if (waarde.trim() === '') {
			errorWaarde = true;
			return;
		}
		errorWaarde = false;
		try {
			await fetch(`${backend_path}/api/createreferentiewaarde`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				credentials: 'include',
				body: JSON.stringify({
					waarde: waarde
				})
			});
			waarden = waarden.concat({
				id: waarden.length + 1,
				waarde: waarde,
				label: waarde
			});

			waarde = ''; // Reset waarde na toevoegen
		} catch (error) {
			console.error('Test kon niet worden aangemaakt: ', error);
		}
	}
</script>

<dialog bind:this={dialog} on:close={closeDialog} tabindex="-1">
	<div class="modal-content flex flex-col justify-between w-full h-full">
		<div>
			<div class="flex flex-row justify-between items-center">
				<p class="font-bold text-xl">Referentiewaardes linken</p>
				<button
					type="button"
					on:click={closeDialog}
					class="w-10 h-10 p-2 mb-3 flex items-center justify-center bg-red-400 rounded-lg"
					><GoX /></button
				>
			</div>
			<label for="referentiewaarden">
				<strong>Kies je referentiewaardes:</strong>
			</label>
			<MultiSelect
				style="height: 60px;"
				id="referentiewaarden"
				options={waarden}
				bind:value={$selectedValues}
				placeholder="Referentiewaarden"
			/>
		</div>
		<div>
			<p class="font-bold text-xl mb-5">Aanmaken referentiewaarde</p>
			<div class="flex flex-row justify-between">
				<input
					type="text"
					name="referentiewaarde"
					id="referentiewaarde"
					bind:value={waarde}
					class="rounded-lg text-black bg-gray-200 h-12 w-80 mt-4 px-3 {errorWaarde
						? 'border-red-500 border-2'
						: ''}"
				/>
				<button
					on:click={nieuweReferentiewaarde}
					type="button"
					class="bg-green-500 rounded-lg text-black h-12 w-56 font-bold text-lg"
				>
					Opslaan
				</button>
			</div>
			{#if errorWaarde}
				<p class="text-red-500 mt-2">Vul een waarde in.</p>
			{/if}
		</div>
	</div>
</dialog>

<style>
	dialog {
		position: fixed;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		max-width: 90vw;
		max-height: 60vh;
		width: 60em;
		height: 100em;
		border: none;
		border-radius: 0.2em;
		padding: 0;
		box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
	}

	dialog::backdrop {
		background: rgba(0, 0, 0, 0.3);
	}

	.modal-content {
		padding: 1.5em;
	}

	dialog[open] {
		animation: zoom 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
	}

	@keyframes zoom {
		from {
			transform: translate(-50%, -50%) scale(0.95);
		}
		to {
			transform: translate(-50%, -50%) scale(1);
		}
	}

	button {
		display: block;
		margin-top: 1em;
	}
</style>
