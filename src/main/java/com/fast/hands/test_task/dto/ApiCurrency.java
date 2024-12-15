package com.fast.hands.test_task.dto;

import com.fast.hands.test_task.api.CurrencyController;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Represents DTO for {@link CurrencyController#getCurrencies()}.
 */
@Data
@Accessors(chain = true)
public class ApiCurrency {
    @JsonProperty("currency_code")
    private String currencyCode;
    private String name;
    private String description;

    public ApiCurrency() {}

    public ApiCurrency(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
