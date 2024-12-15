package com.fast.hands.test_task.functional;

import com.fast.hands.test_task.BaseTest;
import com.fast.hands.test_task.model.Currency;
import com.fast.hands.test_task.repository.CurrencyRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import util.JsonMatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCurrenciesTest extends BaseTest {

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.execute("truncate table currency, exchange_rate;");
    }

    @Test
    public void getCurrenciesSuccessfully() {
        currencyRepository.saveAll(List.of(new Currency()
                .setCurrencyCode("UAH"),
            new Currency()
                .setCurrencyCode("CZK")));

        assertThat(jdbcTemplate.queryForObject("select count(*) from currency", Integer.class)).isEqualTo(2);

        RestAssured.given()
            .accept(ContentType.JSON)
            .get("/currencies")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(ContentType.JSON)
            .body(new JsonMatcher("""
                [{"currency_code":"UAH"},{"currency_code":"CZK"}]
                """));
    }

    @Test
    public void getEmptyListWhenNoCurrenciesArePresent() {
        RestAssured.given()
            .accept(ContentType.JSON)
            .get("/currencies")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(ContentType.JSON)
            .body(new JsonMatcher("""
                []
                """));
    }
}
