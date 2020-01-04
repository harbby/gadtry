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

import com.github.harbby.gadtry.aop.impl.Proxy;
import com.github.harbby.gadtry.aop.impl.ProxyHandler;
import com.github.harbby.gadtry.aop.mock.AopInvocationHandler;
import com.github.harbby.gadtry.function.exception.Consumer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.harbby.gadtry.base.Throwables.throwsThrowable;
import static java.util.Objects.requireNonNull;

public class WhenMethod<T>
{
    private final Class<T> superclass;
    private final T target;

    private Consumer<MockBinder<T>, Throwable>[] binders = new Consumer[0];
    private String basePackage;

    public WhenMethod(Class<T> superclass, T target)
    {
        this.superclass = superclass;
        this.target = target;
    }

    public final WhenMethod<T> basePackage(String basePackage)
    {
        this.basePackage = requireNonNull(basePackage, "basePackage is null");
        return this;
    }

    @SafeVarargs
    public final WhenMethod<T> aop(Consumer<MockBinder<T>, Throwable>... binders)
    {
        this.binders = binders;
        return this;
    }

    public T build()
    {
        ClassLoader loader = superclass.getClassLoader() == null ? ProxyHandler.class.getClassLoader() :
                superclass.getClassLoader();
        final AopInvocationHandler aopInvocationHandler = new AopInvocationHandler(target);
        T proxy = Proxy.builder(superclass)
                .setInvocationHandler(aopInvocationHandler)
                .setClassLoader(loader)
                .basePackage(basePackage)
                .build();
        aopInvocationHandler.setProxyClass(proxy.getClass());
        //---------------------------
        final MockBinder<T> mockBinder = new MockBinder<>(proxy, aopInvocationHandler);
        for (Consumer<MockBinder<T>, Throwable> it : binders) {
            try {
                it.apply(mockBinder);
            }
            catch (Throwable throwable) {
                throwsThrowable(throwable);
            }
        }
        List<Aspect> aspects = mockBinder.build();
        Map<Method, Advice> methodAdviceMap = new HashMap<>();
        for (Aspect aspect : aspects) {
            List<Method> methods = aspect.getPointcut().filter(proxy.getClass());
            //merge aspect
            Arrays.stream(aspect.getAdvices()).reduce(Advice::merge).ifPresent(advice ->
                    methods.forEach(method -> methodAdviceMap.merge(method, advice, Advice::merge)));
        }
        methodAdviceMap.forEach(aopInvocationHandler::register);
        return proxy;
    }
}
