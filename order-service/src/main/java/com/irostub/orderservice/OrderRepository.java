package com.irostub.orderservice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderRepository{
    long insert(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    Page<Order> findAll(Pageable pageable);

    long updateById(Long id, Order order);

    int deleteById(Long id);
}
