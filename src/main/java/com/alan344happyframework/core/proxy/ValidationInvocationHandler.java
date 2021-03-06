package com.alan344happyframework.core.proxy;


import com.alan344happyframework.core.PayIntegrate;
import com.alan344happyframework.util.ValidationUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author AlanSun
 * @date 2019/11/6 11:15
 */
public class ValidationInvocationHandler implements InvocationHandler {
    private Object target;

    public ValidationInvocationHandler(Object obj) {
        this.target = obj;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        ValidationUtil.ValidResult validResult = ValidationUtil.validateBean(args[0]);
        if (validResult.hasErrors()) {
            throw new IllegalArgumentException(validResult.getErrors());
        }
        Object invoke;
        try {
            invoke = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        return invoke;
    }

    public static PayIntegrate getInstance(InvocationHandler handler) {
        return (PayIntegrate) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{PayIntegrate.class}, handler);
    }
}
