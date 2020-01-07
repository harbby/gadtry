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

import com.github.harbby.gadtry.aop.aopgo.AopBuilder;
import com.github.harbby.gadtry.aop.aopgo.AroundHandler;
import com.github.harbby.gadtry.aop.mock.MockGo;
import com.github.harbby.gadtry.aop.model.After;
import com.github.harbby.gadtry.aop.model.AfterReturning;
import com.github.harbby.gadtry.aop.model.AfterThrowing;
import com.github.harbby.gadtry.aop.model.Before;
import com.github.harbby.gadtry.function.exception.Consumer;
import com.github.harbby.gadtry.function.exception.Function;

public class AopGo
{
    private AopGo() {}

    public interface ByInstance<T>
    {
        public AopBuilder<T> byInstance(T target);
    }

    public static <T> ByInstance<T> proxy(Class<T> superclass)
    {
        return target -> new AopBuilder<>(superclass, target);
    }

    public static <T> AopBuilder<T> proxy(T instance)
    {
        return new AopBuilder<T>((Class<T>) instance.getClass(), instance);
    }

    public static MockGo.DoBuilder doBefore(Consumer<Before, Exception> before)
    {
        return doAround(AroundHandler.doBefore(before));
    }

    public static MockGo.DoBuilder doAround(Function<JoinPoint, Object, Throwable> function)
    {
        return MockGo.doAround(function);
    }

    public static MockGo.DoBuilder doAfter(Consumer<After, Exception> after)
    {
        return doAround(AroundHandler.doAfter(after));
    }

    public static MockGo.DoBuilder doAfterReturning(Consumer<AfterReturning, Exception> afterReturning)
    {
        return doAround(AroundHandler.doAfterReturning(afterReturning));
    }

    public static MockGo.DoBuilder doAfterThrowing(Consumer<AfterThrowing, Exception> afterThrowing)
    {
        return doAround(AroundHandler.doAfterThrowing(afterThrowing));
    }
}
