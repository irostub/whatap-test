package com.irostub.orderservice.repository;

import com.irostub.filedb.FileDb;
import com.irostub.orderservice.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Primary
@Component
@RequiredArgsConstructor
public class OrderRepositoryFileImpl implements OrderRepository{

    private final FileDb<Order> orderFileDb;

    @Override
    public Long insert(Order order) {
            return orderFileDb.insertRecord(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
            return Optional.ofNullable(orderFileDb.findRecord(id));

    }

    @Override
    public List<Order> findAll() {
        return orderFileDb.findAllRecords();
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        long count = orderFileDb.count();
        List<Order> allRecords = orderFileDb.findAllRecords(pageable.getOffset(), pageable.getPageSize());
        return new PageImpl<>(allRecords, pageable, count);
    }

    @Override
    public long updateById(Long id, Order order) {
        return orderFileDb.updateRecord(id, order);
    }

    @Override
    public void deleteById(Long id) {
            orderFileDb.deleteRecord(id);
    }
}
