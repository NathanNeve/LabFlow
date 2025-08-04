<script lang="ts">
	import Nav from '../../../components/nav.svelte';
	// @ts-ignore
	import FaCheckSquare from 'svelte-icons/fa/FaCheckSquare.svelte';
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';

	let countdown = 5; // initial countdown in seconds
	let progress = 0; // for progress bar (starting full)

	// Function to update the countdown and progress bar
	onMount(() => {
		const interval = setInterval(() => {
			countdown--;
			progress += 20; // 100% divided by 5 seconds

			if (countdown <= 0) {
				clearInterval(interval);
				goto('/stalen'); // Redirect to home after countdown
			}
		}, 1000);
	});
</script>

<Nav />
<main class="flex flex-col justify-center items-center h-[85vh]">
	<!-- Big icon -->
	<div class="text-green-500">
		<FaCheckSquare />
	</div>
	<!-- text -->
	<h1 class=" text-8xl text-green-800">Helemaal klaar!</h1>
	<p class="text-2xl text-green-600">
		Je test is opgeslagen, en kan hem terugvinden op het startscherm
	</p>

	<!-- Progress Bar -->
	<div class="w-full max-w-md mt-8">
		<div class="h-4 bg-gray-200 rounded-full">
			<div class="h-full bg-green-500 rounded-full" style="width: {progress}%;"></div>
		</div>
	</div>

	<!-- Countdown Text -->
	<p class="text-xl text-green-700 mt-2">
		Je wordt doorgestuurd in {countdown} seconden...
	</p>
</main>
