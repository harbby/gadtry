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

import com.github.harbby.gadtry.aop.model.Before;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

/**
 * around
 */
public interface JoinPoint
        extends Before
{
    default Object proceed()
            throws Throwable
    {
        return proceed(getArgs());
    }

    Object proceed(Object[] args)
            throws Throwable;

    public static JoinPoint of(Object instance, Method method, Object[] args)
    {
        requireNonNull(instance, "instance is null");
        return new JoinPoint()
        {
            @Override
            public Method getMethod()
            {
                return method;
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
    }
}
