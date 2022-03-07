package com.irostub.productservice.repository;

import com.irostub.filedb.FileDb;
import com.irostub.productservice.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Primary
@RequiredArgsConstructor
@Component
public class ProductRepositoryFileImpl implements ProductRepository{

    private final FileDb<Product> productFileDb;

    @Override
    public Long insert(Product product) {
        return productFileDb.insertRecord(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
            return Optional.ofNullable(productFileDb.findRecord(id));
    }

    @Override
    public List<Product> findAll() {
        return productFileDb.findAllRecords();
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        long count = productFileDb.count();
        List<Product> allRecords = productFileDb.findAllRecords(pageable.getOffset(), pageable.getPageSize());
        return new PageImpl<>(allRecords, pageable, count);
    }

    @Override
    public long updateById(Long id, Product product) {
        return productFileDb.updateRecord(id, product);
    }

    @Override
    public void deleteById(Long id) {
            productFileDb.deleteRecord(id);
    }
}
