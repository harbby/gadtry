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

import com.github.harbby.gadtry.aop.event.After;
import com.github.harbby.gadtry.aop.event.AfterReturning;
import com.github.harbby.gadtry.aop.event.AfterThrowing;
import com.github.harbby.gadtry.aop.event.Before;
import com.github.harbby.gadtry.function.Consumer;

import java.util.LinkedHashMap;
import java.util.Map;

public class MockBinder<T>
{
    private final T proxy;
    private final Map<AroundHandler, PointcutBuilder<T>> aspects = new LinkedHashMap<>();

    public MockBinder(T proxy)
    {
        this.proxy = proxy;
    }

    public PointcutBuilder<T> doBefore(Consumer<Before, Exception> before)
    {
        return createMethodSelect(AroundHandler.doBefore(before));
    }

    public PointcutBuilder<T> doAfterReturning(Consumer<AfterReturning, Exception> afterReturning)
    {
        return createMethodSelect(AroundHandler.doAfterReturning(afterReturning));
    }

    public PointcutBuilder<T> doAfterThrowing(Consumer<AfterThrowing, Exception> afterThrowing)
    {
        return createMethodSelect(AroundHandler.doAfterThrowing(afterThrowing));
    }

    public PointcutBuilder<T> doAfter(Consumer<After, Exception> after)
    {
        return createMethodSelect(AroundHandler.doAfter(after));
    }

    public PointcutBuilder<T> doAround(AroundHandler aroundContext)
    {
        return createMethodSelect(aroundContext);
    }

    Map<AroundHandler, PointcutBuilder<T>> build()
    {
        return aspects;
    }

    private PointcutBuilder<T> createMethodSelect(AroundHandler function)
    {
        PointcutBuilder<T> pointcutBuilder = new PointcutBuilder<T>(proxy);
        aspects.put(function, pointcutBuilder);
        return pointcutBuilder;
    }

    public static <T> void copyWrite(MockBinder<T> copyIn, MockBinder<T> copyOut)
    {
        copyOut.aspects.clear();
        copyOut.aspects.putAll(copyIn.aspects);
    }
}
