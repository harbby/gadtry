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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class JDKProxyTest
{
    @Test
    public void of()
    {
        CutModeImpl<Set> setAopProxy = CutModeImpl.of(Set.class, new HashSet(), JdkProxy::newProxyInstance);
        Assert.assertNotNull(setAopProxy);
    }

    @Test
    public void around()
    {
        Set<String> old = new HashSet<>();
        old.add("old");

        Set set = CutModeImpl.of(Set.class, old, JdkProxy::newProxyInstance)
                .around(proxyContext -> {
                    String name = proxyContext.getInfo().getName();
                    Object value = proxyContext.proceed();
                    switch (name) {
                        case "add":
                            Assert.assertEquals(true, value);
                            break;
                        case "size":
                            Assert.assertEquals(2, value);
                            break;
                    }
                });

        set.add("t1");
        Assert.assertEquals(2, set.size());
    }

    @Test
    public void before()
    {
    }

    @Test
    public void before1()
    {
    }

    @Test
    public void afterReturning()
    {
    }

    @Test
    public void afterReturning1()
    {
    }

    @Test
    public void after()
    {
    }

    @Test
    public void after1()
    {
    }

    @Test
    public void afterThrowing()
    {
    }

    @Test
    public void afterThrowing1()
    {
    }

    @Test
    public void getMethodInfo()
    {
    }
}
