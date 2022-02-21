<script>
    import {onMount} from 'svelte';
  import axios from 'axios';

  const baseUrl = "http://localhost:8801/v2/products";

  let productList = [];

  onMount(async()=>{
    axios.get(baseUrl)
    .then(res=>{
      productList = res.data.data
    })
    .catch(error=>{
      console.log(error)
    });
  });

   function onRowClick(event){
    window.location.href="/products/"+event.currentTarget.id;
  }
</script>

<svelte:head>
	<title>product service</title>
</svelte:head>
<div class="absolute w-fit inset-x-10 z-50 left-10 top-10 inline">
  <a href="/products">
    <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" fill="currentColor" class="inline bi bi-arrow-bar-left" viewBox="0 0 16 16">
    <path fill-rule="evenodd" d="M12.5 15a.5.5 0 0 1-.5-.5v-13a.5.5 0 0 1 1 0v13a.5.5 0 0 1-.5.5zM10 8a.5.5 0 0 1-.5.5H3.707l2.147 2.146a.5.5 0 0 1-.708.708l-3-3a.5.5 0 0 1 0-.708l3-3a.5.5 0 1 1 .708.708L3.707 7.5H9.5a.5.5 0 0 1 .5.5z"/>
    </svg>
  </a>
</div>
<div class="flex flex-col h-screen justify-start items-center">
  <div class="w-3/4 text-center">
      <h2 class="text-5xl font-normal leading-normal pt-28 mb-5 text-gray-600">전체 상품 리스트</h2>
    <table class="divide-y divide-gray-300 w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-2 text-xs text-gray-500">No.</th>
            <th class="px-6 py-2 text-xs text-gray-500">id</th>
            <th class="px-6 py-2 text-xs text-gray-500">상품명</th>
            <th class="px-6 py-2 text-xs text-gray-500">가격</th>
            <th class="px-6 py-2 text-xs text-gray-500">재고</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-300">
          {#each productList as product,i}
          <tr id={product.id}
          on:click|stopPropagation={onRowClick}
          class="whitespace-nowrap cursor-pointer hover:bg-gray-200">
            <td>{i+1}</td>
            <td>{product.id}</td>
            <td>{product.name}</td>
            <td>{product.price}</td>
            <td>{product.quantity}</td>
          </tr>
          {/each}
        </tbody>
      </table>
  </div>
</div>