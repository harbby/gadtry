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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AopFactoryTest
{
    @Test
    public void aopCreate()
    {
        AopFactory aopFactory = AopFactory.create(binder -> {
            binder.bind("anyMethod")
                    .withPackage("com.github.harbby")
                    .classes(HashSet.class) // or Set
                    .whereMethod(method -> method.getName().startsWith("cle"))
                    .build()
                    .before(() -> {
                        System.out.println("before");
                    })
                    .after(() -> {
                        System.out.println("after");
                    });
        });

        Assert.assertNotNull(aopFactory);
        System.out.println(aopFactory);

        Set set = aopFactory.proxy(Set.class, new HashSet());
        set.clear();
    }

    @Test
    public void proxyInstanceTest()
    {
        Map<String, Integer> map = AopFactory.proxyInstance(new HashMap<String, Integer>())
                .byClass(Map.class)
                .after(methodInfo -> {
                    System.out.println(methodInfo);
                });
        Assert.assertEquals(map, new HashMap<>());
    }

    private static <T> T getProxy(Class<T> key, T instance)
    {
        return AopFactory.proxy(key).byInstance(instance)
                .returnType(void.class, Boolean.class)
                //.methodAnnotated(Override.class, Override.class)
                .around(proxyContext -> {
                    String name = proxyContext.getInfo().getName();
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
    }

    @Test
    public void jdkPropyTest()
    {
        Set set = getProxy(Set.class, new HashSet<String>());
        set.clear();
        set.add("t1");
        Assert.assertEquals(1, set.size());
    }

    @Test
    public void noJdkPropyTest()
    {
        Set set = getProxy(HashSet.class, new HashSet<String>());
        set.clear();
        set.add("t1");
        Assert.assertEquals(1, set.size());

        System.out.println();
    }
}
