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
package com.github.harbby.gadtry.aop.event;

import com.github.harbby.gadtry.aop.proxy.ProxyAccess;

import java.lang.reflect.Method;

public class ProxyAccessJoinPoint
        implements JoinPoint
{
    private final ProxyAccess mock;
    private final Method method;
    private final Object[] args;
    private final Object instance;

    public ProxyAccessJoinPoint(ProxyAccess mock, Method method, Object[] args, Object instance)
    {
        this.mock = mock;
        this.method = method;
        this.args = args;
        this.instance = instance;
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    @Override
    public Object mock()
    {
        return mock;
    }

    @Override
    public Object proceed(Object[] args)
            throws Exception
    {
        return mock.callRealMethod(method, instance, args);
    }

    @Override
    public Object[] getArgs()
    {
        return args;
    }
}
