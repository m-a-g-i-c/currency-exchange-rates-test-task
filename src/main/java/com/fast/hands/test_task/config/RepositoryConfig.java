package com.fast.hands.test_task.config;

import com.fast.hands.test_task.repository.CurrencyRepository;
import com.fast.hands.test_task.repository.CurrencyRepositoryDelegate;
import com.fast.hands.test_task.repository.ExchangeRateRepository;
import com.fast.hands.test_task.repository.ExchangeRateRepositoryDelegate;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RepositoryConfig {

    private final CurrencyRepository repository;
    private final ExchangeRateRepository exchangeRateRepository;

    @Bean("com.fast.hands.test_task.config.RepositoryConfig.currencyRepositoryDelegate")
    public CurrencyRepositoryDelegate currencyRepositoryDelegate() {
        return new CurrencyRepositoryDelegate(repository);
    }

    @Bean("com.fast.hands.test_task.config.RepositoryConfig.exchangeRateRepositoryDelegate")
    public ExchangeRateRepositoryDelegate exchangeRateRepositoryDelegate() {
        return new ExchangeRateRepositoryDelegate(exchangeRateRepository);
    }
}
