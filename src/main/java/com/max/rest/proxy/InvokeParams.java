package com.max.rest.proxy;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.max.rest.proxy.annotation.*;
import com.max.rest.proxy.codec.AbstractRestCodeC;
import com.max.rest.proxy.codec.DefaultRestCodec;
import com.max.rest.proxy.config.ServiceConfig;
import com.max.rest.proxy.config.ServiceConfigManager;
import com.max.rest.proxy.exception.RestProxyConfigException;
import com.max.rest.proxy.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Slf4j
public class InvokeParams {
    /**
     * 请求的Url
     */
    private String serviceUrl;
    /**
     * 方法路径
     */
    private String methodPath;
    /**
     * 调用超时时间
     */
    private int socketTimeout;

    private ServiceConfig serviceConfig;
    /**
     * 方法协议类型
     */
    private String methodType;
    private AbstractRestCodeC codec;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();
    private Map<String, String> pathParams = new HashMap<>();
    private Object body;
    private Class resultClazz;
    private int retryTimes = 0;

    public static InvokeParams getInstance(ServiceConfigManager configManager,
                                           Method method, Object[] args) {
        RestResource restResource = method.getDeclaringClass().getAnnotation(RestResource.class);
        String serviceKey = restResource.value();
        InvokeParams invokeParams = new InvokeParams();
        Object methodType;
        String methodContentType;
        String uri;
        String codec;
        int socketReadTimeout;
        if ((methodType = method.getAnnotation(POST.class)) != null) {
            POST post = (POST) methodType;
            invokeParams.methodType = "POST";
            uri = post.value();
            methodContentType = post.contentType();
            codec = post.codec();
            if (!Strings.isNullOrEmpty(post.serviceKey())) {
                serviceKey = post.serviceKey();
            }
            socketReadTimeout = post.socketReadTimeoutSecond();

        } else if ((methodType = method.getAnnotation(GET.class)) != null) {
            GET get = (GET) methodType;
            invokeParams.methodType = "GET";
            uri = get.value();
            methodContentType = get.contentType();
            codec = get.codec();
            if (!Strings.isNullOrEmpty(get.serviceKey())) {
                serviceKey = get.serviceKey();
            }
            socketReadTimeout = get.socketReadTimeoutSecond();
        } else if ((methodType = method.getAnnotation(PUT.class)) != null) {
            PUT put = (PUT) methodType;
            invokeParams.methodType = "PUT";
            uri = put.value();
            methodContentType = put.contentType();
            codec = put.codec();
            if (!Strings.isNullOrEmpty(put.serviceKey())) {
                serviceKey = put.serviceKey();
            }
            socketReadTimeout = put.socketReadTimeoutSecond();
        } else if ((methodType = method.getAnnotation(DELETE.class)) != null) {
            DELETE delete = (DELETE) methodType;
            invokeParams.methodType = "DELETE";
            uri = delete.value();
            methodContentType = delete.contentType();
            codec = delete.codec();
            if (!Strings.isNullOrEmpty(delete.serviceKey())) {
                serviceKey = delete.serviceKey();
            }
            socketReadTimeout = delete.socketReadTimeout();
        } else {
            throw new RestProxyConfigException(method.getName() + " not have method type");
        }
        if (Strings.isNullOrEmpty(codec)) {
            codec = restResource.codec();
        }

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            Annotation[] annotations = p.getAnnotations();
            Annotation annotation;
            if (annotations.length == 0) {
                continue;
            } else {
                annotation = annotations[0];
            }
            if (args != null) {
                if (annotation instanceof Body) {
                    invokeParams.body = args[i];
                } else if (annotation instanceof PathParams && args[i] != null) {
                    invokeParams.pathParams.putAll((Map<String, String>) args[i]);
                } else if (annotation instanceof QueryParamsMap && args != null) {
                    invokeParams.queryParams.putAll((Map<String, String>) args[i]);
                } else if (annotation instanceof HeaderMap) {
                    invokeParams.headers.putAll((Map<String, String>) args[i]);
                } else if (annotation instanceof QueryParam && args[i] != null) {
                    invokeParams.queryParams.put(((QueryParam) annotation).value(), (String) args[i]);
                } else if (annotation instanceof PathParam && args[i] != null) {
                    invokeParams.pathParams.put(((PathParam) annotation).value(), (String) args[i]);
                } else if (annotation instanceof HeaderParam && args[i] != null) {
                    invokeParams.headers.put(((HeaderParam) annotation).value(), (String) args[i]);
                }
            }
        }
        ServiceConfig config = configManager.getServiceConfig(serviceKey);
        invokeParams.serviceConfig = config;

        invokeParams.resultClazz = method.getReturnType();
        invokeParams.setMethodPath(method.getName());
        invokeParams.setCodec(getCodeC(codec));
        invokeParams.setSocketTimeout(Math.max(socketReadTimeout * 1000, config.getSocketTimeOut()));

        invokeParams.serviceUrl = getServiceURL(config, uri, invokeParams.pathParams, invokeParams.queryParams);
        String globContentType = restResource.contentType();
        if (!Strings.isNullOrEmpty(globContentType)) {
            invokeParams.headers.put("Content-Type", globContentType);
        }
        if (!Strings.isNullOrEmpty(methodContentType)) {
            invokeParams.headers.put("Content-Type", methodContentType);
        }
        return invokeParams;
    }

    static Pattern p = Pattern.compile("(\\{[^{}]+\\})+");

    public String getServiceIP() {
        return serviceConfig.getAddress();
    }

    public String getServiceKey() {
        return serviceConfig.getServiceKey();
    }

    public String getServiceName() {
        return serviceConfig.getServiceName();
    }

    public int getSocketTimeOut() {
        return socketTimeout;
    }

    public int getConnectionTimeOut() {
        return serviceConfig.getConnectionTimeOut();
    }

    static String getServiceURL(ServiceConfig config, String uri, Map<String, String> pathParams, Map<String, String> queryParams) {
        String url = getResourceAddress(config, uri);
        String serviceUrl = url.replaceAll(" ", "");
        if (pathParams != null) {
            Matcher m = p.matcher(url);
            while (m.find()) {
                String pathPlaceHold = m.group();
                String pathPlaceHoldWithOutChar = pathPlaceHold.substring(1, pathPlaceHold.length() - 1);
                String value = pathParams.get(pathPlaceHoldWithOutChar);
                if (Strings.isNullOrEmpty(value)) {
                    throw new RestProxyConfigException(url + "参数" + pathPlaceHoldWithOutChar + "在参数列表中不存在");
                }
                serviceUrl = serviceUrl.replace(pathPlaceHold, value);
            }
        }
        if (queryParams != null) {
            serviceUrl += map2QueryParams(queryParams);
        }
        return serviceUrl;
    }

    static String map2QueryParams(Map<String, String> map) {
        StringBuffer sb = new StringBuffer("?");
        map.forEach((k, v) -> {
            sb.append(k).append("=").append(v).append("&");
        });
        if (sb.length() > 1) {
            return sb.toString().substring(0, sb.length() - 1);
        } else {
            return "";
        }
    }

    private static String getResourceAddress(ServiceConfig config, String restUri) {
        String address = config.getAddress();
        if (address == null) {
            address = "";
        }
        String template = "%s";
        if (restUri.startsWith("/")) {
            template += "%s";
        } else {
            template += "/%s";
        }
        return String.format(template, address, restUri);
    }

    private final static Map<String, AbstractRestCodeC> serviceCodeCMaps = Maps.newConcurrentMap();

    private static AbstractRestCodeC getCodeC(final String className) {
        if (Strings.isNullOrEmpty(className)) {
            if (Objects.isNull(serviceCodeCMaps.get("default"))) {
                serviceCodeCMaps.put("default", new DefaultRestCodec());
            }
            return serviceCodeCMaps.get("default");
        }
        AbstractRestCodeC rst = serviceCodeCMaps.computeIfAbsent(className, (String key) -> {
            try {
                log.info("init Codec :{}", className);
                return (AbstractRestCodeC) Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new RestProxyConfigException("className init error:" + className);
            }
        });
        return rst;
    }

    private String bodyString;

    public String getBodyString() {
        if (bodyString == null) {
            return bodyString = JsonUtil.toJson(body);
        }
        return bodyString;
    }

    public String getHeaderString() {
        return "[" + Joiner.on(",").withKeyValueSeparator(":").useForNull("").join(this.headers) + "]";
    }

    public void incrRetryTimes() {
        this.setRetryTimes(this.getRetryTimes() + 1);
    }
}
