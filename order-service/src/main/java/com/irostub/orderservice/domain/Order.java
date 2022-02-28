package com.irostub.orderservice.domain;

import com.irostub.filedb.annotation.Field;
import com.irostub.filedb.annotation.Id;
import lombok.Getter;

@Getter
public class Order {
    @Field @Id
    private Long id;

    @Field
    private Long productId;

    @Field
    private Integer quantity;

    @Field
    private String name;

    @Field
    private String description;

    @Field
    private String status;

    @Field
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
