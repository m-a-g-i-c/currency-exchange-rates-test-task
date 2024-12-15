package com.fast.hands.test_task.functional;

import com.fast.hands.test_task.BaseTest;
import com.fast.hands.test_task.mappers.CurrencyRowMapper;
import com.fast.hands.test_task.model.Currency;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import util.JsonMatcher;

public class PostCurrenciesTest extends BaseTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.execute("truncate table currency, exchange_rate;");
    }

    @Test
    public void createCurrencySuccessfully() {
        RestAssured.given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "currency_code": "UAH",
                  "name": "Ukrainian hryvnia",
                  "description": "no description"
                }
                """)
            .post("/currencies")
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .contentType(ContentType.JSON)
            .body(new JsonMatcher("""
                {
                  "currency_code": "UAH",
                  "name": "Ukrainian hryvnia",
                  "description": "no description"
                }
                """));

        Currency actual = jdbcTemplate.queryForObject("select * from currency where currency_code='UAH'", new CurrencyRowMapper());
        assertThat(actual).satisfies(s -> {
            assertThat(s.getCurrencyCode()).isEqualTo("UAH");
            assertThat(s.getName()).isEqualTo("Ukrainian hryvnia");
            assertThat(s.getDescription()).isEqualTo("no description");
            assertThat(s.getId()).isNotNull();
        });
    }

    @Test
    public void failToCreateCurrencyWithoutCurrencyCode() {
        RestAssured.given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "name": "Ukrainian hryvnia",
                  "description": "no description"
                }
                """)
            .post("/currencies")
            .then()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        assertThat(jdbcTemplate.queryForObject("select count(*) from currency", Integer.class)).isEqualTo(0);
    }

    @Test
    public void failToCreateCurrencyWithCodeExtendingLimitLength() {
        RestAssured.given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "currency_code": "760120",
                  "name": "Fake news",
                  "description": "Fake news"
                }
                """)
            .post("/currencies")
            .then()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        assertThat(jdbcTemplate.queryForObject("select count(*) from currency", Integer.class)).isEqualTo(0);
    }

    @Test
    public void failToCreateDuplicateCurrency() {
        RestAssured.given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "currency_code": "USD",
                  "name": "United States Dollar"
                }
                """)
            .post("/currencies")
            .then()
            .statusCode(HttpStatus.SC_CREATED);

        assertThat(jdbcTemplate.queryForObject("select count(*) from currency", Integer.class)).isEqualTo(1);

        RestAssured.given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "currency_code": "USD",
                  "name": "United States Dollar"
                }
                """)
            .post("/currencies")
            .then()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThat(jdbcTemplate.queryForObject("select count(*) from currency", Integer.class)).isEqualTo(1);
    }

}
