package com.github.rest.proxy.exception;

import lombok.Data;

@Data
public class RestProxyRuntimeException extends RuntimeException {
    private int code;

    public RestProxyRuntimeException(int code) {
        this.code = code ;
    }

    public RestProxyRuntimeException(int code, String message) {
        super(message);
        this.code = code ;
    }

    public RestProxyRuntimeException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code ;
    }


    public RestProxyRuntimeException(String code) {
        this.code = Integer.valueOf(code);
    }

    public RestProxyRuntimeException(String code, String message) {
        super(message);
        this.code = Integer.valueOf(code);
    }

    public RestProxyRuntimeException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = Integer.valueOf(code);
    }


}
