<script lang="ts">
	import { setContext } from "svelte";

	export let id: string | number;

	setContext("modalId", id);
</script>

<div>
	<slot />
</div>
