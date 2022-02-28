package com.irostub.orderservice.service;

import com.irostub.orderservice.constant.ApiPath;
import com.irostub.orderservice.domain.Order;
import com.irostub.orderservice.dto.OrderRequest;
import com.irostub.orderservice.dto.OrderResponse;
import com.irostub.orderservice.dto.ProductRequest;
import com.irostub.orderservice.dto.ProductResponse;
import com.irostub.orderservice.exception.OrderNotFoundException;
import com.irostub.orderservice.exception.ProductNotFoundException;
import com.irostub.orderservice.exception.ProductQuantityNotEnoughException;
import com.irostub.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    public long registerOrder(OrderRequest orderRequest) {
        ProductResponse productResponse = getProductResponse(orderRequest.getProductId());

        if (!canOrder(productResponse.getQuantity(), orderRequest.getQuantity())) {
            throw new ProductQuantityNotEnoughException("상품 id " + orderRequest.getProductId() + " 상품의 재고가 부족합니다.");
        }

        restTemplate.put(ApiPath.PRODUCT_SERVICE_URL + "/" + orderRequest.getProductId(),
                new ProductRequest(productResponse.getName(),
                        productResponse.getPrice(),
                        productResponse.getQuantity() - orderRequest.getQuantity()));

        return orderRepository.insert(toEntity(orderRequest, productResponse.getName()));
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(OrderResponse::new);
    }

    public OrderResponse getOneOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(getOrderNotFoundExceptionSupplier(id));

        ProductResponse productResponse = getProductResponse(order.getProductId());

        return new OrderResponse(order, productResponse);
    }

    public long updateOrder(Long id, OrderRequest orderRequest) {
        Order oldOrder = orderRepository.findById(id)
                .orElseThrow(getOrderNotFoundExceptionSupplier(id));

        ProductResponse newProductResponse = getProductResponse(orderRequest.getProductId());

        if (oldOrder.getProductId().equals(orderRequest.getProductId())) {
            //기존 상품과 같을 때
            int newQuantity = newQuantity(newProductResponse.getQuantity(), oldOrder.getQuantity(), orderRequest.getQuantity());

            if( newQuantity < 0 ){
                throw new ProductQuantityNotEnoughException("상품 id " + orderRequest.getProductId() + " 상품의 재고가 부족합니다.");
            }

            restTemplate
                    .put(ApiPath.PRODUCT_SERVICE_URL + "/" + oldOrder.getProductId(),
                            new ProductRequest(newProductResponse.getName(), newProductResponse.getPrice(), newQuantity));
        } else {
            //기존 상품이 아닐 때
            ProductResponse oldProduct = getProductResponse(oldOrder.getProductId());

            if( newProductResponse.getQuantity() < orderRequest.getQuantity()){
                throw new ProductQuantityNotEnoughException("상품 id " + orderRequest.getProductId() + " 상품의 재고가 부족합니다.");
            }

            //기존 상품 복원
            restTemplate
                    .put(ApiPath.PRODUCT_SERVICE_URL + "/" + oldOrder.getProductId(),
                            new ProductRequest(
                                    oldProduct.getName(),
                                    oldProduct.getPrice(),
                                    oldProduct.getQuantity() + oldOrder.getQuantity()));
            //새 상품 차감
            restTemplate
                    .put(ApiPath.PRODUCT_SERVICE_URL + "/" + oldOrder.getProductId(),
                            new ProductRequest(
                                    newProductResponse.getName(),
                                    newProductResponse.getPrice(),
                                    newProductResponse.getQuantity() - orderRequest.getQuantity()));
        }
        return orderRepository.updateById(id, toEntity(orderRequest, newProductResponse.getName()));
    }

    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(getOrderNotFoundExceptionSupplier(id));
        ProductResponse productResponse = getProductResponse(order.getProductId());

        restTemplate.put(ApiPath.PRODUCT_SERVICE_URL + "/" + order.getProductId(),
                new ProductRequest(
                        productResponse.getName(),
                        productResponse.getPrice(),
                        productResponse.getQuantity() + order.getQuantity()
                ));
        orderRepository.deleteById(id);
    }

    private int newQuantity(int productQuantity, int orderOriginQuantity, int targetQuantity) {
        return productQuantity + (orderOriginQuantity - targetQuantity);
    }

    private ResponseEntity<ProductResponse> callProductFindOneApi(long id) {
        return restTemplate
                .getForEntity(ApiPath.PRODUCT_SERVICE_URL + "/" + id, ProductResponse.class);
    }

    private ProductResponse getProductResponse(long id) {
        ResponseEntity<ProductResponse> productEntityResponse = callProductFindOneApi(id);

        if (productEntityResponse.getStatusCode().equals(HttpStatus.OK)
                && productEntityResponse.hasBody()) {
            return productEntityResponse.getBody();
        } else {
            throw new ProductNotFoundException("상품 id " + id + " 는 존재하지않습니다.");
        }
    }

    private Supplier<OrderNotFoundException> getOrderNotFoundExceptionSupplier(Long id) {
        return () -> new OrderNotFoundException("주문 id " + id + "는 존재하지 않습니다.");
    }

    private boolean canOrder(int productQuantity, int orderQuantity) {
        return productQuantity >= orderQuantity;
    }

    private Order toEntity(OrderRequest orderRequest, String productName) {
        return new Order(
                orderRequest.getProductId(),
                orderRequest.getQuantity(),
                orderRequest.getName(),
                orderRequest.getDescription(),
                orderRequest.getStatus(),
                productName
        );
    }
}
