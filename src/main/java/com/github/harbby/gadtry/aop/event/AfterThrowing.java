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

public interface AfterThrowing
        extends Before
{
    public Throwable getThrowable();

    public static AfterThrowing of(MethodSignature method, Object[] args, Throwable e)
    {
        return new AfterThrowing()
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
        };
    }
}
