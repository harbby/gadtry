/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.aop.v1;

import com.github.harbby.gadtry.aop.model.MethodInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.base.Checks.checkContainsTrue;

public class MethodFilter
{
    private final Class<? extends Annotation>[] methodAnnotations;
    private final Class<?>[] returnTypes;
    private final Function<MethodInfo, Boolean> whereMethod;

    public MethodFilter(
            Class<? extends Annotation>[] methodAnnotations,
            Class<?>[] returnTypes,
            Function<MethodInfo, Boolean> whereMethod)
    {
        this.methodAnnotations = methodAnnotations;
        this.whereMethod = whereMethod;
        this.returnTypes = returnTypes == null ?
                null :
                Arrays.stream(returnTypes).flatMap(aClass -> {
                    if (aClass == Boolean.class || aClass == boolean.class) {
                        return Stream.of(Boolean.class, boolean.class);
                    }
                    else if (aClass == Integer.class || aClass == int.class) {
                        return Stream.of(Integer.class, int.class);
                    }
                    else if (aClass == Byte.class || aClass == byte.class) {
                        return Stream.of(Byte.class, byte.class);
                    }
                    else if (aClass == Short.class || aClass == short.class) {
                        return Stream.of(Short.class, short.class);
                    }
                    else if (aClass == Long.class || aClass == long.class) {
                        return Stream.of(Long.class, long.class);
                    }
                    else if (aClass == Double.class || aClass == double.class) {
                        return Stream.of(Double.class, double.class);
                    }
                    else if (aClass == Character.class || aClass == char.class) {
                        return Stream.of(Character.class, char.class);
                    }
                    else if (aClass == Void.class || aClass == void.class) {
                        return Stream.of(Void.class, void.class);
                    }
                    else {
                        return Stream.of(aClass);
                    }
                }).toArray(Class<?>[]::new);
    }

    public Class<? extends Annotation>[] getMethodAnnotations()
    {
        return methodAnnotations;
    }

    public Class<?>[] getReturnTypes()
    {
        return returnTypes;
    }

    public Function<MethodInfo, Boolean> getWhereMethod()
    {
        return whereMethod;
    }

    public boolean checkMethod(MethodInfo method)
    {
        return (whereMethod == null || whereMethod.apply(method)) &&
                checkContainsTrue(returnTypes, (returnType -> returnType.isAssignableFrom(method.getReturnType()))) &&
                checkContainsTrue(methodAnnotations, (ann -> method.getAnnotation(ann) != null));
    }

    public boolean checkMethod(Method method)
    {
        return checkMethod(MethodInfo.of(method));
    }

    public static interface Filter<T>
    {
        public T methodAnnotated(Class<? extends Annotation>... methodAnnotations);

        public T returnType(Class<?>... returnTypes);

        public T whereMethod(Function<MethodInfo, Boolean> whereMethod);
    }
}
