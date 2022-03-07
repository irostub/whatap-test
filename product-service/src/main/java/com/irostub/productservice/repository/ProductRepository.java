package com.irostub.productservice.repository;

import com.irostub.productservice.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository{
    Long insert(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    Page<Product> findAll(Pageable pageable);

    long updateById(Long id, Product product);

    void deleteById(Long id);
}
