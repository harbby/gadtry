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

import com.github.harbby.gadtry.aop.MethodSignature;

import java.lang.reflect.Method;

public interface After
        extends Before, AfterReturning, AfterThrowing
{
    public boolean isSuccess();

    public static After of(MethodSignature method, Object[] args, Object value, Throwable e)
    {
        return new After()
        {
            @Override
            public MethodSignature getMethod()
            {
                return method;
            }

            @Override
            public Object[] getArgs()
            {
                return args;
            }

            @Override
            public Throwable getThrowable()
            {
                return e;
            }

            @Override
            public Object getValue()
            {
                return value;
            }

            @Override
            public boolean isSuccess()
            {
                return e == null;
            }
        };
    }

    public static After of(Method method, Object[] args, Object value, Throwable e)
    {
        return of(MethodSignature.ofByGadtryAop(method), args, value, e);
    }
}
