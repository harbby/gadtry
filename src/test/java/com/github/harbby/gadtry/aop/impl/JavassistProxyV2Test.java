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

import com.github.harbby.gadtry.aop.ProxyRequest;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavassistProxyV2Test
{
    @Test
    public void javassistProxyV2Test()
    {
        String name = "123123-1";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Test1 old = Test1.of(name);
        InvocationHandler handler = (proxy, method, args1) -> {
            System.out.println("before " + method.getName());
            if ("name".equals(method.getName())) {
                atomicBoolean.set(true);
            }
            else if ("age".equals(method.getName())) {
                return (int) method.invoke(proxy, args1) - 1;
            }
            return method.invoke(proxy, args1);
        };
        ProxyRequest<Test1> request = ProxyRequest.builder(Test1.class)
                .setClassLoader(Test1.class.getClassLoader())
                .setInvocationHandler(handler)
                .setTarget(old)
                .build();
        Test1 proxy = JavassistProxy.newProxyInstance(request);

        Assert.assertEquals(18 - 1, proxy.age());
        Assert.assertEquals(name, proxy.name());
        Assert.assertTrue(proxy instanceof ProxyHandler);
        Assert.assertTrue(atomicBoolean.get());
        System.out.println(proxy);

        //---支持方法间this调用
        Assert.assertEquals(proxy.getNameAndAge(), "123123-117");
    }

    @Test
    public void javassistProxyV1Test()
    {
        String name = "123123-1";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Test1 old = Test1.of(name);
        InvocationHandler handler = (proxy, method, args1) -> {
            System.out.println("before " + method.getName());
            if ("name".equals(method.getName())) {
                atomicBoolean.set(true);
            }
            else if ("age".equals(method.getName())) {
                return (int) method.invoke(old, args1) - 1;
            }
            return method.invoke(old, args1);
        };
        ProxyRequest<Test1> request = ProxyRequest.builder(Test1.class)
                .setClassLoader(Test1.class.getClassLoader())
                .addInterface(Serializable.class)
                .setInvocationHandler(handler)
                .disableSuperMethod()  //使用v1代理，关闭this super支持
                .setTarget(old)
                .build();
        //Test1 proxy = JavassistProxy.newProxyInstance(Test1.class.getClassLoader(), handler, Test1.class, Serializable.class);
        Test1 proxy = JavassistProxy.newProxyInstance(request);

        Assert.assertEquals(18 - 1, proxy.age());
        Assert.assertEquals(name, proxy.name());
        Assert.assertTrue(proxy instanceof ProxyHandler);
        Assert.assertTrue(atomicBoolean.get());

        //---不支持方法间this调用
        Assert.assertEquals(proxy.getNameAndAge(), "123123-118");
    }
}
