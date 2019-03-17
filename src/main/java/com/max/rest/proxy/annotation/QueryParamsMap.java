package com.max.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * 请求参数多个值。注解到Map类型上。
 * @see QueryParam
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryParamsMap {
}