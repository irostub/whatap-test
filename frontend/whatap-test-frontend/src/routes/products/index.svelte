<script>
  import {onMount} from 'svelte';
  import axios from 'axios';

  const baseUrl = "http://localhost:8801/v2/products";

  let productList = [];
  let currentPage = 0;
  let totalPage = 0;
  let size = 5;

  onMount(async()=>{
    axios.get(baseUrl+"/page?page=0&size=5")
    .then(getPageData)
    .catch(error=>{
      console.log(error)
    });
  });

  function onPageButtonClick(event){
    const targetPage = parseInt(event.target.innerText);
    axios.get(`${baseUrl}/page?page=${targetPage-1}&size=${size}`)
    .then(getPageData)
    .catch(error=>{
      console.log(error);
    })
  } 

  function onNextPageButtonClick(event){
    axios.get(`${baseUrl}/page?page=${currentPage+1}&size=${size}`)
    .then(getPageData)
    .catch(error=>{
      console.log(error);
    });
  }

  function onPrevPageButtonClick(event){
    axios.get(`${baseUrl}/page?page=${currentPage-1}&size=${size}`)
    .then(getPageData)
    .catch(error=>{
      console.log(error);
    });
  }

  function onRowClick(event){
    window.location.href="/products/"+event.currentTarget.id;
  }

  function getPageData(res){
    productList = res.data.data.content;
    currentPage = res.data.data.number;
    totalPage = res.data.data.totalPages;
    size = res.data.data.size;
  }
</script>

<svelte:head>
	<title>product service</title>
</svelte:head>
<div class="absolute w-fit inset-x-10 z-50 left-10 top-10 inline">
  <a href="/">
    <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" fill="currentColor" class="inline bi bi-arrow-bar-left" viewBox="0 0 16 16">
    <path fill-rule="evenodd" d="M12.5 15a.5.5 0 0 1-.5-.5v-13a.5.5 0 0 1 1 0v13a.5.5 0 0 1-.5.5zM10 8a.5.5 0 0 1-.5.5H3.707l2.147 2.146a.5.5 0 0 1-.708.708l-3-3a.5.5 0 0 1 0-.708l3-3a.5.5 0 1 1 .708.708L3.707 7.5H9.5a.5.5 0 0 1 .5.5z"/>
    </svg>
  </a>
</div>
<div class="absolute inset-x-10 top-10">
  <div class="flex justify-end items-center">
    <a href="/products/add"
      class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-6 mr-4 rounded">상품 등록</a>
    <a href="/products/all"
      class="align-middle bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded">모든 상품 보기</a>
  </div>
</div>
<div class="flex flex-col h-screen justify-center items-center">
  <div class="w-3/4 text-center">
      <h2 class="text-5xl font-normal leading-normal mt-0 mb-5 text-gray-600">상품 서비스</h2>
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
            <td>{(size*currentPage)+i+1}</td>
            <td>{product.id}</td>
            <td>{product.name}</td>
            <td>{product.price}</td>
            <td>{product.quantity}</td>
          </tr>
          {/each}
        </tbody>
      </table>
      <div>
      <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px mt-10" aria-label="Pagination">
        <button
        disabled={0 === currentPage}
        on:click={onPrevPageButtonClick}
        href="#" class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50">
          <span class="sr-only">Previous</span>
          <!-- Heroicon name: solid/chevron-left -->
          <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
          </svg>
        </button>
        <!-- Current: "z-10 bg-indigo-50 border-indigo-500 text-indigo-600", Default: "bg-white border-gray-300 text-gray-500 hover:bg-gray-50" -->
        
        {#each Array(Math.floor(totalPage/10) === Math.floor(currentPage/10) ? totalPage%10:10) as _, i}
          <button
          type="button" class="{currentPage === Math.floor(currentPage/10) * 10 + i? 
          'z-10 bg-indigo-50 border-indigo-500 text-indigo-600 relative inline-flex items-center px-4 py-2 border text-sm font-medium' :
          'bg-white border-gray-300 text-gray-500 hover:bg-gray-50 relative inline-flex items-center px-4 py-2 border text-sm font-medium'}" 
          on:click={onPageButtonClick}
          aria-current="page">
          {#if Math.floor(currentPage/10) !== 0}
            {Math.floor(currentPage/10) * 10 + i + 1}
          {:else}
            {i+1}
          {/if}
        </button>
          
        {/each}
          <button
          disabled={totalPage-1 <= currentPage}
          on:click={onNextPageButtonClick}
          href="#" class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50">
          <span class="sr-only">Next</span>
          <!-- Heroicon name: solid/chevron-right -->
          <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
          </svg>
        </button>
      </nav>
    </div>
  </div>
</div>