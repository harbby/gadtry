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

import com.github.harbby.gadtry.aop.JoinPoint;
import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.function.exception.Function;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

import static com.github.harbby.gadtry.aop.mock.MockGo.LAST_MOCK_BY_WHEN_METHOD;
import static com.github.harbby.gadtry.base.JavaTypes.getClassInitValue;
import static java.util.Objects.requireNonNull;

public class AopInvocationHandler
        implements InvocationHandler, Serializable
{
    private final InvocationHandler defaultHandler;
    private InvocationHandler handler;
    private final Map<Method, Function<JoinPoint, Object, Throwable>> mockMethods = new IdentityHashMap<>();

    public AopInvocationHandler(Object target)
    {
        requireNonNull(target, "instance is null");
        this.defaultHandler = (proxy, method, args) -> {
            if (mockMethods.containsKey(method)) {
                JoinPoint joinPoint = JoinPoint.of(target, method, args);
                return mockMethods.get(method).apply(joinPoint);
            }
            else {
                return method.invoke(target, args);
            }
        };
        this.initHandler();
    }

    public AopInvocationHandler()
    {
        this.defaultHandler = (proxy, method, args) -> {
            if (mockMethods.containsKey(method)) {
                return mockMethods.get(method).apply(JoinPoint.of(method, args));
            }
            else {
                return getClassInitValue(method.getReturnType());
            }
        };
        this.initHandler();
    }

    public void setHandler(InvocationHandler handler)
    {
        this.handler = handler;
    }

    public void initHandler()
    {
        this.handler = defaultHandler;
    }

    /**
     * WhenThen register
     */
    public void register(Method method, Function<JoinPoint, Object, Throwable> advice)
    {
        mockMethods.put(method, advice);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
    {
        LAST_MOCK_BY_WHEN_METHOD.set(Tuple2.of(proxy, method));
        try {
            //method.setAccessible(true);  //todo: 如果未来需要默认开启所有访问权限时。设置此处为 true 即可
            return this.handler.invoke(proxy, method, args);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
