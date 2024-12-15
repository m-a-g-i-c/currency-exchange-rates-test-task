package com.fast.hands.test_task;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    // On windows possible "connection refused" errors. TESTCONTAINERS_RYUK_DISABLED=true does the trick
    private static final PostgreSQLContainer<?> POSTGRES_CONTEINER = new PostgreSQLContainer<>("postgres:13-alpine");
    protected static final String JDBC_URL;

    @LocalServerPort
    private int port;

    static {
        POSTGRES_CONTEINER.start();
        JDBC_URL = POSTGRES_CONTEINER.getJdbcUrl();
        System.setProperty("spring.datasource.url", JDBC_URL);
    }

    @BeforeEach
    public void beforeEachBase() {

        RestAssured.reset();
        WireMock.reset();
        WireMock.resetAllScenarios();

        RestAssured.port = port;
    }
}
