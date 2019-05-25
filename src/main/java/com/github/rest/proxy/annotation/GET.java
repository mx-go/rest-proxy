package com.github.rest.proxy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {
    String value() default "";

    String desc() default "";

    String contentType() default "";

    String serviceKey() default "";

    String codec() default "";

    int socketReadTimeoutSecond() default 0;//s
}