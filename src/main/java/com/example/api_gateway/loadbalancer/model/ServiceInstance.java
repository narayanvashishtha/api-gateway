package com.example.api_gateway.loadbalancer.model;

import lombok.Getter;

@Getter
public class ServiceInstance {
    private final String serviceName;
    private final String url;

    public ServiceInstance(String serviceName, String url){
        this.serviceName = serviceName;
        this.url = url;
    }

    @Override
    public String toString() {
        return serviceName + "@" + url;
    }
}
