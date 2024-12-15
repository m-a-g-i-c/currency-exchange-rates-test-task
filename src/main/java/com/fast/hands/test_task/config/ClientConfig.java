package com.fast.hands.test_task.config;

import com.fast.hands.test_task.client.ApiLayerClient;
import com.fast.hands.test_task.client.ApiLayerClientImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {

    private final WebClient webClient;

    @Value("${com.fast.hands.test_task.client.apilayer.base-url}")
    private String baseUrl;

    @Value("${com.fast.hands.test_task.client.apilayer.api_key}")
    private String apiKey;

    @Bean("com.spribe.test_task.config.ApiLayerConfig.apiLayerClient")
    public ApiLayerClient apiLayerClient() {
        return new ApiLayerClientImpl(baseUrl, apiKey, webClient);
    }
}
