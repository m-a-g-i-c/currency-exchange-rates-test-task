package com.fast.hands.test_task.service;

import com.fast.hands.test_task.converter.CurrencyConverter;
import com.fast.hands.test_task.dto.ApiCurrency;
import com.fast.hands.test_task.dto.ApiExchangeRate;
import com.fast.hands.test_task.model.ExchangeRate;
import com.fast.hands.test_task.repository.CurrencyRepositoryDelegate;
import com.github.benmanes.caffeine.cache.Cache;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class CurrencyService {
    private final CurrencyRepositoryDelegate currencyRepository;
    private final CurrencyConverter currencyConverter;
    private final Cache<String, List<ExchangeRate>> cache;

    public Mono<List<ApiCurrency>> getCurrencies() {
        log.info("getCurrencies");
        return Mono.fromSupplier(() -> currencyRepository.findAll().stream()
            .map(currencyConverter::convert)
            .toList()
        );
    }

    public Mono<ApiCurrency> createCurrency(ApiCurrency s) {
        log.info("Create currency: {}", s.getCurrencyCode());
        return Mono.fromSupplier(() -> currencyRepository.saveThrowable(
                currencyConverter.reverse(s)
            ))
            .map(currencyConverter::convert);
    }

    public Mono<ApiExchangeRate> getExchangeRate(String sourceCurrencyToGetExchangeRateFor, Set<String> targetCurrencies) {
        List<ExchangeRate> ratesCache = cache.getIfPresent(sourceCurrencyToGetExchangeRateFor);
        if (ratesCache == null || ratesCache.isEmpty()) {
            log.info("No exchange rates for {} currency", sourceCurrencyToGetExchangeRateFor);
            return Mono.error(new RuntimeException("No exchange rates for " + sourceCurrencyToGetExchangeRateFor + " currency"));
        }


        Map<String, BigDecimal> rates = ratesCache.stream()
            .filter(s -> targetCurrencies.contains(s.getTarget().getCurrencyCode()))
            .map(s -> Pair.of(s.getTarget().getCurrencyCode(), s.getRate()))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        ApiExchangeRate rate = new ApiExchangeRate()
            .setExchangeRates(rates)
            .setSource(sourceCurrencyToGetExchangeRateFor)
            .setTimestamp(ratesCache.get(0).getTimestamp());

        return Mono.just(rate);
    }
}
