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
import com.github.harbby.gadtry.aop.model.After;
import com.github.harbby.gadtry.aop.model.AfterReturning;
import com.github.harbby.gadtry.aop.model.AfterThrowing;
import com.github.harbby.gadtry.aop.model.Before;
import com.github.harbby.gadtry.aop.model.MethodInfo;
import com.github.harbby.gadtry.base.Lazys;
import com.github.harbby.gadtry.function.Function1;
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
    private final Supplier<Function1<MethodInfo, Boolean>> methodFilter =
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

    protected Function1<MethodInfo, Boolean> getMethodFilter()
    {
        return (method) -> true;
    }

    private T getProxy(InvocationHandler handler, Class<?> interfaces)
    {
        Function1<MethodInfo, Boolean> filter = this.methodFilter.get();
        return getProxyStatic(proxy, loader, interfaces, handler, instance, filter);
    }

    private static <T> T getProxyStatic(
            Proxy proxyFactory,
            ClassLoader loader,
            Class<?> interfaces,
            InvocationHandler handler,
            T instance,
            Function1<MethodInfo, Boolean> filter)
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

        return proxyFactory.getProxy(loader, proxyHandler, interfaces);
    }

    @Override
    public T around(Function<ProxyContext, Object, Throwable> aroundHandler)
    {
        InvocationHandler handler = aroundStatic(aroundHandler, instance);
        return this.getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler aroundStatic(Function<ProxyContext, Object, Throwable> aroundHandler, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            ProxyContext context = ProxyContext.of(instance, method, args);
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
    public T before(Consumer<Before, Exception> runnable)
    {
        InvocationHandler handler = beforeStatic(runnable, instance);
        return getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler beforeStatic(Consumer<Before, Exception> runnable, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            runnable.apply(Before.of(method, args));
            return method.invoke(instance, args);
        };
        return handler;
    }

    @Override
    public T afterReturning(Consumer<AfterReturning, Exception> runnable)
    {
        InvocationHandler handler = afterReturningStatic(runnable, instance);
        return getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler afterReturningStatic(Consumer<AfterReturning, Exception> runnable, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            Object value = method.invoke(instance, args);
            runnable.apply(AfterReturning.of(method, args, value));
            return value;
        };
        return handler;
    }

    @Override
    public T after(Consumer<After, Exception> runnable)
    {
        InvocationHandler handler = afterStatic(runnable, instance);
        return getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler afterStatic(Consumer<After, Exception> runnable, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            Object value = null;
            Throwable throwable = null;
            try {
                value = method.invoke(instance, args);
                return value;
            }
            catch (Throwable e) {
                throwable = e;
                throw e;
            }
            finally {
                runnable.apply(After.of(method, args, value, throwable));
            }
        };
        return handler;
    }

    @Override
    public T afterThrowing(Consumer<AfterThrowing, Exception> runnable)
    {
        InvocationHandler handler = afterThrowingStatic(runnable, instance);
        return getProxy(handler, interfaces);
    }

    private static <T> InvocationHandler afterThrowingStatic(Consumer<AfterThrowing, Exception> runnable, T instance)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            try {
                return method.invoke(instance, args);
            }
            catch (Exception e) {
                runnable.apply(AfterThrowing.of(method, args, e));
                throw e;
            }
        };
        return handler;
    }
}
