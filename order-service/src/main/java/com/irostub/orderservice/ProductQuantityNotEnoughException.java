package com.irostub.orderservice;

public class ProductQuantityNotEnoughException extends RuntimeException{
    public ProductQuantityNotEnoughException(String message) {
        super(message);
    }
}
