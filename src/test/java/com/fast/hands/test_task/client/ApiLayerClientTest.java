package com.fast.hands.test_task.client;

import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

public class ApiLayerClientTest {

    private WebClient webClient = Mockito.mock(WebClient.class);
    private ApiLayerClient apiLayerClient = new ApiLayerClientImpl("baseUrl", "apiKey", webClient);

    void test() {

//        apiLayerClient.getLatestRates()
    }
}
