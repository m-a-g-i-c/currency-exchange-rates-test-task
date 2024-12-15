package com.fast.hands.test_task.config;

import com.fast.hands.test_task.model.ExchangeRate;
import com.fast.hands.test_task.service.CurrencyService;
import com.fast.hands.test_task.service.RateExchangeCronJob;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ServiceConfig {

    private final RepositoryConfig repositoryConfig;
    private final ClientConfig apiLayerConfig;
    private final ConverterConfig converterConfig;

    @Value("${com.fast.hands.test_task.base-currency}")
    private String baseCurrency;

    @Bean("com.fast.hands.test_task.config.ServiceConfig.currencyService")
    public CurrencyService currencyService() {
        return new CurrencyService(repositoryConfig.currencyRepositoryDelegate(),
            converterConfig.currencyConverter(), currencyCache());
    }

    @Bean("com.fast.hands.test_task.config.ServiceConfig.rateExchangeCronJob")
    public RateExchangeCronJob rateExchangeCronJob() {
        return new RateExchangeCronJob(baseCurrency, repositoryConfig.exchangeRateRepositoryDelegate(), repositoryConfig.currencyRepositoryDelegate(),
            apiLayerConfig.apiLayerClient(), currencyCache());
    }

    @Bean("com.fast.hands.test_task.config.ServiceConfig.currencyCache")
    public Cache<String, List<ExchangeRate>> currencyCache() {
        return Caffeine.newBuilder()
            .build();
    }
}
