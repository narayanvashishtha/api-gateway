package com.example.api_gateway.circuitbreaker;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class CircuitBreakerConfig {

    //Maximum consecutive failures before circuit opens
    private final int failureThreshold = 5;
    // Minimum consecutive success before circuit closes
    private final int successThreshold = 2;
    //How long circuit remains OPEN before switching to HALF-OPEN (ms)
    private final long openStateDurationMs = 10_000;
    //Timeout per backend call (ms)
    private final int timeoutMs = 2000;
    //Number of retries before marking backend as failed
    private final int retryCount = 1;
}
