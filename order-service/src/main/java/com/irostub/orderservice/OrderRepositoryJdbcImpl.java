package com.irostub.orderservice;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderRepositoryJdbcImpl implements OrderRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public long insert(Order order) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con
                    .prepareStatement("INSERT INTO ORDERS (PRODUCT_ID, QUANTITY, NAME, DESCRIPTION, STATUS, PRODUCT_NAME) VALUES (?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, order.getProductId());
            ps.setInt(2, order.getQuantity());
            ps.setString(3, order.getName());
            ps.setString(4, order.getDescription());
            ps.setString(5, order.getStatus());
            ps.setString(6, order.getProductName());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<Order> findById(Long id) {
        try {
            return Optional.ofNullable(jdbcTemplate
                    .queryForObject("SELECT * FROM ORDERS WHERE id = ?", orderRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findAll() {
        return jdbcTemplate
                .query("SELECT * FROM ORDERS", orderRowMapper);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ORDERS", (rs, rowNum) -> rs.getInt(1));

        String query = "SELECT * FROM ORDERS" +
                " LIMIT " + pageable.getPageSize() +
                " OFFSET " + pageable.getOffset();

        List<Order> result = jdbcTemplate.query(query, orderRowMapper);

        return new PageImpl<>(result, pageable, count);
    }

    @Override
    public long updateById(Long id, Order order) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        String query = "UPDATE ORDERS" +
                " SET PRODUCT_ID = ?, QUANTITY = ?, NAME = ?, DESCRIPTION = ?, STATUS = ?, PRODUCT_NAME = ?" +
                " WHERE id = ?";

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(query,
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, order.getProductId());
                    ps.setInt(2, order.getQuantity());
                    ps.setString(3, order.getName());
                    ps.setString(4, order.getDescription());
                    ps.setString(5, order.getStatus());
                    ps.setString(6, order.getProductName());
                    ps.setLong(7, id);
                    return ps;
                }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate
                .update("DELETE FROM ORDERS WHERE id = ?", id);
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) ->
            new Order(
                    rs.getLong("id"),
                    rs.getLong("product_id"),
                    rs.getInt("quantity"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("product_name")
            );
}
