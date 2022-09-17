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
package com.github.harbby.gadtry.aop;

import com.github.harbby.gadtry.aop.aopgo.AroundHandler;
import com.github.harbby.gadtry.aop.proxy2.Interceptor;
import com.github.harbby.gadtry.aop.proxy2.MethodCallerImpl;
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.collection.tuple.Tuple2;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;

public class MockInterceptor
        implements Interceptor
{
    static final ThreadLocal<Tuple2<Object, Method>> LAST_MOCK_BY_WHEN_METHOD = new ThreadLocal<>();

    private final Interceptor defaultHandler;
    private Interceptor handler;
    private final Map<Method, AroundHandler> mockMethods = new IdentityHashMap<>();

    public MockInterceptor(boolean isSpy)
    {
        this.defaultHandler = (Interceptor & Serializable) (proxy, method, caller) -> {
            AroundHandler answer = mockMethods.get(method);
            if (answer != null) {
                return answer.apply(new MethodCallerImpl(proxy, method, caller));
            }
            else {
                if (isSpy) {
                    return caller.call();
                }
                else {
                    return JavaTypes.getClassInitValue(method.getReturnType());
                }
            }
        };
        this.initHandler();
    }

    public void setHandler(Interceptor handler)
    {
        this.handler = handler;
    }

    public void initHandler()
    {
        this.handler = requireNonNull(defaultHandler, "defaultHandler is null");
    }

    /*
     * WhenThen register
     */
    public void register(Method method, AroundHandler answer)
    {
        mockMethods.put(method, answer);
    }

    @Override
    public Object invoke(Object proxy, Method method, Callable<Object> caller)
            throws Throwable
    {
        Object value = this.handler.invoke(proxy, method, caller);
        LAST_MOCK_BY_WHEN_METHOD.set(Tuple2.of(proxy, method));
        return value;
    }
}
