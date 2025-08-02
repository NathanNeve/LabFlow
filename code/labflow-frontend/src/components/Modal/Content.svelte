<script>
	import { id } from './store.js';
	import { getContext } from 'svelte';

	const modalId = getContext('modalId');
</script>

<div>
	<div class="modal" class:hidden={modalId !== $id}>
		<div class="content">
			<div class="absolute top-3 right-8">
				<button
					type="button"
					class="close-button text-3xl"
					on:click={() => ($id = false)}
					aria-label="Close"
					>&times;
				</button>
			</div>
			<slot />
		</div>
	</div>
</div>

<style>
	@import './style.css';
</style>
