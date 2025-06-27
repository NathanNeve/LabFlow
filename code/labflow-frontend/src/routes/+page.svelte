<script>
	import Login from '../components/Login.svelte';
	// https://kit.svelte.dev/docs/images
	import loginphoto from '$lib/assets/lab_login.jpg';
	import logoLabflow from '$lib/assets/labflowLogoTM.svg';
</script>

<div class="flex h-screen">
	<div class="w-1/2">
		<img
			src={loginphoto}
			alt="laboranten aan het werk"
			class="w-full h-full object-cover rounded-r-3xl"
		/>
	</div>

	<div class="w-1/2 flex justify-center items-center flex-col">
		<img src={logoLabflow} alt="logo labflow" class="w-1/2 pb-7" />
		<Login />
	</div>
</div>
