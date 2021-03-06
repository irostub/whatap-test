package com.irostub.orderservice.controller;

import com.irostub.orderservice.dto.OrderRequest;
import com.irostub.orderservice.dto.OrderResponse;
import com.irostub.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/orders")
public class ApiOrderController {
    private final String endpoint = "http://localhost:8802/orders";
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Response<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> allOrders = orderService.getAllOrders();
        return ResponseEntity
                .ok(new Response<>(allOrders));
    }

    @GetMapping("/page")
    public ResponseEntity<Response<Page<OrderResponse>>> getAllOrdersPage(Pageable pageable){
        Page<OrderResponse> allOrdersPage = orderService.getAllOrders(pageable);
        return ResponseEntity
                .ok(new Response<>(allOrdersPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<OrderResponse>> getOneOrder(@PathVariable Long id) {
        OrderResponse oneOrder = orderService.getOneOrder(id);
        return ResponseEntity
                .ok(new Response<>(oneOrder));
    }

    @PostMapping
    public ResponseEntity<?> registerOrder(@Validated @RequestBody OrderRequest orderRequest,
                                           BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            log.warn(bindingResult.toString());
            List<ErrorResult> errorList = getErrorResultList(bindingResult);
            return ResponseEntity.badRequest().body(errorList);
        }

        long orderId = orderService.registerOrder(orderRequest);
        return ResponseEntity
                .created(URI.create(endpoint+"/"+orderId))
                .body(orderId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id,
                                         @Validated @RequestBody OrderRequest orderRequest,
                                         BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            log.warn(bindingResult.toString());
            List<ErrorResult> errorList = getErrorResultList(bindingResult);
            return ResponseEntity.badRequest().body(errorList);
        }

        long orderId = orderService.updateOrder(id, orderRequest);
        return ResponseEntity.ok(orderId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity
                .noContent()
                .build();
    }

    private List<ErrorResult> getErrorResultList(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> new ErrorResult(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    static class Response<T>{
        T data;
    }

    @Getter
    @AllArgsConstructor
    static class ErrorResult{
        private String arguments;
        private String reason;
    }
}
