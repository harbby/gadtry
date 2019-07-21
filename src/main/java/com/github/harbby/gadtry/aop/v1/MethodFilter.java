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
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.function.Function1;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public interface MethodFilter<T>
{
    public T methodAnnotated(Class<? extends Annotation>[] methodAnnotations);

    public T returnType(Class<?>... returnTypes);

    public T whereMethod(Function1<MethodInfo, Boolean> whereMethod);

    public static Function1<MethodInfo, Boolean> buildMethodFilter(
            Class<? extends Annotation>[] methodAnnotations,
            Class<?>[] inReturnTypes,
            Function1<MethodInfo, Boolean> whereMethod)
    {
        checkState(inReturnTypes != null, "inReturnTypes is null");
        final Class<?>[] returnTypes = Arrays.stream(inReturnTypes)
                .map(aClass -> {
                    if (aClass.isPrimitive()) {
                        return JavaTypes.getWrapperClass(aClass);
                    }
                    else {
                        return aClass;
                    }
                }).toArray(Class<?>[]::new);

        final List<Function1<MethodInfo, Boolean>> filters = new ArrayList<>();
        if (whereMethod != null) {
            filters.add(whereMethod);
        }
        if (returnTypes.length > 0) {
            filters.add(methodInfo -> Stream.of(returnTypes).anyMatch(returnType ->
            {
                if (methodInfo.getReturnType().isPrimitive()) {
                    return returnType.isAssignableFrom(JavaTypes.getWrapperClass(methodInfo.getReturnType()));
                }
                return returnType.isAssignableFrom(methodInfo.getReturnType());
            }));
        }
        if (methodAnnotations != null && methodAnnotations.length > 0) {
            filters.add(methodInfo -> Stream.of(methodAnnotations).anyMatch(ann -> methodInfo.getAnnotation(ann) != null));
        }

        return methodInfo -> {
            for (Function1<MethodInfo, Boolean> filter : filters) {
                if (!filter.apply(methodInfo)) {
                    return false;
                }
            }
            return true;
        };
    }
}
