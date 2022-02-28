package com.irostub.productservice.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.irostub.productservice.dto.ErrorResult;
import com.irostub.productservice.dto.ProductRequest;
import com.irostub.productservice.dto.ProductResponse;
import com.irostub.productservice.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000", exposedHeaders = "*")
@RequiredArgsConstructor
@RequestMapping("/v2/products")
public class ApiProductControllerV2 {
    private final ProductService productService;
    private final String endpoint = "http://localhost:8801/v2/products";

    @GetMapping
    public ResponseEntity<Response<List<ProductResponse>>> getProducts() {
        List<ProductResponse> allProducts = productService.getAllProducts();
        return ResponseEntity.ok(new Response<>(allProducts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<ProductResponse>> getOneProduct(@PathVariable Long id) {
        ProductResponse oneProduct = productService.getOneProduct(id);
        return ResponseEntity.ok(new Response<>(oneProduct));
    }

    @GetMapping("/page")
    public ResponseEntity<Response<Page<ProductResponse>>> getProductsPage(Pageable pageable) {
        Page<ProductResponse> allProductsPage = productService.getAllProducts(pageable);
        return ResponseEntity.ok(new Response<>(allProductsPage));
    }

    @PostMapping
    public ResponseEntity<?> registerProduct(@Validated @RequestBody ProductRequest productRequest,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn(bindingResult.toString());
            List<ErrorResult> errorList = getErrorResultList(bindingResult);
            return ResponseEntity.badRequest().body(errorList);
        }
        long productId = productService.registerProduct(productRequest);
        return ResponseEntity
                .created(URI.create(endpoint + "/" + productId))
                .body(productId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Validated @RequestBody ProductRequest productRequest,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn(bindingResult.toString());
            List<ErrorResult> errorList = getErrorResultList(bindingResult);
            return ResponseEntity.badRequest().body(errorList);
        }

        long productId = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(productId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private List<ErrorResult> getErrorResultList(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> new ErrorResult(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Response<T> {
        private T data;
        private List<Link> links;

        public Response(T data) {
            this.data = data;
        }

        public Response(T data, Link... links) {
            this.data = data;
            this.links = Arrays.asList(links);
        }
    }

    @Getter
    @AllArgsConstructor
    static class Link {
        private String rel;
        private String method;
        private String link;
    }
}
