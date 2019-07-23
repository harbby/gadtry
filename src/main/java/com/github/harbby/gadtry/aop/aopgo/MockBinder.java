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
import com.github.harbby.gadtry.aop.mock.AopInvocationHandler;
import com.github.harbby.gadtry.aop.model.After;
import com.github.harbby.gadtry.aop.model.AfterReturning;
import com.github.harbby.gadtry.aop.model.AfterThrowing;
import com.github.harbby.gadtry.aop.model.Before;
import com.github.harbby.gadtry.function.exception.Consumer;
import com.github.harbby.gadtry.function.exception.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MockBinder<T>
{
    private final AopInvocationHandler aopInvocationHandler;
    private final T proxy;
    private final AtomicReference<PointcutBuilder<T>> last = new AtomicReference<>();
    private final List<Aspect> aspects = new ArrayList<>();

    public MockBinder(T proxy, AopInvocationHandler aopInvocationHandler)
    {
        this.aopInvocationHandler = aopInvocationHandler;
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

    public PointcutBuilder<T> doAround(Function<JoinPoint, Object, Throwable> aroundContext)
    {
        return createMethodSelect(aroundContext);
    }

    private void flush()
    {
        if (last.get() != null) {
            Aspect aspect = last.get().build();
            aspects.add(aspect);
        }
    }

    List<Aspect> build()
    {
        this.flush();
        return aspects;
    }

    private PointcutBuilder<T> createMethodSelect(Function<JoinPoint, Object, Throwable> function)
    {
        this.flush();
        PointcutBuilder<T> pointcutBuilder = new PointcutBuilder<T>(aopInvocationHandler, proxy, function);
        last.set(pointcutBuilder);
        return pointcutBuilder;
    }
}
