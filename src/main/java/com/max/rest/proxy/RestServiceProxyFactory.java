package com.max.rest.proxy;

import com.google.common.reflect.Reflection;
import com.max.rest.proxy.config.ServiceConfigManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author max
 */
@Slf4j
@Data
public class RestServiceProxyFactory {

    private final static RestClient restClient = new RestClient();

    private ServiceConfigManager configManager;

    /**
     * 配置文件路径
     */
    private String location;

    public RestServiceProxyFactory() {

    }

    public void init() {
        configManager = ServiceConfigManager.build(location);
    }


    public <T> T newRestServiceProxy(Class<T> clazz) {
        return Reflection.newProxy(clazz, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }

                if (method.isDefault()) {
                    MethodHandle methodHandler = RestServiceProxyFactory.this.getMethodHandler(method);
                    return methodHandler.bindTo(proxy).invokeWithArguments(args);
                }

                InvokeParams invokeParams = InvokeParams.getInstance(configManager, method, args);

                Object ret = null;
                try {
                    ret = restClient.invoke(invokeParams);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return ret;
            }
        });
    }

    private MethodHandle getMethodHandler(Method method)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, java.lang.reflect.InvocationTargetException {

        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        constructor.setAccessible(true);

        Class<?> declaringClass = method.getDeclaringClass();
        int allModes = (MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE);
        return constructor.newInstance(declaringClass, allModes)
                .unreflectSpecial(method, declaringClass);
    }
}
