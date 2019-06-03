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
package com.github.harbby.gadtry.aop;

import com.github.harbby.gadtry.aop.impl.AopFactoryImpl;
import com.github.harbby.gadtry.aop.impl.CutModeImpl;
import com.github.harbby.gadtry.aop.impl.JavassistProxy;
import com.github.harbby.gadtry.aop.impl.JdkProxy;
import com.github.harbby.gadtry.aop.impl.Proxy;
import com.github.harbby.gadtry.aop.model.MethodInfo;
import com.github.harbby.gadtry.aop.model.Pointcut;
import com.github.harbby.gadtry.aop.v1.FilterBuilder;
import com.github.harbby.gadtry.aop.v1.MethodFilter;
import com.github.harbby.gadtry.collection.mutable.MutableList;
import com.github.harbby.gadtry.function.exception.Function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public interface AopFactory
{
    List<Pointcut> getPointcuts();

    <T> T proxy(Class<T> interfaces, T instance);

    /**
     * Not implemented
     *
     * @param aspects aspects
     * @return AopFactory
     */
    public static AopFactory create(Aspect... aspects)
    {
        List<Pointcut> pointcuts = new ArrayList<>();
        Binder binder = new Binder()
        {
            @Deprecated
            @Override
            public PointBuilder bind(String pointName, String location)
            {
                throw new UnsupportedOperationException("this method have't support!");
            }

            @Override
            public FilterBuilder bind(String pointName)
            {
                Pointcut pointcut = new Pointcut(pointName);
                pointcuts.add(pointcut);
                return new FilterBuilder(pointcut);
            }
        };
        for (Aspect aspect : aspects) {
            aspect.register(binder);
        }

        return new AopFactoryImpl(MutableList.copy(pointcuts));
    }

    public static <T> ByInstance<T> proxy(Class<T> pClass)
    {
        if (pClass.isInterface()) {
            return instance -> new ProxyBuilder<>(pClass, instance, JdkProxy::newProxyInstance);
        }
        else {
            checkState(!Modifier.isFinal(pClass.getModifiers()), pClass + " is final");
            return instance -> new ProxyBuilder<>(pClass, instance, JavassistProxy::newProxyInstance);
        }
    }

    public interface ByInstance<T>
    {
        public ProxyBuilder<T> byInstance(T instance);
    }

    public static class ProxyBuilder<T>
            extends CutModeImpl<T>
            implements MethodFilter<ProxyBuilder>
    {
        //-- method filter
        private Class<? extends Annotation>[] methodAnnotations = new Class[0];
        private Class<?>[] returnTypes = new Class<?>[0];
        private Function<MethodInfo, Boolean> whereMethod;

        private ProxyBuilder(Class<?> interfaces, T instance, Proxy proxy)
        {
            super(interfaces, instance, proxy);
        }

        @Override
        @SafeVarargs
        public final ProxyBuilder<T> methodAnnotated(Class<? extends Annotation>... methodAnnotations)
        {
            this.methodAnnotations = requireNonNull(methodAnnotations, "methodAnnotations is null");
            return this;
        }

        @Override
        public ProxyBuilder<T> returnType(Class<?>... returnTypes)
        {
            this.returnTypes = requireNonNull(returnTypes, "returnTypes is null");
            return this;
        }

        @Override
        public ProxyBuilder<T> whereMethod(Function<MethodInfo, Boolean> whereMethod)
        {
            this.whereMethod = whereMethod;
            return this;
        }

        @Override
        protected Function<MethodInfo, Boolean> getMethodFilter()
        {
            return MethodFilter.buildMethodFilter(methodAnnotations, returnTypes, whereMethod);
        }
    }
}
