package com.max.rest.proxy.exception;

import lombok.Data;

@Data
public class RestProxyConfigException extends RestProxyRuntimeException {

    public RestProxyConfigException(String message) {
        super(RestProxyExceptionCode.REST_PROXY_CONFIG, message);
    }

    public RestProxyConfigException(String message, Throwable cause) {
        super(RestProxyExceptionCode.REST_PROXY_CONFIG, message, cause);
    }
}
