package com.fast.hands.test_task.converter;

import com.fast.hands.test_task.dto.ApiCurrency;
import com.fast.hands.test_task.model.Currency;
import org.springframework.core.convert.converter.Converter;

public class CurrencyConverter implements Converter<Currency, ApiCurrency> {

    @Override
    public ApiCurrency convert(Currency source) {
        return new ApiCurrency()
                .setCurrencyCode(source.getCurrencyCode())
                .setName(source.getName())
                .setDescription(source.getDescription());
    }

    public Currency reverse(ApiCurrency source) {
        return new Currency()
                .setCurrencyCode(source.getCurrencyCode())
                .setName(source.getName())
                .setDescription(source.getDescription());
    }
}
