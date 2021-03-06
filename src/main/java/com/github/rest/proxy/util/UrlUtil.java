package com.github.rest.proxy.util;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlUtil {
    /**
     * 简单的添加url的操作
     * 若url中若开头是 http 就不用添加前缀，若没有则添加 http://
     *
     * @param url
     * @return
     */
    public static String getServiceUrl(String url) {
        if (Strings.isNullOrEmpty(url) || url.startsWith("http")) {
            return url;
        }
        return "http://" + url;
    }
}