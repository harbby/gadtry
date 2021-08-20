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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyInstanceTest
{
    @Deprecated
    String get()
    {
        return "hello";
    }

    @Test
    public void proxyAfterReturningTest()
    {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        ProxyInstanceTest supplier = AopGo.proxy(ProxyInstanceTest.class)
                .byInstance(new ProxyInstanceTest())
                .aop(binder -> {
                    binder.doAfterReturning(methodInfo -> {
                        atomicBoolean.set(true);
                        Assert.assertEquals(methodInfo.getName(), "get");
                    }).annotated(Deprecated.class);
                }).build();

        Assert.assertEquals(supplier.get(), "hello");
        Assert.assertTrue(atomicBoolean.get());
    }

    @Test
    public void returnTypeTest()
    {
        List<String> rs = new ArrayList<>();
        Set set = AopGo.proxy(Set.class).byInstance(new HashSet<String>())
                .aop(binder -> {
                    binder.doBefore(before -> {
                        rs.add(before.getName());
                    }).returnType(boolean.class);
                })
                .build();
        Assert.assertTrue(set.isEmpty());
        Assert.assertEquals(Arrays.asList("isEmpty"), rs);
    }

    @Test
    public void jdkPropyTest()
    {
        List<String> actions = new ArrayList<>();
        Set set = AopGo.proxy(Set.class).byInstance(new HashSet<String>())
                .aop(binder -> {
                    binder.doAround(proxyContext -> {
                        String name = proxyContext.getName();
                        actions.add(name);
                        System.out.println("around: " + name);
                        Object value = proxyContext.proceed();
                        switch (name) {
                            case "add":
                                Assert.assertEquals(true, value);  //Set or List
                                break;
                            case "size":
                                Assert.assertTrue(value instanceof Integer);
                                break;
                        }
                        return value;
                    }).returnType(void.class, Boolean.class);
                    //.methodAnnotated(Override.class, Override.class)
                }).build();

        set.clear();
        set.add("t1");
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(actions, Arrays.asList("clear", "add"));
    }

    @Test
    public void propyTest2()
    {
        List<String> actions = new ArrayList<>();
        Set set = AopGo.proxy(Set.class).byInstance(new HashSet<String>())
                .aop(binder -> {
                    binder.doAfter(methodInfo -> {
                        String name = methodInfo.getName();
                        actions.add(name);
                    }).returnType(void.class, Boolean.class);
                    //.methodAnnotated(Override.class, Override.class)
                }).build();
        set.clear();
        set.add("t1");
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(actions, Arrays.asList("clear", "add"));
    }
}
