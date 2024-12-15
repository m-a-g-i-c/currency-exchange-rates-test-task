package com.fast.hands.test_task.functional;

import com.fast.hands.test_task.BaseTest;
import com.fast.hands.test_task.model.Currency;
import com.fast.hands.test_task.model.ExchangeRate;
import com.fast.hands.test_task.repository.CurrencyRepository;
import com.fast.hands.test_task.repository.ExchangeRateRepository;
import com.fast.hands.test_task.service.RateExchangeCronJob;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.tomakehurst.wiremock.client.WireMock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import util.JsonMatcher;

public class GetExchangeRateTest extends BaseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExchangeRateRepository repo;
    @Autowired
    private CurrencyRepository currencyRepo;
    @Autowired
    private RateExchangeCronJob cronJob;
    @Autowired
    private Cache<String, List<ExchangeRate>> cache;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.execute("truncate table currency, exchange_rate;");
        cache.invalidateAll();
    }

    @Test
    public void getExchangeRateSuccessfully() throws IOException, InterruptedException {
        currencyRepo.saveAll(List.of(
            new Currency("EUR"),
            new Currency("PLN"),
            new Currency("USD"),
            new Currency("GBP"),
            new Currency("UAH")
        ));

        String body = IOUtils.resourceToString("/stub/get_all_currencies_response.json", StandardCharsets.UTF_8);
        stubFor(get(urlPathEqualTo("/live"))
            .withHeader("Accept", WireMock.equalTo("application/json"))
            .withQueryParam("access_key", equalTo("api_key"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(body)));

        cronJob.batchJob();

        Thread.sleep(2_000L);

        RestAssured.given()
            .accept(ContentType.JSON)
            .queryParam("target", "EUR,PLN")
            .get("/currency/UAH/exchange-rates")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(ContentType.JSON)
            .body(new JsonMatcher("""
                {
                  "source": "UAH",
                  "timestamp": "${json-unit.any-string}",
                  "exchange_rates": {
                      "EUR": 0.022802,
                      "PLN": 0.097360
                  }
                }
                """));

        WireMock.verify(1, getRequestedFor(urlPathEqualTo("/live")));
    }

    @Test
    public void failToGetExchangeRatesWhenTargetRatesAreNotSpecified() throws IOException, InterruptedException {
        currencyRepo.saveAll(List.of(
            new Currency("EUR"),
            new Currency("PLN"),
            new Currency("USD"),
            new Currency("GBP"),
            new Currency("UAH")
        ));

        String body = IOUtils.resourceToString("/stub/get_all_currencies_response.json", StandardCharsets.UTF_8);
        stubFor(get(urlPathEqualTo("/live"))
            .withHeader("Accept", WireMock.equalTo("application/json"))
            .withQueryParam("access_key", equalTo("api_key"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(body)));

        cronJob.batchJob();

        Thread.sleep(2_000L);

        RestAssured.given()
            .accept(ContentType.JSON)
            .get("/currency/UAH/exchange-rates")
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .contentType(ContentType.JSON);

        WireMock.verify(1, getRequestedFor(urlPathEqualTo("/live")));
    }

    @Test
    public void failToGetExchangeRateWheNoCurrenciesArePresentInDb() throws IOException, InterruptedException {
        String body = IOUtils.resourceToString("/stub/get_all_currencies_response.json", StandardCharsets.UTF_8);
        stubFor(get(urlPathEqualTo("/live"))
            .withHeader("Accept", WireMock.equalTo("application/json"))
            .withQueryParam("access_key", equalTo("api_key"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(body)));

        RestAssured.given()
            .accept(ContentType.JSON)
            .queryParam("target", "EUR,PLN")
            .get("/currency/UAH/exchange-rates")
            .then()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        WireMock.verify(0, getRequestedFor(urlPathEqualTo("/live")));
    }
}
