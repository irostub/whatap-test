package com.irostub.orderservice.exception;

public class ProductQuantityNotEnoughException extends RuntimeException{
    public ProductQuantityNotEnoughException(String message) {
        super(message);
    }
}
