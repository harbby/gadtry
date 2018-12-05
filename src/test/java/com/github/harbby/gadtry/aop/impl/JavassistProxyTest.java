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

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavassistProxyTest
{
    @Test
    public void myProxyTest()
    {
        String name = "123123-1";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Test1 old = Test1.of(name);
        InvocationHandler handler = (proxy, method, args1) -> {
            System.out.println("before " + method.getName());
            if ("name".equals(method.getName())) {
                atomicBoolean.set(true);
            }
            return method.invoke(old, args1);
        };
        Test1 proxy = JavassistProxy.newProxyInstance(Test1.class.getClassLoader(), Test1.class, handler);
        int age = proxy.age();
        Assert.assertEquals(18, age);
        Assert.assertEquals(name, proxy.name());
        Assert.assertTrue(proxy instanceof Proxy.ProxyHandler);
        Assert.assertEquals(true, atomicBoolean.get());
        System.out.println(proxy);

        Test1 proxy2 = JavassistProxy.newProxyInstance(Test1.class.getClassLoader(), Test1.class, handler);
        System.out.println(proxy2);
    }
}
