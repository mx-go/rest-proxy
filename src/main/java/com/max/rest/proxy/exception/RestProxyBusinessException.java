package com.max.rest.proxy.exception;

public class RestProxyBusinessException extends RestProxyRuntimeException {
    public RestProxyBusinessException(int code) {
        super(code);
    }

    public RestProxyBusinessException(int code, String message) {
        super(code, message);
    }

    public RestProxyBusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public RestProxyBusinessException(String code, String message) {
        super(code, message);
    }

    public RestProxyBusinessException(String  code, String message, Throwable cause) {
        super(code, message, cause);
    }
}