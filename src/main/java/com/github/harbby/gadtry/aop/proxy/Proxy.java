/*
 * Copyright (C) 2018 The GadTry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.harbby.gadtry.aop.proxy;

import com.github.harbby.gadtry.aop.ProxyRequest;
import com.github.harbby.gadtry.aop.mockgo.AopInvocationHandler;
import com.github.harbby.gadtry.base.Platform;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public class Proxy
{
    public static final Object[] EMPTY = new Object[0];

    private Proxy() {}

    public static <T> T proxy(ProxyRequest<T> request)
    {
        Class<?> superclass = request.getSuperclass();
        if (superclass.isInterface()) {
            return ProxyFactory.getJdkProxy().newProxyInstance(request);
        }
        else {
            checkState(!Modifier.isFinal(superclass.getModifiers()), superclass + " is final");
            //return ProxyFactory.getJavassistProxy().newProxyInstance(request);
            return ProxyFactory.getAsmProxy().newProxyInstance(request);
        }
    }

    public static AopInvocationHandler getInvocationHandler(Object instance)
    {
        ProxyFactory jdkProxy = ProxyFactory.getJdkProxy();
        if (jdkProxy.isProxyClass(instance.getClass())) {
            return (AopInvocationHandler) jdkProxy.getInvocationHandler(instance);
        }

        ProxyAccess proxyAccess = (ProxyAccess) instance;
        InvocationHandler handler = proxyAccess.getHandler();
        checkState(handler instanceof AopInvocationHandler, "instance not mock proxy");
        return (AopInvocationHandler) handler;
    }

    public static List<Method> filter(Class<?> proxyClass, List<Function<Method, Boolean>> filters)
    {
        List<Method> methods = new ArrayList<>();
        for (Field field : proxyClass.getDeclaredFields()) {
            if (field.getType() != Method.class || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Method method;
            if (ProxyAccess.class.isAssignableFrom(proxyClass)) {
                try {
                    method = (Method) field.get(null);
                }
                catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
            else {
                Unsafe unsafe = Platform.getUnsafe();
                method = (Method) unsafe.getObject(proxyClass, unsafe.staticFieldOffset(field));
            }
            boolean access = true;
            for (Function<Method, Boolean> filter : filters) {
                if (!filter.apply(method)) {
                    access = false;
                    break;
                }
            }
            if (access) {
                methods.add(method);
            }
        }
        return methods;
    }
}
