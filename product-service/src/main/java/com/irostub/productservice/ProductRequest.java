package com.irostub.productservice;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ProductRequest {
    @NotNull @NotEmpty
    String name;
    @NotNull
    Integer price;
    @NotNull
    Integer quantity;
}
