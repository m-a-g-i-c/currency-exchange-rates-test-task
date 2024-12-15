package com.fast.hands.test_task.service;

import com.fast.hands.test_task.client.ApiLayerClient;
import com.fast.hands.test_task.dto.apilayer.ApiLayer;
import com.fast.hands.test_task.model.Currency;
import com.fast.hands.test_task.model.ExchangeRate;
import com.fast.hands.test_task.repository.CurrencyRepositoryDelegate;
import com.fast.hands.test_task.repository.ExchangeRateRepositoryDelegate;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class RateExchangeCronJobTest {

    private final ExchangeRateRepositoryDelegate exchangeRepo = Mockito.mock(ExchangeRateRepositoryDelegate.class);
    private final CurrencyRepositoryDelegate currencyRepo = Mockito.mock(CurrencyRepositoryDelegate.class);
    private final ApiLayerClient apiLayerClient = Mockito.mock(ApiLayerClient.class);
    private final Cache<String, List<ExchangeRate>> cache = Caffeine.newBuilder().build();

    private RateExchangeCronJob rateExchangeCronJob = new RateExchangeCronJob("USD",
        exchangeRepo, currencyRepo, apiLayerClient, cache
    );

    @BeforeEach
    void beforeEach() {
        cache.invalidateAll();
    }

    @Test
    public void updateExchangeRatesBasedOnAvailableRatesSuccessfully() {
        when(apiLayerClient.getLatestRates())
            .thenReturn(Mono.just(getApiLayerResponse()));
        when(currencyRepo.findAll())
            .thenReturn(List.of(
                new Currency("EUR"),
                new Currency("UAH"),
                new Currency("PLN")
            ));

        rateExchangeCronJob.batchJob();

        ArgumentCaptor<List<ExchangeRate>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(exchangeRepo, times(1)).saveAll(captor.capture());
        Mockito.verify(apiLayerClient, times(1)).getLatestRates();

        List<ExchangeRate> actual = captor.getValue();
        assertThat(actual)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "timestamp")
            .containsExactlyInAnyOrderElementsOf(getExpectedExchangeRateCombinations());

        assertThat(cache.estimatedSize()).isEqualTo(3);
        assertThat(cache.getIfPresent("UAH")).hasSize(2);
    }

    @Test
    public void updateExchangeRatesWhenSourceIsBaseCurrency() {
        when(apiLayerClient.getLatestRates())
            .thenReturn(Mono.just(getApiLayerResponse()));
        when(currencyRepo.findAll())
            .thenReturn(List.of(
                new Currency("USD"),
                new Currency("UAH"),
                new Currency("PLN")
            ));

        rateExchangeCronJob.batchJob();
        ArgumentCaptor<List<ExchangeRate>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(exchangeRepo, times(1)).saveAll(captor.capture());
        Mockito.verify(apiLayerClient, times(1)).getLatestRates();

        List<ExchangeRate> actual = captor.getValue();
        assertThat(actual)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "timestamp")
            .containsExactlyInAnyOrderElementsOf(getExpectedRateCombinationsWhenBaseCurrencyIsPresent());

        assertThat(cache.estimatedSize()).isEqualTo(3);
    }

    @Test
    public void doNothingWhenNoCurrenciesAreStoredInDb() {
        when(currencyRepo.findAll())
            .thenReturn(Collections.emptyList());

        rateExchangeCronJob.batchJob();

        ArgumentCaptor<List<ExchangeRate>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(exchangeRepo, times(0)).saveAll(captor.capture());
        Mockito.verify(apiLayerClient, times(0)).getLatestRates();
        assertThat(cache.estimatedSize()).isEqualTo(0);
    }

    @Test
    public void doNothingWhenOnlyOneCurrencyIsPresentInDb() {
        when(currencyRepo.findAll())
            .thenReturn(List.of(new Currency("USD")));

        rateExchangeCronJob.batchJob();

        ArgumentCaptor<List<ExchangeRate>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(exchangeRepo, times(0)).saveAll(captor.capture());
        Mockito.verify(apiLayerClient, times(0)).getLatestRates();
        assertThat(cache.estimatedSize()).isEqualTo(0);
    }

    private List<ExchangeRate> getExpectedRateCombinationsWhenBaseCurrencyIsPresent() {
        return List.of(new ExchangeRate()
                .setSource(new Currency("USD"))
                .setTarget(new Currency("UAH"))
                .setRate(new BigDecimal("41.746745")),
            new ExchangeRate()
                .setSource(new Currency("UAH"))
                .setTarget(new Currency("USD"))
                .setRate(new BigDecimal("0.023954")),
            new ExchangeRate()
                .setSource(new Currency("USD"))
                .setTarget(new Currency("PLN"))
                .setRate(new BigDecimal("4.064446")),
            new ExchangeRate()
                .setSource(new Currency("PLN"))
                .setTarget(new Currency("USD"))
                .setRate(new BigDecimal("0.246036")),
            new ExchangeRate()
                .setSource(new Currency("UAH"))
                .setTarget(new Currency("PLN"))
                .setRate(new BigDecimal("0.097360")),
            new ExchangeRate()
                .setSource(new Currency("PLN"))
                .setTarget(new Currency("UAH"))
                .setRate(new BigDecimal("10.271202"))
        );
    }

    private List<ExchangeRate> getExpectedExchangeRateCombinations() {
        return List.of(new ExchangeRate()
                .setSource(new Currency("EUR"))
                .setTarget(new Currency("UAH"))
                .setRate(new BigDecimal("43.855078")),
            new ExchangeRate()
                .setSource(new Currency("EUR"))
                .setTarget(new Currency("PLN"))
                .setRate(new BigDecimal("4.269712")),
            new ExchangeRate()
                .setSource(new Currency("UAH"))
                .setTarget(new Currency("EUR"))
                .setRate(new BigDecimal("0.022802")),
            new ExchangeRate()
                .setSource(new Currency("UAH"))
                .setTarget(new Currency("PLN"))
                .setRate(new BigDecimal("0.097360")),
            new ExchangeRate()
                .setSource(new Currency("PLN"))
                .setTarget(new Currency("UAH"))
                .setRate(new BigDecimal("10.271202")),
            new ExchangeRate()
                .setSource(new Currency("PLN"))
                .setTarget(new Currency("EUR"))
                .setRate(new BigDecimal("0.234208"))
        );
    }

    private ApiLayer getApiLayerResponse() {
        return new ApiLayer()
            .setSource("USD")
            .setTimestamp(OffsetDateTime.now())
            .setQuotes(Map.of(
                "USDEUR", new BigDecimal("0.951925"),
                "USDUAH", new BigDecimal("41.746745"),
                "USDPLN", new BigDecimal("4.064446")
            ));
    }
}
