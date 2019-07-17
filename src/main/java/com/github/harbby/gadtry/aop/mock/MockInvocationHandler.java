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
import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.function.exception.Function;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.harbby.gadtry.aop.mock.MockGo.LAST_MOCK_BY_WHEN_METHOD;
import static com.github.harbby.gadtry.base.JavaTypes.getClassInitValue;
import static java.util.Objects.requireNonNull;

public class MockInvocationHandler
        implements InvocationHandler, Serializable
{
    private final InvocationHandler handler;
    private final AtomicReference<Function<ProxyContext, Object, Throwable>> next = new AtomicReference<>();
    private final Map<Method, Function<ProxyContext, Object, Throwable>> mockMethods = new IdentityHashMap<>();
    private final Object instance;

    public MockInvocationHandler()
    {
        this.instance = null;
        this.handler = (proxy, method, args) ->
        {
            LAST_MOCK_BY_WHEN_METHOD.set(Tuple2.of(proxy, method));
            return getClassInitValue(method.getReturnType());
        };
    }

    public MockInvocationHandler(Object instance)
    {
        this.instance = requireNonNull(instance, "instance is null");
        this.handler = (proxy, method, args) ->
        {
            LAST_MOCK_BY_WHEN_METHOD.set(Tuple2.of(proxy, method));
            try {
                return method.invoke(instance, args);
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        };
    }

    /**
     * doReturn register
     */
    public void setDoNext(Function<ProxyContext, Object, Throwable> function)
    {
        next.set(function);
    }

    /**
     * WhenThen register
     */
    public void register(Method method, Function<ProxyContext, Object, Throwable> nextValue)
    {
        mockMethods.put(method, nextValue);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
    {
        Function<ProxyContext, Object, Throwable> nextValue = next.getAndSet(null);
        if (nextValue != null) {
            register(method, nextValue);
            return getClassInitValue(method.getReturnType());
        }
        else if (mockMethods.containsKey(method)) {
            ProxyContext context = new ProxyContext()
            {
                @Override
                public Method getMethod()
                {
                    return method;
                }

                @Override
                public Object[] getArgs()
                {
                    return args;
                }

                @Override
                public Object proceed(Object[] args)
                        throws Throwable
                {
                    if (instance == null) {  //@Mock
                        return getClassInitValue(method.getReturnType());
                    }
                    else {  //MockSpy
                        return method.invoke(instance, args);
                    }
                }
            };
            return mockMethods.get(method).apply(context);
        }
        else {
            return handler.invoke(proxy, method, args);
        }
    }
}
