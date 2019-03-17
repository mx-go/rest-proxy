package com.max.rest.proxy.config;

import lombok.Data;

import java.util.Map;

@Data
public class RestServiceConfig extends ServiceConfig{

    private Map<String,ServiceConfig> serviceConfigs;

    public ServiceConfig getServiceConfigs() {
        return this;
    }
}