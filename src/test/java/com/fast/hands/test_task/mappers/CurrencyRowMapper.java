package com.fast.hands.test_task.mappers;

import com.fast.hands.test_task.model.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

public class CurrencyRowMapper implements RowMapper<Currency> {
    @Override
    public Currency mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Currency()
            .setId(UUID.fromString(rs.getString("id")))
            .setCurrencyCode(rs.getString("currency_code"))
            .setName(rs.getString("name"))
            .setDescription(rs.getString("description"));
    }
}
