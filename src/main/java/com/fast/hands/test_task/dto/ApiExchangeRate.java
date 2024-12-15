package com.fast.hands.test_task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DTO for {@link com.fast.hands.test_task.api.CurrencyController#getExchangeRates(String, List)}.
 */
@Data
@Accessors(chain = true)
public class ApiExchangeRate {
    private String source;
    private OffsetDateTime timestamp;
    private Map<String, BigDecimal> exchangeRates;
}
