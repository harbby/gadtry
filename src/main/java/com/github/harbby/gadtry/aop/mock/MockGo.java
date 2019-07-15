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
package com.github.harbby.gadtry.aop.mock;

import com.github.harbby.gadtry.aop.ProxyContext;
import com.github.harbby.gadtry.aop.impl.JavassistProxy;
import com.github.harbby.gadtry.aop.impl.Proxy;
import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.function.exception.Function;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static com.github.harbby.gadtry.base.JavaTypes.getClassInitValue;
import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static com.github.harbby.gadtry.base.Throwables.throwsException;

/**
 * MockGo
 */
public class MockGo
{
    private static final ThreadLocal<Tuple2<Proxy.ProxyHandler, Method>> LAST_MOCK_BY_WHEN_METHOD = new ThreadLocal<>();

    private MockGo() {}

    public static <T> T spy(T instance)
    {
        Class<T> tClass = (Class<T>) instance.getClass();
        try {
            Class<?> proxyClass = JavassistProxy.getProxyClass(tClass.getClassLoader(), tClass);
            Proxy.ProxyHandler proxyHandler = (Proxy.ProxyHandler) proxyClass.newInstance();
            InvocationHandler handler = (proxy, method, args) ->
            {
                LAST_MOCK_BY_WHEN_METHOD.set(Tuple2.of(proxyHandler, method));
                return method.invoke(instance, args);
            };
            proxyHandler.setHandler(new MockInvocationHandler(instance, handler));
            return (T) proxyHandler;
        }
        catch (Exception e) {
            throw throwsException(e);
        }
    }

    public static <T> T mock(Class<T> tClass)
    {
        try {
            Class<?> proxyClass = JavassistProxy.getProxyClass(tClass.getClassLoader(), tClass);
            Proxy.ProxyHandler instance = (Proxy.ProxyHandler) proxyClass.newInstance();

            InvocationHandler handler = (proxy, method, args1) ->
            {
                LAST_MOCK_BY_WHEN_METHOD.set(Tuple2.of(instance, method));
                return getClassInitValue(method.getReturnType());
            };
            instance.setHandler(new MockInvocationHandler(instance, handler));
            return (T) instance;
        }
        catch (Exception e) {
            throw throwsException(e);
        }
    }

    public static void initMocks(Object testObject)
    {
        MockAnnotations.initMocks(testObject);
    }

    public static DoReturnBuilder doReturn(Object value)
    {
        return new DoReturnBuilder(f -> value);
    }

    public static DoReturnBuilder doAround(Function<ProxyContext, Object> function)
    {
        return new DoReturnBuilder(function);
    }

    public static class DoReturnBuilder
    {
        private final Function<ProxyContext, Object> function;

        public DoReturnBuilder(Function<ProxyContext, Object> function)
        {
            this.function = function;
        }

        public <T> T when(T instance)
        {
            Proxy.ProxyHandler proxyHandler = (Proxy.ProxyHandler) instance;
            InvocationHandler old = proxyHandler.getHandler();
            if (old instanceof MockInvocationHandler) {
                ((MockInvocationHandler) old).setDoNext(function);
            }
            else {
                MockInvocationHandler handler = new MockInvocationHandler(proxyHandler, old);
                handler.setDoNext(function);
                proxyHandler.setHandler(handler);
            }
            return instance;
        }
    }

    public static <T> WhenThenBuilder<T> when(T methodCallValue)
    {
        return new WhenThenBuilder<>();
    }

    public static DoReturnBuilder doThrow(Exception e)
    {
        return new DoReturnBuilder(f -> { throw e; });
    }

    public static class WhenThenBuilder<T>
    {
        private final Tuple2<Proxy.ProxyHandler, Method> lastWhenMethod;

        public WhenThenBuilder()
        {
            this.lastWhenMethod = LAST_MOCK_BY_WHEN_METHOD.get();
            LAST_MOCK_BY_WHEN_METHOD.remove();
            checkState(lastWhenMethod != null, "whenMethod is null");
        }

        public void thenReturn(T value)
        {
            bind(p -> value);
        }

        public void thenAround(Function<ProxyContext, Object> function)
        {
            bind(function);
        }

        public void thenThrow(Exception e)
        {
            bind(f -> { throw e; });
        }

        private void bind(Function<ProxyContext, Object> function)
        {
            Proxy.ProxyHandler proxyHandler = lastWhenMethod.f1();

            InvocationHandler old = proxyHandler.getHandler();
            if (old instanceof MockInvocationHandler) {
                ((MockInvocationHandler) old).register(lastWhenMethod.f2(), function);
            }
            else {
                MockInvocationHandler handler = new MockInvocationHandler(proxyHandler, old);
                handler.register(lastWhenMethod.f2(), function);
                proxyHandler.setHandler(handler);
            }
        }
    }
}
