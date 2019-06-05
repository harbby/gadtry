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

import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.collection.mutable.MutableList;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProxyInstanceTest
        implements Supplier<String>
{
    @Override
    @Ignore
    public String get()
    {
        return "hello";
    }

    @Test
    public void proxyAfterReturningTest()
    {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AopFactoryTest supplier = AopFactory.proxy(AopFactoryTest.class)
                .byInstance(new AopFactoryTest())
                .methodAnnotated(Ignore.class)
                .afterReturning(methodInfo -> {
                    atomicBoolean.set(true);
                    Assert.assertEquals(methodInfo.getName(), "get");
                });

        Assert.assertEquals(supplier.get(), "hello");
        Assert.assertTrue(atomicBoolean.get());
    }

    @Test
    public void returnTypeTest()
    {
        List<Class<?>> primitiveTypes = com.github.harbby.gadtry.base.Arrays.PRIMITIVE_TYPES;
        List<Class<?>> classWraps = primitiveTypes.stream().map(JavaTypes::getWrapperClass).collect(Collectors.toList());
        List<Class<?>> allTypes = MutableList.<Class<?>>builder()
                .addAll(primitiveTypes)
                .addAll(classWraps)
                .add(String.class)
                .build();

        Set set = AopFactory.proxy(Set.class).byInstance(new HashSet<String>())
                .returnType(allTypes.toArray(new Class[0]))
                .before(methodInfo -> {});
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void jdkPropyTest()
    {
        List<String> actions = new ArrayList<>();
        Set set = AopFactory.proxy(Set.class).byInstance(new HashSet<String>())
                .returnType(void.class, Boolean.class)
                //.methodAnnotated(Override.class, Override.class)
                .around(proxyContext -> {
                    String name = proxyContext.getInfo().getName();
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
                });
        set.clear();
        set.add("t1");
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(actions, Arrays.asList("clear", "add"));
    }

    @Test
    public void noJdkPropyTest()
    {
        List<String> actions = new ArrayList<>();
        Set set = AopFactory.proxy(HashSet.class).byInstance(new HashSet<String>())
                .returnType(void.class, Boolean.class)
                //.methodAnnotated(Override.class, Override.class)
                .after(methodInfo -> {
                    String name = methodInfo.getName();
                    actions.add(name);
                });
        set.clear();
        set.add("t1");
        Assert.assertEquals(1, set.size());
        Assert.assertEquals(actions, Arrays.asList("clear", "add"));
    }
}
