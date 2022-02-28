package com.irostub.orderservice.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class OrderRequest {
    @NotNull
    private Long productId;

    @NotNull
    private Integer quantity;

    @NotNull @NotEmpty
    private String name;

    private String description;

    private String status;
}
