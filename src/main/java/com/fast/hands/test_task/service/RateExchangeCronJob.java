package com.fast.hands.test_task.service;

import com.fast.hands.test_task.client.ApiLayerClient;
import com.fast.hands.test_task.config.ServiceConfig;
import com.fast.hands.test_task.dto.apilayer.ApiLayer;
import com.fast.hands.test_task.model.Currency;
import com.fast.hands.test_task.model.ExchangeRate;
import com.fast.hands.test_task.repository.CurrencyRepositoryDelegate;
import com.fast.hands.test_task.repository.ExchangeRateRepositoryDelegate;
import com.github.benmanes.caffeine.cache.Cache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Schedules a job to update the exchange rates for all available currencies that are present in the system.
 * Clears and populates the exchange-rate cache based on cron expression.
 *
 * @see ServiceConfig#currencyCache()
 * @see CurrencyService#getExchangeRate(String, Set)
 */
@Slf4j
@RequiredArgsConstructor
public class RateExchangeCronJob {

    private final String baseCurrency;

    private final ExchangeRateRepositoryDelegate exchangeRateRepository;
    private final CurrencyRepositoryDelegate currencyRepository;
    private final ApiLayerClient apiLayerClient;

    /**
     * Stores exchange rates per currency country code:
     * USD -> [USD->PLN(1.11), USD->EUR(1.0)]
     */
    private final Cache<String, List<ExchangeRate>> cache;

    private ExecutorService getExecutor() {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "rate-exchanger-" + count.getAndIncrement());
            }
        });
    }

    @Scheduled(cron = "${cron.schedule}")
    public void batchJob() {
        ExecutorService service = getExecutor();
        log.info("Processing started");
        long start = System.currentTimeMillis();
        try {
            CompletableFuture.runAsync(this::fetchCurrenciesAndCalculateCorrespondingRatesBasedOnSingleSourceCurrency, service)
                .exceptionally(e -> {
                    log.error("Error: {}", e.getMessage());
                    return null;
                })
                .join();
        } catch (CompletionException e) {
            log.error("Completed future: {}", e.getMessage());
        } catch (CancellationException e) {
            log.error("Cancelled future: {}", e.getMessage());
        } finally {
            service.shutdown();
        }
        long time = System.currentTimeMillis() - start;
        log.info("Processing finished. Execution time: {}ms", time);
    }

    private void fetchCurrenciesAndCalculateCorrespondingRatesBasedOnSingleSourceCurrency() {
        List<Currency> currenciesFromDb = currencyRepository.findAll();
        if (currenciesFromDb.size() < 2) {
            log.info("There are less than 2 currencies in the database. Stopping processing");
            return;
        }

        ApiLayer currenciesResp = apiLayerClient.getLatestRates().block();
        Map<String, BigDecimal> quotes = Optional.ofNullable(currenciesResp)
            .map(ApiLayer::getQuotes)
            .orElse(Collections.emptyMap());
        if (quotes.isEmpty()) {
            log.error("No public exchange rates. Stopping processing");
            return;
        }

        Set<Pair<Currency, Currency>> sourceTargetPair = getCurrenciesCombinationsGenerateTo(currenciesFromDb);
        Map<String, List<ExchangeRate>> groupedCurrencies = generateCurrencyRateMap(sourceTargetPair, quotes, currenciesResp.getTimestamp());
        cache.invalidateAll();
        cache.putAll(groupedCurrencies);

        List<ExchangeRate> list = groupedCurrencies.values().stream()
            .flatMap(Collection::stream)
            .toList();

        exchangeRateRepository.saveAll(list);
    }

    /**
     * Generates and groups currency - exchange rate map:
     * {@code
     * "USD" -> [USD->PLN(1.23)], [USD->EUR(1.11)], etc.
     * }
     * TODO: Add list of rates during one iteration
     * @return list of exchange rates per currency
     */
    private Map<String, List<ExchangeRate>> generateCurrencyRateMap(Set<Pair<Currency, Currency>> sourceTargetPair,
                                                                    Map<String, BigDecimal> quotes,
                                                                    OffsetDateTime timestamp) {
        Map<String, List<ExchangeRate>> rates = new HashMap<>();
        for (Pair<Currency, Currency> pair : sourceTargetPair) {
            Currency source = pair.getLeft();
            Currency target = pair.getRight();

            BigDecimal sourceToTargetRate = handleBaseCurrency(source, target, quotes);
            if (sourceToTargetRate == null) {
                log.error("Exchange rate is null for {} -> {} rate", source.getCurrencyCode(), target.getCurrencyCode());
                continue;
            }

            String key = source.getCurrencyCode();
            ExchangeRate value = new ExchangeRate()
                .setSource(source)
                .setTarget(target)
                .setRate(sourceToTargetRate)
                .setTimestamp(timestamp);

            rates.computeIfAbsent(key, v -> new ArrayList<>()).add(value);
        }
        return rates;
    }

    private BigDecimal handleBaseCurrency(Currency source, Currency target, Map<String, BigDecimal> quotes) {
        BigDecimal sourceRate = quotes.get(baseCurrency + source.getCurrencyCode());
        BigDecimal targetRate = quotes.get(baseCurrency + target.getCurrencyCode());
        BigDecimal sourceToTargetRate = null;

        if (source.getCurrencyCode().equals(baseCurrency)) {
            sourceToTargetRate = quotes.get(baseCurrency + target.getCurrencyCode());
        } else {
            if (baseCurrency.equals(target.getCurrencyCode())) {
                return sourceRate == null
                    ? null
                    : BigDecimal.ONE.divide(sourceRate, 6, RoundingMode.HALF_UP);
            }
            sourceToTargetRate = adjustRatesForNewBase(targetRate, sourceRate);
        }
        return sourceToTargetRate;
    }

    /**
     * Generates all possible currencies combination to calculate.
     * USD->EUR, means USD is a source and EUR is a target.
     *
     * @return Set<Pair < source, target>>
     */
    private Set<Pair<Currency, Currency>> getCurrenciesCombinationsGenerateTo(List<Currency> currenciesFromDb) {
        Set<Pair<Currency, Currency>> sourceTargetPair = new HashSet<>();
        for (int i = 0; i < currenciesFromDb.size(); i++) {
            for (int j = 0; j < currenciesFromDb.size(); j++) {
                if (i == j) {
                    continue;
                }
                Currency source = currenciesFromDb.get(i);
                Currency target = currenciesFromDb.get(j);
                sourceTargetPair.add(Pair.of(source, target));
            }
        }
        return sourceTargetPair;
    }

    /**
     * Converts source to target currencies with less precision, due to public-free exchange rate api restriction:
     * it only shows exchanges for one base currency: USD/EUR.
     * So in order to convert different currencies, you need to use two exchanges with one base and divide them.
     *
     * @param target target currency to get rate for
     * @param source target source currency to get rate by
     * @return
     */
    private BigDecimal adjustRatesForNewBase(BigDecimal target, BigDecimal source) {
        try {
            return target.divide(source, 6, RoundingMode.HALF_UP);
        } catch (ArithmeticException e) {
            throw new RuntimeException("Conversion error. Cannot get rate for: {}");
        }
    }
}
