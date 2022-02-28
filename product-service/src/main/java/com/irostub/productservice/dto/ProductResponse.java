package com.irostub.productservice.dto;

import com.irostub.productservice.domain.Product;
import lombok.Data;

@Data
public class ProductResponse{
    private Long id;
    private String name;
    private Integer price;
    private Integer quantity;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.quantity = product.getQuantity();
    }
}
