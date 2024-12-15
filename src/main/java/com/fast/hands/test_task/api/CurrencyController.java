package com.fast.hands.test_task.api;

import com.fast.hands.test_task.dto.ApiCurrency;
import com.fast.hands.test_task.dto.ApiExchangeRate;
import com.fast.hands.test_task.service.CurrencyService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService service) {
        currencyService = service;
    }

    @RequestMapping(
        method = RequestMethod.POST,
        value = {"/currencies"},
        produces = {"application/json"},
        consumes = {"application/json"}
    )
    public Mono<ResponseEntity<ApiCurrency>> addCurrency(@RequestBody Mono<ApiCurrency> apiCurrency) {
        return apiCurrency
            .flatMap(s -> currencyService.createCurrency(s))
            .map(s -> new ResponseEntity<>(s, HttpStatus.CREATED));
    }

    @RequestMapping(
        method = RequestMethod.GET,
        value = {"/currencies"},
        produces = {"application/json"}
    )
    public Mono<ResponseEntity<List<ApiCurrency>>> getCurrencies() {
        return currencyService.getCurrencies()
            .map(s -> new ResponseEntity<>(s, HttpStatus.OK));
    }

    @RequestMapping(
        method = RequestMethod.GET,
        value = {"/currency/{currency_code}/exchange-rates"},
        produces = {"application/json"}
    )
    public Mono<ResponseEntity<ApiExchangeRate>> getExchangeRates(@PathVariable(value = "currency_code", required = true)
                                                                  String sourceCurrencyToGetExchangeRateFor,
                                                                  @RequestParam("target") List<String> targetCurrencies) {
        Set<String> currencies = new HashSet<>(targetCurrencies);
        return currencyService.getExchangeRate(sourceCurrencyToGetExchangeRateFor, currencies)
            .map(s -> ResponseEntity.ok(s));
    }
}
