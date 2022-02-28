package com.irostub.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ProductResponse {
    private Long id;

    private String name;

    private Integer price;

    private Integer quantity;

    @JsonProperty("data")
    private void unpackNested(Map<String, Object> data){
        this.id = ((Number) data.get("id")).longValue();
        this.name = (String)data.get("name");
        this.price = (Integer)data.get("price");
        this.quantity = (Integer)data.get("quantity");
    }
}
