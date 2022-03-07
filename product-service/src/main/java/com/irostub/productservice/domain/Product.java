package com.irostub.productservice.domain;

import com.irostub.filedb.annotation.Field;
import com.irostub.filedb.annotation.Id;
import lombok.Getter;

@Getter
public class Product {

    @Field @Id
    private Long id;

    @Field
    private String name;

    @Field
    private Integer price;

    @Field
    private Integer quantity;

    public Product(Long id, String name, Integer price, Integer quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Product(String name, Integer price, Integer quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    private Product() {

    }

    public void increaseQuantity(int count){
        this.quantity += count;
    }
}
