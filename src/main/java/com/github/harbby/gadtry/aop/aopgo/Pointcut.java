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

import com.github.harbby.gadtry.function.Function1;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.base.Throwables.throwsException;

public interface Pointcut
{
    public List<Function1<Method, Boolean>> getLocation();

    default List<Method> filter(Class<?> proxyClass)
    {
        return Stream.of(proxyClass.getDeclaredFields())
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
                .filter(this::methodFilter)
                .collect(Collectors.toList());
    }

    default boolean methodFilter(Method method)
    {
        for (Function1<Method, Boolean> filter : getLocation()) {
            if (!filter.apply(method)) {
                return false;
            }
        }
        return true;
    }
}
