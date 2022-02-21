package com.irostub.productservice;

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
public class ProductRepositoryJdbcImpl implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long insert(Product product) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con->{
            PreparedStatement ps = con
                    .prepareStatement("INSERT INTO PRODUCT (name, price, quantity) VALUES (?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getName());
            ps.setInt(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<Product> findById(Long id) {
        try{
         return Optional.ofNullable(jdbcTemplate
                 .queryForObject("SELECT * FROM PRODUCT WHERE id = ?", productRowMapper, id));
        }catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PRODUCT", (rs, rowNum) -> rs.getInt(1));

        String query = "SELECT * FROM PRODUCT" +
                " LIMIT " + pageable.getPageSize() +
                " OFFSET " + pageable.getOffset();

        List<Product> result = jdbcTemplate.query(query, productRowMapper);

        return new PageImpl<>(result , pageable, count);
    }

    @Override
    public List<Product> findAll() {
        return jdbcTemplate
                .query("SELECT * FROM PRODUCT", productRowMapper);
    }

    @Override
    public long updateById(Long id, Product product) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con->{
                    PreparedStatement ps = con.prepareStatement("UPDATE PRODUCT SET name = ?, price = ?, quantity = ? WHERE id = ?",
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, product.getName());
                    ps.setInt(2, product.getPrice());
                    ps.setInt(3, product.getQuantity());
                    ps.setLong(4, id);
                    return ps;
                },keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate
                .update("DELETE FROM PRODUCT WHERE id = ?", id);
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) ->
            new Product(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getInt("price"),
            rs.getInt("quantity"));
}
