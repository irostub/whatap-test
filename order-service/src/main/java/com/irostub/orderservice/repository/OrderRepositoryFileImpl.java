package com.irostub.orderservice.repository;

import com.irostub.filedb.FileDb;
import com.irostub.orderservice.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;


@Primary
@Component
@RequiredArgsConstructor
public class OrderRepositoryFileImpl implements OrderRepository{

    private final FileDb<Order> orderFileDb;

    @Override
    public Long insert(Order order) {
        try {
            return orderFileDb.insertRecord(order);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        try {
            return Optional.ofNullable(orderFileDb.findRecord(id));
        } catch (IOException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findAll() {
        return null;
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public long updateById(Long id, Order order) {
        return 0;
    }

    @Override
    public int deleteById(Long id) {
        try {
            orderFileDb.deleteRecord(id);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
