package com.max.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * HTTP请求中header中的值。注解单个字段。
 * eg. (@HeaderParam("id") String id)
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HeaderParam {
    String value();
}