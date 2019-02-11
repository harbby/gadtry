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

import com.github.harbby.gadtry.aop.Binder;
import com.github.harbby.gadtry.aop.model.ClassInfo;
import com.github.harbby.gadtry.aop.model.MethodInfo;
import com.github.harbby.gadtry.aop.model.Pointcut;
import com.github.harbby.gadtry.classloader.ClassScanner;
import com.github.harbby.gadtry.collection.ImmutableSet;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Checks.checkState;

public class FilterBuilder
        implements MethodFilter<FilterBuilder>
{
    private final Pointcut pointcut;
    private Set<Class<?>> inputClass = new HashSet<>();
    //class filter
    private String packageName;
    private Class<? extends Annotation>[] classAnnotations;
    private Class<?>[] subclasses;
    private Function<Class<?>, Boolean> whereClass = aClass -> true;
    //-- method filter
    private Class<? extends Annotation>[] methodAnnotations;
    private Class<?>[] returnTypes;
    private Function<MethodInfo, Boolean> whereMethod;

    public FilterBuilder(Pointcut pointcut)
    {
        this.pointcut = pointcut;
    }

    @Override
    @SafeVarargs
    public final FilterBuilder methodAnnotated(Class<? extends Annotation>... methodAnnotations)
    {
        this.methodAnnotations = methodAnnotations;
        return this;
    }

    @Override
    public FilterBuilder returnType(Class<?>... returnTypes)
    {
        this.returnTypes = returnTypes;
        return this;
    }

    @Override
    public FilterBuilder whereMethod(Function<MethodInfo, Boolean> whereMethod)
    {
        this.whereMethod = whereMethod;
        return this;
    }

    public FilterBuilder withPackage(String packageName)
    {
        this.packageName = packageName;
        return this;
    }

    @SafeVarargs
    public final FilterBuilder classAnnotated(Class<? extends Annotation>... classAnnotations)
    {
        this.classAnnotations = classAnnotations;
        return this;
    }

    public FilterBuilder classes(Class<?>... inputClass)
    {
        this.inputClass = ImmutableSet.of(inputClass);
        return this;
    }

    /**
     * or
     *
     * @param subclasses sub class
     * @return FilterBuilder
     */
    public FilterBuilder subclassOf(Class<?>... subclasses)
    {
        this.subclasses = subclasses;
        return this;
    }

    public FilterBuilder whereClass(Function<ClassInfo, Boolean> whereClass)
    {
        checkState(whereClass != null, "whereClass is null");
        this.whereClass = (aClass) -> whereClass.apply(ClassInfo.of(aClass));
        return this;
    }

    public Binder.PointBuilder build()
    {
        Set<Class<?>> scanClass = new HashSet<>();
        if (packageName != null) {
            ClassScanner scanner = ClassScanner.builder(packageName)
                    .annotated(classAnnotations)
                    .subclassOf(subclasses)
                    .filter(whereClass)
                    .scan();
            scanClass.addAll(scanner.getClasses());
        }
        scanClass.addAll(inputClass);

        Function<MethodInfo, Boolean> methodFilter = MethodFilter.buildFilter(methodAnnotations, returnTypes, whereMethod);

        //---class filter
        Set<Class<?>> searchClass = scanClass.stream().filter(
                aClass -> !Arrays.stream(aClass.getMethods())
                        .map(method -> !(methodFilter.apply(MethodInfo.of(method))))
                        .reduce((x, y) -> x && y).orElse(false)
        ).collect(Collectors.toSet());

        pointcut.setLocation(methodFilter);
        pointcut.setSearchClass(searchClass);
        return new Binder.PointBuilder(pointcut);
    }
}
