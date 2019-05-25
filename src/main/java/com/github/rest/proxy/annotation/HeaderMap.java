package com.github.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * HTTP请求header中存放的Map
 * eg.
 * Map headerMap = new HashMap();
 * headerMap.put("id",id);
 * (@HeaderMap headerMap map);
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HeaderMap {

}