package com.example.api_gateway.circuitbreaker;

import lombok.Getter;

@Getter
public class CircuitBreakerState {

    private boolean open = false;
    private long lastOpenedTime = 0L;
    private int failureCount = 0;
    private int successCount = 0;

    public void open() {
        this.open = true;
        this.lastOpenedTime = System.currentTimeMillis();
        this.failureCount = 0;
        this.successCount = 0;
    }
    public void close() {
        this.open = false;
        this.failureCount = 0;
        this.successCount = 0;
    }

    public boolean isHalfOpen(){
        return open && canAttempt(0);
    }
    public boolean canAttempt(long openDurationMs) {
        return (System.currentTimeMillis() - lastOpenedTime) >= openDurationMs;
    }

    public void incrementFailure(){
        failureCount++;
    }
    public void incrementSuccess() {
        successCount++;
    }
    public void resetFailure(){
        failureCount = 0;
    }
}
