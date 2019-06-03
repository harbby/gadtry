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
package com.github.harbby.gadtry.aop.impl;

import com.github.harbby.gadtry.aop.AopFactory;
import com.github.harbby.gadtry.aop.ProxyContext;
import com.github.harbby.gadtry.aop.model.Pointcut;
import com.github.harbby.gadtry.function.exception.Function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AopFactoryImpl
        implements AopFactory
{
    private final List<Pointcut> pointcuts;
    private final Map<Class<?>, Pointcut> pointcutMap;

    public AopFactoryImpl(List<Pointcut> pointcuts)
    {
        this.pointcuts = pointcuts;

        this.pointcutMap = new HashMap<>();
        pointcuts.forEach(pointcut -> {
            for (Class<?> aClass : pointcut.getSearchClass()) {
                pointcutMap.put(aClass, pointcut);
            }
        });
    }

    @Override
    public List<Pointcut> getPointcuts()
    {
        return pointcuts;
    }

    @Override
    public <T> T proxy(Class<T> driver, T instance)
    {
        Pointcut pointcut = pointcutMap.get(driver);
        if (pointcut == null) {
            pointcut = pointcutMap.get(instance.getClass());
        }

        if (pointcut != null) {
            final Function<ProxyContext, Object> handler = pointcut.buildRunHandler();
            return AopFactory.proxy(driver)
                    .byInstance(instance)
                    .whereMethod(pointcut.getMethodFilter())
                    .around(handler);
        }

        return instance;
        //throw new IllegalStateException(String.format("Unable to proxy object %s,ecause no cut point rules are configured", instance));
    }
}
