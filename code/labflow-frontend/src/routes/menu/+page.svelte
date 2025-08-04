<script>
	import menufoto from '$lib/assets/lab_menu.jpg';
	import logoLabflow from '$lib/assets/labflowLogoTM.svg';
	import hematologie from '$lib/assets/hematologie.svg';
	import microbiologie from '$lib/assets/microbiologie.svg';
</script>

<div class="flex h-screen">
	<div class="w-1/2">
		<img
			src={menufoto}
			alt="laboranten aan het werk"
			class="w-full h-full object-cover rounded-r-3xl"
		/>
	</div>

	<div class="w-1/2 flex justify-center items-center flex-col">
		<img src={logoLabflow} alt="logo labflow" class="w-1/2 pb-7" />
		<p class="text-gray-900 text-xl font-bold title-font mb-5">Kies een module</p>

		<div class="flex flex-row space-x-5">
			<!-- Hematologie -->
			<div
				class="bg-blue-400 text-white aspect-square w-60 rounded-2xl flex flex-col items-center justify-between p-4"
			>
				<img src={hematologie} alt="Hematologie" class="w-3/4" />
				<p class="text-white text-2xl text-center">Hematologie</p>
			</div>

			<!-- Microbiologie -->
			<div
				class="bg-blue-400 text-white aspect-square w-60 rounded-2xl flex flex-col items-center justify-between p-4"
			>
				<img src={microbiologie} alt="Microbiologie" class="w-3/4" />
				<p class="text-white text-2xl text-center">Microbiologie</p>
			</div>
		</div>
	</div>
</div>
