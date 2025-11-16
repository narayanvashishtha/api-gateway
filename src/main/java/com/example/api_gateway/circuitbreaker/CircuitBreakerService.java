package com.example.api_gateway.circuitbreaker;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CircuitBreakerService {

    private final CircuitBreakerConfig circuitBreakerConfig;
    Map<String, CircuitBreakerState> serviceState = new ConcurrentHashMap<>();

    public CircuitBreakerService(CircuitBreakerConfig circuitBreakerConfig){
        this.circuitBreakerConfig = circuitBreakerConfig;
    }

    private String key(String serviceName, String backendUrl) {
        return serviceName + "@" + backendUrl;
    }

    public boolean allowRequest(String serviceName, String backendUrl) {
        String k = key(serviceName, backendUrl);
        CircuitBreakerState state = serviceState.computeIfAbsent(k, s -> new CircuitBreakerState());

        if (!state.isOpen()) return true;

        //If open, check if cooldown expired → half-open (allow a test)
        return state.canAttempt(circuitBreakerConfig.getOpenStateDurationMs());
    }

    public void recordFailure(String serviceName, String backendUrl){
        String k = key(serviceName, backendUrl);
        CircuitBreakerState state = serviceState.computeIfAbsent(k, s -> new CircuitBreakerState());

        state.incrementFailure();

        if(state.getFailureCount() >= circuitBreakerConfig.getFailureThreshold()){
            state.open();
        }
    }
    public void recordSuccess(String serviceName, String backendUrl) {
        String k = key(serviceName, backendUrl);
        CircuitBreakerState state = serviceState.computeIfAbsent(k, s -> new CircuitBreakerState());

        if (!state.isOpen()) return;

        state.incrementSuccess();

        if (state.getSuccessCount() >= circuitBreakerConfig.getSuccessThreshold()) {
            state.close();
            state.resetFailure();
        }
    }
}
