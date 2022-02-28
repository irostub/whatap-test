package com.irostub.productservice;

import com.irostub.productservice.domain.Product;
import com.irostub.productservice.repository.ProductRepositoryJdbcImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class ProductRepositoryJdbcImplTest {

    @Autowired
    ProductRepositoryJdbcImpl productRepositoryJdbc;

    @Test
    @DisplayName("insert 성공테스트")
    void insert(){
        //given
        long productId = productRepositoryJdbc.insert(new Product("상품1", 1000, 50));

        //when

        //then
    }
}