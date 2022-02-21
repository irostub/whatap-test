package com.irostub.productservice;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    public long registerProduct(ProductRequest productRequest){
        Product product = toEntity(productRequest);
        return productRepository.insert(product);
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductResponse::new);
    }

    public ProductResponse getOneProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id + "는 존재하지 않습니다."));
        return new ProductResponse(product);
    }

    public long updateProduct(Long id, ProductRequest productRequest) {
        Product product = toEntity(productRequest);
        return productRepository.updateById(id, product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private Product toEntity(ProductRequest productRequest) {
        return new Product(
                productRequest.getName(),
                productRequest.getPrice(),
                productRequest.getQuantity());
    }
}
