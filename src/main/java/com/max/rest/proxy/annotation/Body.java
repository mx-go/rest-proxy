package com.max.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * HTTP请求中的body，注解在对象前
 * eg. (@body Args args)
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Body {
}