package com.max.rest.proxy;

import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

@Data
public class RestServiceProxyFactoryBean<T> implements FactoryBean<T> {

    /**
     * RestServiceProxyFactory路径
     */
    private RestServiceProxyFactory factory;
    /**
     * 接口路径
     */
    private Class<T> type;

    @Override
    public T getObject() throws Exception {
        return factory.newRestServiceProxy(type);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
