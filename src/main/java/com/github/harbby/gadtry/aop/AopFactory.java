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
package com.github.harbby.gadtry.aop;

import com.github.harbby.gadtry.aop.impl.CutModeImpl;
import com.github.harbby.gadtry.aop.impl.JavassistProxy;
import com.github.harbby.gadtry.aop.impl.JdkProxy;
import com.github.harbby.gadtry.aop.model.Pointcut;
import com.github.harbby.gadtry.aop.v1.LocationBuilder;
import com.github.harbby.gadtry.collection.ImmutableList;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.github.harbby.gadtry.base.Checks.checkState;

public interface AopFactory
{
    List<Pointcut> getPointcuts();

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
            @Override
            public PointBuilder bind(String pointName, String location)
            {
                throw new UnsupportedOperationException("this method have't support!");
            }

            @Override
            public LocationBuilder bind(String pointName)
            {
                Pointcut pointcut = new Pointcut(pointName);
                pointcuts.add(pointcut);
                return new LocationBuilder(pointcut);
            }
        };
        for (Aspect aspect : aspects) {
            aspect.register(binder);
        }

        List<Pointcut> copy = ImmutableList.copy(pointcuts);
        return () -> copy;
    }

    public static <T> ByInstance<T> proxy(Class<T> interfaces)
    {
        if (interfaces.isInterface()) {
            return instance -> CutModeImpl.of(interfaces, instance, JdkProxy::newProxyInstance);
        }
        else {
            checkState(!Modifier.isFinal(interfaces.getModifiers()), interfaces + " is final");
            return instance -> CutModeImpl.of(interfaces, instance, JavassistProxy::newProxyInstance);
        }
    }

    public interface ByInstance<T>
    {
        public CutMode<T> byInstance(T instance);
    }
}
