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

import com.github.harbby.gadtry.aop.mockgo.AopInvocationHandler;
import com.github.harbby.gadtry.base.JavaTypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.aop.proxy.Proxy.getInvocationHandler;

/**
 * Method Selector
 */
public class PointcutBuilder<T>
{
    private final T proxy;
    protected final List<Function<Method, Boolean>> filters = new ArrayList<>();

    public PointcutBuilder(T proxy)
    {
        this.proxy = proxy;
    }

    public T when()
    {
        AopInvocationHandler aopInvocationHandler = getInvocationHandler(proxy);
        aopInvocationHandler.setHandler((proxy, method, args) -> {
            aopInvocationHandler.initHandler();
            filters.add(method1 -> method1 == method);
            return JavaTypes.getClassInitValue(method.getReturnType());
        });
        return proxy;
    }

    public T method()
    {
        return when();
    }

    public PointcutBuilder<T> returnType(Class<?>... returnTypes)
    {
        filters.add(method -> Stream.of(returnTypes)
                .flatMap(aClass -> {
                    if (aClass.isPrimitive()) {
                        return Stream.of(aClass, JavaTypes.getWrapperClass(aClass));
                    }
                    else {
                        return Stream.of(aClass);
                    }
                })
                .anyMatch(returnType -> {
                    if (method.getReturnType().isPrimitive()) {
                        return returnType.isAssignableFrom(JavaTypes.getWrapperClass(method.getReturnType()));
                    }
                    return returnType.isAssignableFrom(method.getReturnType());
                }));
        return this;
    }

    @SafeVarargs
    public final PointcutBuilder<T> annotated(Class<? extends Annotation>... methodAnnotations)
    {
        filters.add(method -> Stream.of(methodAnnotations)
                .anyMatch(ann -> method.getAnnotation(ann) != null));
        return this;
    }

    public PointcutBuilder<T> whereMethod(Function<Method, Boolean> whereMethod)
    {
        filters.add(whereMethod);
        return this;
    }

    public PointcutBuilder<T> methodName(String... names)
    {
        filters.add(method -> Stream.of(names).anyMatch(x -> method.getName().equals(x)));
        return this;
    }

    public void allMethod()
    {
    }

    List<Function<Method, Boolean>> build()
    {
        return filters;
    }
}
