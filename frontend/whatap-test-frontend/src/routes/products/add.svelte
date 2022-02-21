<script>
  import axios from 'axios';

  function onSubmitButtonClick(event){
    const baseUrl = "http://localhost:8801/v2/products";

    const name = document.getElementById("name").value;
    const price = document.getElementById("price").value;
    const quantity = document.getElementById("quantity").value;

    const requestObj = {
      name,
      price,
      quantity
    }

    axios.post(baseUrl,requestObj)
    .then(res=>{
      window.location.href="/products/"+res.data
    }).catch(error=>{
      console.log(error.response)
      //TODO: 어떤 필드가 잘못되었는지 알려줄 것
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
<div class="flex flex-col h-screen justify-center items-center">
  <div class="w-3/4 text-center">
      <h2 class="text-5xl font-normal leading-normal mt-0 mb-5 text-gray-600">상품 등록</h2>
      <form class="text-left">
        <div class="mb-6">
          <label for="name" class="block mb-2 text-sm font-medium">상품명</label>
          <input type="text" id="name" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:border-gray-600 dark:placeholder-gray-400 dark:focus:ring-blue-500 dark:focus:border-blue-500" required>
        </div>
        <div class="mb-6">
          <label for="price" class="block mb-2 text-sm font-medium">상품 가격</label>
          <input type="text" id="price" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:border-gray-600 dark:placeholder-gray-400 dark:focus:ring-blue-500 dark:focus:border-blue-500" required>
        </div>
        <div class="mb-6">
          <label for="quantity" class="block mb-2 text-sm font-medium">수량</label>
          <input type="text" id="quantity" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:border-gray-600 dark:placeholder-gray-400 dark:focus:ring-blue-500 dark:focus:border-blue-500" required>
        </div>
        <div class="text-right">
        <button  
        on:click={onSubmitButtonClick}
        type="button" class="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm w-full sm:w-auto px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">등록</button>
        </div>
      </form>
  </div>
</div>
    