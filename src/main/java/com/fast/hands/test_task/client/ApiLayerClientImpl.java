package com.fast.hands.test_task.client;

import com.fast.hands.test_task.dto.apilayer.ApiLayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class ApiLayerClientImpl implements ApiLayerClient {

    private final String baseUrl;
    private final String apiKey;
    private final WebClient webClient;

    public Mono<ApiLayer> getLatestRates() {
        return webClient.get()
                .uri(s -> UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path("/live")
                        .queryParam("access_key", apiKey)
                        .build()
                        .toUri())
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(r -> {
                    if (r.statusCode().isError()) {
                        log.error("Unable to fetch exchange rate");
                        return r.createError();
                    }
                    return r.bodyToMono(new ParameterizedTypeReference<>() {
                    });
                });
    }
}
