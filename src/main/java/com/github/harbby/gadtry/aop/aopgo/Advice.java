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
package com.github.harbby.gadtry.aop.aopgo;

import com.github.harbby.gadtry.aop.JoinPoint;
import com.github.harbby.gadtry.function.exception.Function;

import java.lang.reflect.Method;

public interface Advice
        extends Function<JoinPoint, Object, Throwable>
{
    default Advice merge(Advice newAdvice)
    {
        return joinPoint -> {
            final JoinPoint mergeJoinPoint = new JoinPoint()
            {
                @Override
                public Method getMethod()
                {
                    return joinPoint.getMethod();
                }

                @Override
                public Object[] getArgs()
                {
                    return joinPoint.getArgs();
                }

                @Override
                public Object proceed(Object[] args)
                        throws Throwable
                {
                    return newAdvice.apply(joinPoint);
                }
            };
            return this.apply(mergeJoinPoint);
        };
    }
}
