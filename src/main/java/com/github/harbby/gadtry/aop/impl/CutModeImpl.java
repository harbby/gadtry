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
package com.github.harbby.gadtry.aop.impl;

import com.github.harbby.gadtry.aop.CutMode;
import com.github.harbby.gadtry.aop.ProxyContext;
import com.github.harbby.gadtry.aop.model.MethodInfo;
import com.github.harbby.gadtry.base.Lazys;
import com.github.harbby.gadtry.function.exception.Consumer;
import com.github.harbby.gadtry.function.exception.Function;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.function.Supplier;

public class CutModeImpl<T>
        implements CutMode<T>
{
    private final Class<?> interfaces;
    private final T instance;
    private final ClassLoader loader;

    private final Proxy proxy;
    private final Supplier<Function<MethodInfo, Boolean>> methodFilter =
            Lazys.goLazy(this::getMethodFilter);

    protected CutModeImpl(Class<?> interfaces, T instance, Proxy proxy)
    {
        this.interfaces = interfaces;
        this.instance = instance;
        this.loader = instance.getClass().getClassLoader();
        this.proxy = proxy;
    }

    public static <T> CutModeImpl<T> of(Class<T> interfaces, T instance, Proxy proxy)
    {
        return new CutModeImpl<>(interfaces, instance, proxy);
    }

    protected Function<MethodInfo, Boolean> getMethodFilter()
    {
        return (method) -> true;
    }

    private T getProxy(InvocationHandler handler, Class<?> interfaces)
    {
        Function<MethodInfo, Boolean> filter = this.methodFilter.get();
        return getProxyStatic(proxy, loader, interfaces, handler, instance, filter);
    }

    private static <T> T getProxyStatic(
            Proxy proxyContext,
            ClassLoader loader,
            Class<?> interfaces,
            InvocationHandler handler,
            T instance,
            Function<MethodInfo, Boolean> filter)
    {
        InvocationHandler proxyHandler = filter != null ?
                (InvocationHandler & Serializable) (proxy, method, args) -> {
                    if (filter.apply(MethodInfo.of(method))) {
                        return handler.invoke(proxy, method, args);
                    }
                    else {
                        return method.invoke(instance, args);
                    }
                }
                : handler;

        return proxyContext.getProxy(loader, interfaces, proxyHandler);
    }

    @Override
    public T around(Function<ProxyContext, Object> aroundHandler)
    {
        InvocationHandler handler = aroundStatic(aroundHandler, instance);
        return this.getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler aroundStatic(Function<ProxyContext, Object> aroundHandler, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            ProxyContext context = new ProxyContext()
            {
                private final MethodInfo info = MethodInfo.of(method);

                @Override
                public MethodInfo getInfo()
                {
                    return info;
                }

                @Override
                public Object proceed()
                        throws Exception
                {
                    return proceed(args);
                }

                @Override
                public Object proceed(Object[] args)
                        throws Exception
                {
                    return method.invoke(instance, args);
                }

                @Override
                public Object[] getArgs()
                {
                    return args;
                }
            };
            Object returnValue = aroundHandler.apply(context);
            Class<?> returnType = method.getReturnType();

            if (returnValue == null && returnType != Void.TYPE && returnType.isPrimitive()) {
                throw new IllegalStateException("Null return value from advice does not match primitive return type for: " + method);
            }
            else {
                return returnValue;
            }
        };
        return handler;
    }

    @Override
    public T before(Consumer<MethodInfo> runnable)
    {
        InvocationHandler handler = beforeStatic(runnable, instance);
        return getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler beforeStatic(Consumer<MethodInfo> runnable, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            runnable.apply(MethodInfo.of(method));
            return method.invoke(instance, args);
        };
        return handler;
    }

    @Override
    public T afterReturning(Consumer<MethodInfo> runnable)
    {
        InvocationHandler handler = afterReturningStatic(runnable, instance);
        return getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler afterReturningStatic(Consumer<MethodInfo> runnable, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            Object value = method.invoke(instance, args);
            runnable.apply(MethodInfo.of(method));
            return value;
        };
        return handler;
    }

    @Override
    public T after(Consumer<MethodInfo> runnable)
    {
        InvocationHandler handler = afterStatic(runnable, instance);
        return getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler afterStatic(Consumer<MethodInfo> runnable, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            try {
                return method.invoke(instance, args);
            }
            finally {
                runnable.apply(MethodInfo.of(method));
            }
        };
        return handler;
    }

    @Override
    public T afterThrowing(Consumer<MethodInfo> runnable)
    {
        InvocationHandler handler = afterThrowingStatic(runnable, instance);
        return getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler afterThrowingStatic(Consumer<MethodInfo> runnable, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            try {
                return method.invoke(instance, args);
            }
            catch (Exception e) {
                runnable.apply(MethodInfo.of(method));
                throw e;
            }
        };
        return handler;
    }
}
