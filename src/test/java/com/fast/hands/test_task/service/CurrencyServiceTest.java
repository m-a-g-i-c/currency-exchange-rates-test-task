package com.fast.hands.test_task.service;

import com.fast.hands.test_task.converter.CurrencyConverter;
import com.fast.hands.test_task.dto.ApiCurrency;
import com.fast.hands.test_task.model.Currency;
import com.fast.hands.test_task.model.ExchangeRate;
import com.fast.hands.test_task.repository.CurrencyRepositoryDelegate;
import com.github.benmanes.caffeine.cache.Cache;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

public class CurrencyServiceTest {

    private final Cache<String, List<ExchangeRate>> cache = Mockito.mock(Cache.class);
    private final CurrencyConverter currencyConverter = new CurrencyConverter();

    private final CurrencyRepositoryDelegate currencyRepo = Mockito.mock(CurrencyRepositoryDelegate.class);

    private final CurrencyService currencyService = new CurrencyService(currencyRepo, currencyConverter, cache);

    @BeforeEach
    void beforeEach() {
        cache.invalidateAll();
    }

    @Test
    public void getNothingWhenNoCurrenciesArePresent() {
        Mockito.when(currencyRepo.findAll()).thenReturn(Collections.emptyList());

        StepVerifier.create(currencyService.getCurrencies())
            .expectNextMatches(s -> s.isEmpty())
            .verifyComplete();
    }

    @Test
    public void getCurrenciesSuccessfully() {
        Mockito.when(currencyRepo.findAll()).thenReturn(
            List.of(new Currency("USD"), new Currency("EUR"), new Currency("GBP"))
        );

        StepVerifier.create(currencyService.getCurrencies())
            .expectNextMatches(s -> s.size() == 3)
            .verifyComplete();
    }

    @Test
    public void createCurrencySuccessfully() {
        Mockito.when(currencyRepo.saveThrowable(Mockito.any())).thenReturn(new Currency("USD"));

        StepVerifier.create(currencyService.createCurrency(new ApiCurrency("USD")))
            .expectNextMatches(s -> s.getCurrencyCode().equals("USD"))
            .verifyComplete();
    }

    @Test
    public void failToCreateCurrencyWithNoCurrencyCode() {
        Mockito.when(currencyRepo.saveThrowable(Mockito.any()))
            .thenThrow(new RuntimeException("Cannot save currency"));

        StepVerifier.create(currencyService.createCurrency(new ApiCurrency()))
            .verifyError(RuntimeException.class);
    }

    @Test
    public void getExchangeRatesSuccessfully() {
        Mockito.when(cache.getIfPresent("EUR"))
            .thenReturn(List.of(new ExchangeRate()
                    .setSource(new Currency("EUR"))
                    .setRate(new BigDecimal("1.11"))
                    .setTarget(new Currency("PLN")),
                new ExchangeRate()
                    .setSource(new Currency("EUR"))
                    .setRate(new BigDecimal("1.12"))
                    .setTarget(new Currency("CZK")),
                new ExchangeRate()
                    .setSource(new Currency("EUR"))
                    .setRate(new BigDecimal("1.13"))
                    .setTarget(new Currency("UAH")),
                new ExchangeRate()
                    .setSource(new Currency("EUR"))
                    .setRate(new BigDecimal("1.14"))
                    .setTarget(new Currency("JPY"))
            ));

        StepVerifier.create(currencyService.getExchangeRate("EUR", Set.of("PLN", "CZK")))
            .expectNextMatches(s -> {
                Map<String, BigDecimal> actualRates = s.getExchangeRates();
                return actualRates.size() == 2
                    && actualRates.get("PLN").equals(new BigDecimal("1.11"))
                    && actualRates.get("CZK").equals(new BigDecimal("1.12"));
            })
            .verifyComplete();
    }

    @Test
    public void failOnGetExchangeRateWhenCacheIsEmpty() {
        Mockito.when(cache.getIfPresent("EUR")).thenReturn(Collections.emptyList());
        StepVerifier.create(currencyService.getExchangeRate("EUR", Set.of("USD")))
            .expectErrorMessage("No exchange rates for EUR currency")
            .verify();
    }
}
