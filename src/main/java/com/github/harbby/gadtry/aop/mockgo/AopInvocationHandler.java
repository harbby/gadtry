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
package com.github.harbby.gadtry.aop.mockgo;

import com.github.harbby.gadtry.aop.aopgo.AroundHandler;
import com.github.harbby.gadtry.aop.event.JoinPoint;
import com.github.harbby.gadtry.aop.proxy.ProxyAccess;
import com.github.harbby.gadtry.function.Function;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

import static com.github.harbby.gadtry.base.JavaTypes.getClassInitValue;
import static java.util.Objects.requireNonNull;

public class AopInvocationHandler
        implements InvocationHandler
{
    private final InvocationHandler defaultHandler;
    private InvocationHandler handler;
    private final Map<Method, AroundHandler> mockMethods = new IdentityHashMap<>();

    public AopInvocationHandler(Object target)
    {
        requireNonNull(target, "instance is null");
        this.defaultHandler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            Function<JoinPoint, Object, Throwable> userCode = mockMethods.get(method);
            if (userCode != null) {
                JoinPoint joinPoint = JoinPoint.of(proxy, method, args, target);
                return userCode.apply(joinPoint);
            }
            else {
                if (proxy instanceof ProxyAccess) {
                    return ((ProxyAccess) proxy).callRealMethod(method, target, args);
                }
                return method.invoke(target, args);
            }
        };
        this.initHandler();
    }

    public AopInvocationHandler()
    {
        this.defaultHandler = (InvocationHandler & Serializable) (proxy, method, args) -> {
            Function<JoinPoint, Object, Throwable> userCode = mockMethods.get(method);
            if (userCode != null) {
                return userCode.apply(JoinPoint.of(proxy, method, args));
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
        this.handler = requireNonNull(defaultHandler, "defaultHandler is null");
    }

    /*
     * WhenThen register
     */
    public void register(Method method, AroundHandler advice)
    {
        mockMethods.put(method, advice);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
    {
        try {
            return this.handler.invoke(proxy, method, args);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
