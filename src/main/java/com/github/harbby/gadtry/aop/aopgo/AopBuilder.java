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

import com.github.harbby.gadtry.aop.ProxyRequest;
import com.github.harbby.gadtry.aop.impl.Proxy;
import com.github.harbby.gadtry.aop.impl.ProxyHandler;
import com.github.harbby.gadtry.aop.mock.AopInvocationHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class AopBuilder<T>
{
    private final Class<T> superclass;
    private final T target;

    private Consumer<MockBinder<T>>[] binders = new Consumer[0];
    private String basePackage;

    public AopBuilder(Class<T> superclass, T target)
    {
        this.superclass = superclass;
        this.target = target;
    }

    public final AopBuilder<T> basePackage(String basePackage)
    {
        this.basePackage = requireNonNull(basePackage, "basePackage is null");
        return this;
    }

    @SafeVarargs
    public final AopBuilder<T> aop(Consumer<MockBinder<T>>... binders)
    {
        this.binders = binders;
        return this;
    }

    public T build()
    {
        ClassLoader loader = superclass.getClassLoader() == null ? ProxyHandler.class.getClassLoader() :
                superclass.getClassLoader();
        final AopInvocationHandler aopInvocationHandler = new AopInvocationHandler(target);
        ProxyRequest<T> request = ProxyRequest.builder(superclass)
                .setInvocationHandler(aopInvocationHandler)
                .setClassLoader(loader)
                .setTarget(target)
                .basePackage(basePackage)
                .build();
        T proxy = Proxy.proxy(request);
        aopInvocationHandler.setProxyClass(proxy.getClass());
        //---------------------------
        final MockBinder<T> mockBinder = new MockBinder<>(proxy);
        for (Consumer<MockBinder<T>> it : binders) {
            it.accept(mockBinder);
        }
        Map<AroundHandler, PointcutBuilder<T>> aspects = mockBinder.build();
        Map<Method, AroundHandler> methodAdviceMap = new HashMap<>();
        aspects.forEach((k, v) -> {
            List<Method> methods = Proxy.filter(proxy.getClass(), v.build());
            //merge aspect
            methods.forEach(method -> methodAdviceMap.merge(method, k, AroundHandler::merge));
        });

        methodAdviceMap.forEach(aopInvocationHandler::register);
        return proxy;
    }
}
