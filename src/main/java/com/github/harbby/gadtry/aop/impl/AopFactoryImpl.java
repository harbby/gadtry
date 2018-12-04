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
import com.github.harbby.gadtry.aop.model.Pointcut;

import java.util.List;

public class AopFactoryImpl
        implements AopFactory
{
    private final List<Pointcut> pointcuts;
    //private final Map<Class<?>, Pointcut> bindingMap;

    public AopFactoryImpl(List<Pointcut> pointcuts)
    {
        this.pointcuts = pointcuts;
        //ClassScanner scanner = ClassScanner.scanClasses()
    }

    @Override
    public List<Pointcut> getPointcuts()
    {
        return pointcuts;
    }
}
