package com.max.rest.proxy.config;

import lombok.Data;

/**
 * json文件中可配置的字段
 *
 * @author max
 */
@Data
public class ServiceConfig {

    private String serviceKey;
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 请求地址
     */
    private String address;
    /**
     * default 5000ms
     */
    private int socketTimeOut;
    /**
     * default 2000ms
     */
    private int connectionTimeOut;
}