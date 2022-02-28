package com.irostub.orderservice.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.irostub.orderservice.exception.OrderNotFoundException;
import com.irostub.orderservice.exception.ProductNotFoundException;
import com.irostub.orderservice.exception.ProductQuantityNotEnoughException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ApiOrderController.class)
public class ApiExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ApiOrderController.ErrorResult> handleMethodArgumentNotValidException(InvalidFormatException e){
        String pathReference = e.getPathReference();
        String path = pathReference.substring(pathReference.indexOf('[')+2,pathReference.indexOf(']')-1);

        ApiOrderController.ErrorResult errorResult = new ApiOrderController.ErrorResult
                (path, e.getValue() + "는 " + e.getTargetType() + "가 될 수 없습니다.");
        return ResponseEntity
                .badRequest()
                .body(errorResult);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleOrderNotFoundException(OrderNotFoundException e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleProductQuantityNotEnoughException(ProductQuantityNotEnoughException e){
        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }
}
