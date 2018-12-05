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

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicReference;

public final class CutModeImpl<T>
        implements CutMode<T>
{
    private final Class<T> interfaces;
    private final T instance;
    private final ClassLoader loader;

    private final Proxy proxy;

    private CutModeImpl(Class<T> interfaces, T instance, Proxy proxy)
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

    private T getProxy(InvocationHandler handler, Class<T> interfaces)
    {
        return proxy.getProxy(loader, interfaces, handler);
    }

    @Override
    public T around(Handler1<ProxyContext> runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            AtomicReference<Object> result = new AtomicReference<>(null);
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
                    Object value = method.invoke(instance, args);
                    result.set(value);
                    return value;
                }

                @Override
                public Object[] getArgs()
                {
                    return args;
                }
            };
            runnable.apply(context);
            return result.get();
        };

        return getProxy(handler, interfaces);
    }

    @Override
    public T before(Handler0 runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            runnable.apply();
            return method.invoke(instance, args);
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T before(Handler1<MethodInfo> runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            runnable.apply(MethodInfo.of(method));
            return method.invoke(instance, args);
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T afterReturning(Handler0 runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            Object value = method.invoke(instance, args);
            runnable.apply();
            return value;
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T afterReturning(Handler1<MethodInfo> runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            Object value = method.invoke(instance, args);
            runnable.apply(MethodInfo.of(method));
            return value;
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T after(Handler0 runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            try {
                return method.invoke(instance, args);
            }
            finally {
                runnable.apply();
            }
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T after(Handler1<MethodInfo> runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            Object value = method.invoke(instance, args);
            runnable.apply(MethodInfo.of(method));
            return value;
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T afterThrowing(Handler0 runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
            try {
                return method.invoke(instance, args);
            }
            catch (Exception e) {
                runnable.apply();
                throw e;
            }
        };
        return getProxy(handler, interfaces);
    }

    @Override
    public T afterThrowing(Handler1<MethodInfo> runnable)
    {
        InvocationHandler handler = (proxy, method, args) -> {
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
