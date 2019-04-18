/*
 * Copyright (C) 2018 The Harbby Authors
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
import com.github.harbby.gadtry.function.Consumer;
import com.github.harbby.gadtry.function.Function;
import com.github.harbby.gadtry.function.Runnable;

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

    private static <T> T getProxy(InvocationHandler handler, Class<?> interfaces)
    {
        Function<MethodInfo, Boolean> filter = this.methodFilter.get();
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

        return proxy.getProxy(loader, interfaces, proxyHandler);
    }

    @Override
    public T around(Consumer<ProxyContext> aroundHandler)
    {
        return this.around((proxyContext -> {
            aroundHandler.apply(proxyContext);
            return null;
        }));
    }

    @Override
    public T around(Function<ProxyContext, Object> aroundHandler)
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

        return getProxy(handler, interfaces);
    }

    @Override
    public T before(Runnable runnable)
    {
        return this.before((a) -> runnable.apply());
    }

    @Override
    public T before(Consumer<MethodInfo> runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            runnable.apply(MethodInfo.of(method));
            return method.invoke(instance, args);
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T afterReturning(Runnable runnable)
    {
        return this.afterReturning((a) -> runnable.apply());
    }

    @Override
    public T afterReturning(Consumer<MethodInfo> runnable)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            Object value = method.invoke(instance, args);
            runnable.apply(MethodInfo.of(method));
            return value;
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T after(Runnable runnable)
    {
        return this.after((a) -> runnable.apply());
    }

    @Override
    public T after(Consumer<MethodInfo> runnable)
    {
        return afterStatic(loader, runnable, instance, interfaces, methodFilter.get());
    }

    private static <T> T afterStatic(ClassLoader classLoader, Consumer<MethodInfo> runnable,
            T instance,
            Class<?> interfaces,
            Function<MethodInfo, Boolean> filter)
    {
        InvocationHandler handler = (InvocationHandler & Serializable) (proxy, method, args) -> {
                try {
                    return method.invoke(instance, args);
                }
                finally {
                    runnable.apply(MethodInfo.of(method));
                }
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T afterThrowing(Runnable runnable)
    {
        return this.afterThrowing((a) -> runnable.apply());
    }

    @Override
    public T afterThrowing(Consumer<MethodInfo> runnable)
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
        return getProxy(handler, interfaces);
    }
}
