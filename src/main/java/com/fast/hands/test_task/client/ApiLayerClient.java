package com.fast.hands.test_task.client;

import com.fast.hands.test_task.dto.apilayer.ApiLayer;
import reactor.core.publisher.Mono;

/**
 * Queries api_rate_v1 service.
 */
public interface ApiLayerClient {

    /**
     * Gets latest exchange rates for base currency USD.
     */
    Mono<ApiLayer> getLatestRates();
}
