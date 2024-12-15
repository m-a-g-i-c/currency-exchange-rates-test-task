package com.fast.hands.test_task.config;

import com.fast.hands.test_task.converter.CurrencyConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {

    @Bean("com.spribe.test_task.config.ConverterConfig.currencyConverter")
    public CurrencyConverter currencyConverter() {
        return new CurrencyConverter();
    }
}
