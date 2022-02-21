<script>
  import axios from 'axios';
  import { page } from '$app/stores';
  import { onMount } from 'svelte'; 

  const baseUrl = "http://localhost:8801/v2/products";

  let data = {
    id:0,
    name:"",
    price:0,
    quantity:0
  }

  onMount(async()=>{
    axios.get(baseUrl+"/"+$page.params.id)
    .then(res=>{
      console.log(res.data)
      data=res.data.data
    })
    .catch(error=>{
      console.log(error)
    });
  });

  function onDeleteButtonClick(event){
    axios.delete(baseUrl+"/"+$page.params.id)
    .then(()=>{
      //TODO : 삭제 요청 처리 후 modal or alert
      console.log("삭제요청 처리됨");
      window.location.href="/products";
    })
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
<div class="absolute inset-x-10 top-10">
  <div class="flex justify-end items-center">
    <a href="/products/update/{data.id}"
      class="bg-emerald-500 hover:bg-emerald-700 text-white font-bold py-3 px-6 mr-4 rounded">상품 수정</a>
    <button
    on:click={onDeleteButtonClick}
      class="align-middle bg-red-500 hover:bg-red-700 text-white font-bold py-3 px-6 rounded">상품 삭제</button>
  </div>
</div>
<div class="flex flex-col h-screen justify-center items-center">
  <div class="w-3/4 text-center">
      <h2 class="text-5xl font-normal leading-normal mt-0 mb-5 text-gray-600">상품 상세</h2>
    <table class="divide-y divide-gray-300 w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-2 text-xs text-gray-500">Type</th>
            <th class="px-6 py-2 text-xs text-gray-500">Desc</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-300">

          <tr
          class="whitespace-nowrap">
            <td>id</td>
            <td>{data.id}</td>
          </tr>
          <tr
          class="whitespace-nowrap">
            <td>상품명</td>
            <td>{data.name}</td>
          </tr>
          <tr
          class="whitespace-nowrap">
            <td>가격</td>
            <td>{data.price}</td>
          </tr>
          <tr
          class="whitespace-nowrap">
            <td>재고</td>
            <td>{data.quantity}</td>
          </tr>
        </tbody>
      </table>
  </div>
</div>
