package com.max.rest.proxy.util;

import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class StringUtil {

    public static String  format(String temp, Object... objects) {
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                Object item = objects[i];
                objects[i] = Objects.isNull(item) || "null".equals(item) ? "" : item;
            }
        }else{
            objects=new Object[]{""};
        }
        return String.format(temp, objects);
    }
}
