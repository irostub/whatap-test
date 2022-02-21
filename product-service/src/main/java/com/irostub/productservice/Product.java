package com.irostub.productservice;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Product {
    private Long id;
    private String name;
    private Integer price;
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

    public void increaseQuantity(int count){
        this.quantity += count;
    }
}
