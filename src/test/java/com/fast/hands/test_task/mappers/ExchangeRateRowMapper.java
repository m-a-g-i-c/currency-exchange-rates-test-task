package com.fast.hands.test_task.mappers;

import com.fast.hands.test_task.model.Currency;
import com.fast.hands.test_task.model.ExchangeRate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

public class ExchangeRateRowMapper implements RowMapper<ExchangeRate> {
    @Override
    public ExchangeRate mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ExchangeRate()
            .setId(UUID.fromString(rs.getString("id")))
            .setSource(new Currency().setId(UUID.fromString(rs.getString("source"))))
            .setTarget(new Currency().setId(UUID.fromString(rs.getString("target"))))
            .setRate(rs.getBigDecimal("rate"))
            .setTimestamp(OffsetDateTime.parse(rs.getString("timestamp")));
    }
}
