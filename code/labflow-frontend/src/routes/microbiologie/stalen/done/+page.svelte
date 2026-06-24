<script lang="ts">
	import Nav from '../../../../components/nav.svelte';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	// @ts-ignore
	import FaCheckSquare from 'svelte-icons/fa/FaCheckSquare.svelte';

	let countdown = 5;
	let progress = 0;

	onMount(() => {
		const interval = setInterval(() => {
			countdown--;
			progress += 20;

			if (countdown <= 0) {
				clearInterval(interval);
				goto('/microbiologie/stalen');
			}
		}, 1000);

		return () => clearInterval(interval);
	});
</script>

<Nav />
<main class="flex h-[85vh] flex-col items-center justify-center">
	<div class="text-green-500">
		<FaCheckSquare />
	</div>
	<h1 class="text-8xl text-green-800">Helemaal klaar!</h1>
	<p class="text-2xl text-green-600">
		Je staal is opgeslagen en kan je terugvinden op het microbiologie-dashboard
	</p>

	<div class="mt-8 w-full max-w-md">
		<div class="h-4 rounded-full bg-gray-200">
			<div class="h-full rounded-full bg-green-500" style="width: {progress}%;"></div>
		</div>
	</div>

	<p class="mt-2 text-xl text-green-700">
		Je wordt doorgestuurd in {countdown} seconden...
	</p>
</main>
