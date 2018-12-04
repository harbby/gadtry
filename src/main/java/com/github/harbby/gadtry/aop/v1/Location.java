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
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Checks.checkContainsTrue;

public class Location
{
    private final Class<? extends Annotation>[] methodAnnotations;
    private final Class<?>[] returnTypes;
    private final Function<MethodInfo, Boolean> whereMethod;

    private final Set<Class<?>> searchClass;

    public Location(
            Class<? extends Annotation>[] methodAnnotations,
            Class<?>[] returnTypes,
            Function<MethodInfo, Boolean> whereMethod,
            Set<Class<?>> scanClass)
    {
        this.methodAnnotations = methodAnnotations;
        this.returnTypes = returnTypes;
        this.whereMethod = whereMethod;

        //---class filter
        this.searchClass = scanClass.stream().filter(aClass -> !Arrays.stream(aClass.getMethods())
                .map(method -> !(this.checkMethod(MethodInfo.of(method))))
                .reduce((x, y) -> x && y).orElse(false))
                .collect(Collectors.toSet());
    }

    public Set<Class<?>> getSearchClass()
    {
        return searchClass;
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
        return whereMethod == null || whereMethod.apply(method) &&
                checkContainsTrue(returnTypes, (returnType -> returnType.isAssignableFrom(method.getReturnType()))) &&
                checkContainsTrue(methodAnnotations, (ann -> method.getAnnotation(ann) != null));
    }
}
