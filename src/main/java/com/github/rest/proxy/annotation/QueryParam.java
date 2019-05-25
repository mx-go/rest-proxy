package com.github.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * 请求参数。
 * eg.
 * (@QueryParam("id") String id)
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryParam {
    String value();
}