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
package com.github.harbby.gadtry.aop.v1;

import com.github.harbby.gadtry.aop.model.MethodInfo;
import com.github.harbby.gadtry.function.exception.Function;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.base.MoreObjects.checkContainsTrue;

public interface MethodFilter<T>
{
    public T methodAnnotated(Class<? extends Annotation>[] methodAnnotations);

    public T returnType(Class<?>... returnTypes);

    public T whereMethod(Function<MethodInfo, Boolean> whereMethod);

    public static Function<MethodInfo, Boolean> buildMethodFilter(
            Class<? extends Annotation>[] methodAnnotations,
            Class<?>[] inReturnTypes,
            Function<MethodInfo, Boolean> whereMethod)
    {
        final Class<?>[] returnTypes = inReturnTypes == null ?
                null :
                Arrays.stream(inReturnTypes).flatMap(aClass -> {
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

        final List<Function<MethodInfo, Boolean>> filters = new ArrayList<>();
        if (whereMethod != null) {
            filters.add(whereMethod);
        }
        if (returnTypes != null && returnTypes.length > 0) {
            filters.add(methodInfo -> checkContainsTrue(returnTypes, returnType -> returnType.isAssignableFrom(methodInfo.getReturnType())));
        }
        if (methodAnnotations != null && methodAnnotations.length > 0) {
            filters.add(methodInfo -> checkContainsTrue(methodAnnotations, ann -> methodInfo.getAnnotation(ann) != null));
        }

        return methodInfo -> {
            for (Function<MethodInfo, Boolean> filter : filters) {
                if (!filter.apply(methodInfo)) {
                    return false;
                }
            }
            return true;
        };
    }
}
