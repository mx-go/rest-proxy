package com.max.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * 替换URL中{}中的值。
 * eg.
 * /get/{id}
 * (@PathParam('id') String id)
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathParam {
    String value();
}