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

public interface Before
{
    /**
     * @return getMethodName
     */
    public default String getName()
    {
        return getMethod().getName();
    }

    MethodSignature getMethod();

    @SuppressWarnings("unchecked")
    default <V> V getArgument(int index)
    {
        return (V) this.getArgs()[index];
    }

    Object[] getArgs();

    public static Before of(MethodSignature method, Object[] args)
    {
        return new Before()
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
        };
    }
}
