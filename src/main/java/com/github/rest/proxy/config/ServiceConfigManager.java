package com.github.rest.proxy.config;

import com.github.rest.proxy.util.JsonUtil;
import com.github.rest.proxy.util.UrlUtil;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import com.github.rest.proxy.exception.RestProxyExceptionCode;
import com.github.rest.proxy.exception.RestProxyRuntimeException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class ServiceConfigManager {
    private String configLocation;
    protected volatile Map<String, RestServiceConfig> serviceConfigMaps = new HashMap<>();

    public static synchronized ServiceConfigManager build(String configLocation) {
        return new ServiceConfigManager(configLocation);
    }

    private ServiceConfigManager(String configLocation) {
        this.configLocation = configLocation;
        this.init();
    }

    private void init() {
        // 解析配置文件
        String content = "";
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources(configLocation);
            if (resources.length > 1) {
                log.info("rest-proxy config must be single");
                return;
            }
            for (Resource resource : resources) {
                content = IOUtils.toString(resource.getInputStream(), "utf-8");
            }
            if (Strings.isNullOrEmpty(content)) {
                log.error("rest-proxy config is empty. System.exit(0)");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        serviceConfigMaps = JsonUtil.fromJson(content, new TypeToken<Map<String, RestServiceConfig>>() {
        }.getType());
        initServiceKey(serviceConfigMaps);
    }

    private void initServiceKey(Map<String, RestServiceConfig> serviceConfigMaps) {
        if (MapUtils.isNotEmpty(serviceConfigMaps)) {
            serviceConfigMaps.forEach((key, serviceConfig) -> {
                //设置url
                serviceConfig.setAddress(UrlUtil.getServiceUrl(serviceConfig.getAddress()));
                serviceConfig.setServiceKey(key);
            });
        }
    }

    public ServiceConfig getServiceConfig(String serviceKey) {
        return getGrayServiceConfig(serviceKey);
    }

    private ServiceConfig getGrayServiceConfig(String serviceKey) {
        RestServiceConfig serviceConfig = serviceConfigMaps.get(serviceKey);
        if (serviceConfig == null) {
            throw new RestProxyRuntimeException(RestProxyExceptionCode.REST_PROXY_CONFIG, serviceKey + " Config is not found ,please check config");
        }
        return serviceConfigMaps.get(serviceKey).getServiceConfigs();
    }

}
