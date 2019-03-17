package com.max.rest.proxy.exception;

import lombok.Data;

@Data
public class RestProxyInvokeException extends RestProxyRuntimeException {

    public RestProxyInvokeException(String message) {
        super(RestProxyExceptionCode.REST_PROXY_INVOKE_ROMOTE_SERVICE, message);
    }

    public RestProxyInvokeException(String message, Throwable cause) {
        super(RestProxyExceptionCode.REST_PROXY_INVOKE_ROMOTE_SERVICE, message, cause);
    }
    public RestProxyInvokeException(Throwable cause) {
        super(RestProxyExceptionCode.REST_PROXY_INVOKE_ROMOTE_SERVICE, null,cause);
    }
}
