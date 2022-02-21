package com.irostub.orderservice;

import lombok.Getter;

@Getter
public class Order {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String name;
    private String description;
    private String status;
    private String productName;

    public Order(Long id, Long productId, Integer quantity, String name, String description, String status, String productName) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.name = name;
        this.description = description;
        this.status = status;
        this.productName = productName;
    }

    public Order(Long productId, Integer quantity, String name, String description, String status, String productName) {
        this.productId = productId;
        this.quantity = quantity;
        this.name = name;
        this.description = description;
        this.status = status;
        this.productName = productName;
    }
}
