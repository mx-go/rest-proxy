package com.max.rest.proxy.annotation;

import java.lang.annotation.*;

/**
 * 替换URL中{}中的值。注解参数为Map类型
 * @see PathParam
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathParams {
}