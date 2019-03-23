package com.max.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * 注解到接口
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestResource {
    String value();

    String desc() default "";

    String codec() default "com.max.rest.proxy.codec.DefaultRestCodec";

    String contentType() default "";
}