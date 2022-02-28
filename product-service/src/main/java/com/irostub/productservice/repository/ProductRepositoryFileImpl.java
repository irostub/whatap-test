package com.irostub.productservice.repository;

import com.irostub.filedb.FileDb;
import com.irostub.productservice.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
        try {
            return productFileDb.insertRecord(product);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        try {
            return Optional.ofNullable(productFileDb.findRecord(id));
        }catch(IOException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e){
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        return null;
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public long updateById(Long id, Product product) {
        return 0;
    }

    @Override
    public int deleteById(Long id) {
        try {
            productFileDb.deleteRecord(id);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
