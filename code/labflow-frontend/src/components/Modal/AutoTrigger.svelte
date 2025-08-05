<script lang="ts">
	import { id } from './store.js';
	import { getContext, onMount } from 'svelte';
	const modalId = getContext('modalId');

	onMount(() => {
		$id = modalId;
	});
</script>

<span>
	<slot />
</span>
