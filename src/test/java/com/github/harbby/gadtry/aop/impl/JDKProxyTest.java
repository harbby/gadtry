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
package com.github.harbby.gadtry.aop.impl;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JDKProxyTest
{
    @Test
    public void newProxyInstance()
            throws Exception
    {
        Supplier<String> set1 = () -> "done";
        InvocationHandler handler = (proxy, method, args) -> {
            return method.invoke(set1, args);
        };
        Supplier<String> proxySet = JdkProxy.newProxyInstance(getClass().getClassLoader(), handler, Supplier.class);
        List a2 = Stream.of(proxySet.getClass().getDeclaredFields())
                .map(field -> {
                    field.setAccessible(true);
                    try {
                        return field.get(null);
                    }
                    catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

//        ClassPool classPool = new ClassPool(true);
//        classPool.appendClassPath(new LoaderClassPath(proxySet.getClass().getClassLoader()));
//        CtClass ctClass = classPool.get(proxySet.getClass().getName());
        Assert.assertEquals(proxySet.get(), "done");
    }

    @Test
    public void around()
    {
        Set<String> old = new HashSet<>();
        old.add("old");

        Set set = CutModeImpl.of(Set.class, old, JdkProxy::newProxyInstance)
                .around(proxyContext -> {
                    String name = proxyContext.getName();
                    Object value = proxyContext.proceed();
                    switch (name) {
                        case "add":
                            Assert.assertEquals(true, value);
                            break;
                        case "size":
                            Assert.assertEquals(2, value);
                            break;
                    }
                    return value;
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
