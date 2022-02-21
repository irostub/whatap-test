package com.irostub.productservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/products")
public class ApiProductControllerV1 {

    private final ProductService productService;

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ProductResponse>>> getAllProducts() {
        List<EntityModel<ProductResponse>> allProducts = productService.getAllProducts().stream()
                .map(productResponse ->
                        EntityModel.of(productResponse, linkTo(methodOn(this.getClass()).getOneProduct(productResponse.getId())).withSelfRel())
                ).collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(CollectionModel.of(allProducts)
                        .add(linkTo(methodOn(this.getClass()).getAllProducts()).withSelfRel())
                        .add(linkTo(methodOn(this.getClass()).registerProduct(null)).withRel("register")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ProductResponse>> getOneProduct(@PathVariable Long id) {
        ProductResponse oneProduct = productService.getOneProduct(id);

        return ResponseEntity.ok()
                .body(EntityModel.of(oneProduct,
                        linkTo(methodOn(this.getClass()).getOneProduct(id)).withSelfRel()
                                .andAffordance(afford(methodOn(this.getClass()).updateProduct(id, null)))
                                .andAffordance(afford(methodOn(this.getClass()).deleteProduct(id))),

                        linkTo(methodOn(this.getClass()).getAllProducts()).withRel("all-products")));
    }

    @GetMapping("/page")
    public ResponseEntity<CollectionModel<EntityModel<ProductResponse>>> getAllProductsPage(Pageable pageable){
        Page<ProductResponse> allProducts = productService.getAllProducts(pageable);

        List<EntityModel<ProductResponse>> productResponseEntityModel = allProducts.getContent().stream()
                .map(productResponse ->
                        EntityModel.of( productResponse ,linkTo(methodOn(this.getClass()).getOneProduct(productResponse.getId())).withSelfRel()))
                .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel
                .PageMetadata(allProducts.getSize(), allProducts.getNumber(), allProducts.getTotalElements());

        PagedModel<EntityModel<ProductResponse>> pagedModel = PagedModel.of(productResponseEntityModel, pageMetadata,
                linkTo(methodOn(this.getClass()).getAllProductsPage(pageable)).withSelfRel());


        //TODO HATEOAS prev and next link, AND sort by id
        return ResponseEntity.ok().body(pagedModel
                .add(linkTo(methodOn(this.getClass()).registerProduct(null)).withRel("register")));
    }

    @PostMapping
    public ResponseEntity<EntityModel<Link>> registerProduct(@Validated @RequestBody ProductRequest productRequest) {
        long productId = productService.registerProduct(productRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EntityModel
                        .of(linkTo(methodOn(this.getClass()).getOneProduct(productId)).withSelfRel()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Link>> updateProduct(@PathVariable Long id,
                                                           @Validated @RequestBody ProductRequest productRequest) {
        long productId = productService.updateProduct(id, productRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(EntityModel
                        .of(linkTo(methodOn(this.getClass()).getOneProduct(productId)).withSelfRel()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
