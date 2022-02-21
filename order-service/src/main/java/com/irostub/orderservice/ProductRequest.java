package com.irostub.orderservice;


import lombok.Getter;

@Getter
public class ProductRequest {
    String name;
    Integer price;
    Integer quantity;

    public ProductRequest(String name, Integer price, Integer quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public ProductRequest(ProductResponse productResponse) {
        this.name = productResponse.getName();
        this.price = productResponse.getPrice();
        this.quantity = productResponse.getQuantity();
    }
}