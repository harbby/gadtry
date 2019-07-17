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
import com.github.harbby.gadtry.function.Function1;
import com.github.harbby.gadtry.function.exception.Function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.aop.AopGo.doAround;
import static com.github.harbby.gadtry.base.Throwables.throwsException;

public class MethodSelect<T>
{
    private final MockInvocationHandler mockInvocationHandler;
    private final T proxy;
    private final Function<ProxyContext, Object, Throwable> function;
    protected final List<Function1<Method, Boolean>> filters = new ArrayList<>();

    public MethodSelect(MockInvocationHandler mockInvocationHandler, T proxy, Function<ProxyContext, Object, Throwable> function)
    {
        this.mockInvocationHandler = mockInvocationHandler;
        this.proxy = proxy;
        this.function = function;
    }

    public T when()
    {
        filters.add(method -> false);
        return doAround(function).when(proxy);
    }

    public MethodSelect<T> returnType(Class<?>... returnTypes)
    {
        filters.add(method -> Stream.of(returnTypes)
                .anyMatch(returnType -> returnType.isAssignableFrom(method.getReturnType())));
        return this;
    }

    public MethodSelect<T> annotated(Class<? extends Annotation>... methodAnnotations)
    {
        filters.add(method -> Stream.of(methodAnnotations)
                .anyMatch(ann -> method.getAnnotation(ann) != null));
        return this;
    }

    public MethodSelect<T> whereMethod(Function1<Method, Boolean> whereMethod)
    {
        filters.add(whereMethod);
        return this;
    }

    void build()
    {
        Arrays.stream(proxy.getClass().getDeclaredFields())
                .filter(field -> field.getType() == Method.class && Modifier.isStatic(field.getModifiers()))
                .map(field -> {
                    field.setAccessible(true);
                    try {
                        return (Method) field.get(null);
                    }
                    catch (IllegalAccessException e) {
                        throw throwsException(e);
                    }
                })
                .filter(method -> {
                    for (Function1<Method, Boolean> filter : filters) {
                        if (!filter.apply(method)) {
                            return false;
                        }
                    }
                    return true;
                })
                .forEach(method -> mockInvocationHandler.register(method, function));
    }
}
