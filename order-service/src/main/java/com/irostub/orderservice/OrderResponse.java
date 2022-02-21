package com.irostub.orderservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String name;
    private String description;
    private String status;
    private String productName;
    private ProductResponse productResponse;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.productId = order.getProductId();
        this.quantity = order.getQuantity();
        this.name = order.getName();
        this.description = order.getDescription();
        this.status = order.getStatus();
        this.productName = order.getProductName();
    }

    public OrderResponse(Order order, ProductResponse productResponse){
        this.id = order.getId();
        this.productId = order.getProductId();
        this.quantity = order.getQuantity();
        this.name = order.getName();
        this.description = order.getDescription();
        this.status = order.getStatus();
        this.productName = order.getProductName();
        this.productResponse = productResponse;
    }
}
