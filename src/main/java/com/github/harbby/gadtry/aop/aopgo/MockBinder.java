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

import com.github.harbby.gadtry.aop.ProxyContext;
import com.github.harbby.gadtry.aop.mock.MockInvocationHandler;
import com.github.harbby.gadtry.aop.model.After;
import com.github.harbby.gadtry.aop.model.AfterReturning;
import com.github.harbby.gadtry.aop.model.AfterThrowing;
import com.github.harbby.gadtry.aop.model.Before;
import com.github.harbby.gadtry.function.exception.Consumer;
import com.github.harbby.gadtry.function.exception.Function;

import java.util.concurrent.atomic.AtomicReference;

public class MockBinder<T>
{
    private final MockInvocationHandler mockInvocationHandler;
    private final T proxy;

    public MockBinder(T proxy, MockInvocationHandler mockInvocationHandler)
    {
        this.mockInvocationHandler = mockInvocationHandler;
        this.proxy = proxy;
    }

    public MethodSelect<T> doBefore(Consumer<Before, Exception> before)
    {
        return createMethodSelect(AroundHandler.doBefore(before));
    }

    public MethodSelect<T> doAfterReturning(Consumer<AfterReturning, Exception> afterReturning)
    {
        return createMethodSelect(AroundHandler.doAfterReturning(afterReturning));
    }

    public MethodSelect<T> doAfterThrowing(Consumer<AfterThrowing, Exception> afterThrowing)
    {
        return createMethodSelect(AroundHandler.doAfterThrowing(afterThrowing));
    }

    public MethodSelect<T> doAfter(Consumer<After, Exception> after)
    {
        return createMethodSelect(AroundHandler.doAfter(after));
    }

    public MethodSelect<T> doAround(Function<ProxyContext, Object, Throwable> aroundContext)
    {
        return createMethodSelect(aroundContext);
    }

    void flush()
    {
        if (last.get() != null) {
            last.get().build();
        }
    }

    AtomicReference<MethodSelect<T>> last = new AtomicReference<>();

    private MethodSelect<T> createMethodSelect(Function<ProxyContext, Object, Throwable> function)
    {
        this.flush();
        MethodSelect<T> methodSelect = new MethodSelect<T>(mockInvocationHandler, proxy, function);
        last.set(methodSelect);
        return methodSelect;
    }
}
