package com.example.api_gateway.loadbalancer.service;

import com.example.api_gateway.loadbalancer.registry.ServiceRegistry;
import com.example.api_gateway.loadbalancer.model.ServiceInstance;
import com.example.api_gateway.loadbalancer.strategy.RoundRobinStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoadBalancerService {

    private final ServiceRegistry registry;
    private final RoundRobinStrategy strategy;

    public LoadBalancerService(ServiceRegistry registry, RoundRobinStrategy strategy) {
        this.registry = registry;
        this.strategy = strategy;
    }

    //Returns null if no instance available.
    public ServiceInstance chooseInstance(String serviceName) {
        List<ServiceInstance> instances = registry.getInstances(serviceName);
        if (instances.isEmpty()) return null;
        return strategy.select(serviceName, instances);
    }

}
