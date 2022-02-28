package com.irostub.productservice.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.irostub.productservice.dto.ErrorResult;
import com.irostub.productservice.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ApiProductControllerV2.class)
public class ApiExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorResult> handleMethodArgumentNotValidException(InvalidFormatException e){
        String pathReference = e.getPathReference();
        String path = pathReference.substring(pathReference.indexOf('[')+2,pathReference.indexOf(']')-1);

        ErrorResult errorResult = new ErrorResult
                (path, e.getValue() + "는 " + e.getTargetType() + "가 될 수 없습니다.");
        return ResponseEntity
                .badRequest()
                .body(errorResult);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }
}
