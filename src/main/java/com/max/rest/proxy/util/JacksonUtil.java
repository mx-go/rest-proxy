package com.max.rest.proxy.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.max.rest.proxy.exception.RestProxyRuntimeException;

import java.io.IOException;

public class JacksonUtil {
    private static final ObjectMapper propertyMapper;
    private static final ObjectMapper getterSetterMapper;

    static {
        propertyMapper = new ObjectMapper();
        propertyMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        propertyMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        propertyMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        propertyMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        propertyMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        getterSetterMapper = new ObjectMapper();
        getterSetterMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }


    public static String toJson(Object obj) {
        try {
            return getterSetterMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RestProxyRuntimeException(-1, e.getMessage());
        }
    }

    public static String toJson(Object obj, JsonInclude.Include include) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(include);
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RestProxyRuntimeException(-1, e.getMessage());
        }
    }


    public static String toJsonUseProperty(Object obj) {
        try {
            return propertyMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RestProxyRuntimeException(-1, e.getMessage());
        }
    }

    public static <E> E fromJson(String json, Class<E> clazz) {
        try {
            return propertyMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RestProxyRuntimeException(-1, e.getMessage());
        }
    }

    public static <E, R> R fromJsonOfGeneric(String json, Class<E> clazz) {
        try {
            return (R) propertyMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RestProxyRuntimeException(-1, e.getMessage());
        }
    }


    public static <E> E fromJson(String json, TypeReference<E> valueTypeRef) {
        try {
            return propertyMapper.readValue(json, valueTypeRef);
        } catch (IOException e) {
            throw new RestProxyRuntimeException(-1, e.getMessage());
        }
    }
}
