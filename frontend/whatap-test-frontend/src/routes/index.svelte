<script context="module">
	export const prerender = true;
</script>

<svelte:head>
	<title>main page</title>
</svelte:head>

<div class="flex flex-col h-screen justify-center items-center">
	<h2 class="text-5xl font-normal leading-normal mt-0 mb-2 text-gray-600">상품 & 주문 홈</h2>
	<div class="my-8">
		<a href="/products"
		class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-4 px-20 mx-2 rounded">상품 관리</a>
		<a href="/orders"
		class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-4 px-20 mx-2 rounded">주문 관리</a>
	</div>
</div>
