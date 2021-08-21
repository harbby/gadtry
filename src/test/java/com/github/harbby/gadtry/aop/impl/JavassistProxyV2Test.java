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
import com.github.harbby.gadtry.aop.codegen.JavassistProxy;
import com.github.harbby.gadtry.aop.codegen.ProxyAccess;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavassistProxyV2Test
{
    private static class Test1
    {
        private final String name;

        public Test1(String name)
        {
            this.name = name;
        }

        public String name()
        {
            return name;
        }

        public int age()
        {
            return 18;
        }

        public String getNameAndAge()
        {
            return name() + age();
        }
    }

    @Test
    public void javassistProxyV2Test()
    {
        /**
         * v2 method 名字加了前缀$_
         * */
        String name = "123123-1";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Test1 old = new Test1(name);
        InvocationHandler handler = (proxy, method, args1) -> {
            System.out.println("before " + method.getName());
            if ("$_name".equals(method.getName())) {
                atomicBoolean.set(true);
            }
            else if ("$_age".equals(method.getName())) {
                return (int) method.invoke(proxy, args1) - 1;
            }
            return method.invoke(proxy, args1);
        };
        ProxyRequest<Test1> request = ProxyRequest.builder(Test1.class)
                .setClassLoader(Test1.class.getClassLoader())
                .setInvocationHandler(handler)
                .enableV2()
                .setTarget(old)
                .build();
        Test1 proxy = JavassistProxy.newProxyInstance(request);

        Assert.assertEquals(18 - 1, proxy.age());
        Assert.assertEquals(name, proxy.name());
        Assert.assertTrue(proxy instanceof ProxyAccess);
        Assert.assertTrue(atomicBoolean.get());
        System.out.println(proxy);

        //---支持方法间this调用
        Assert.assertEquals(proxy.getNameAndAge(), "123123-117");   //上面带里age()，这里支持方法间this，所以生效了
    }

    @Test
    public void javassistProxyV1Test()
    {
        String name = "123123-1";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Test1 old = new Test1(name);
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
        //使用v1代理，关闭this super支持，关闭方法间this调用支持
        ProxyRequest<Test1> request = ProxyRequest.builder(Test1.class)
                .setClassLoader(Test1.class.getClassLoader())
                .addInterface(Serializable.class)
                .setInvocationHandler(handler)
                .setTarget(old)
                .build();
        //Test1 proxy = JavassistProxy.newProxyInstance(Test1.class.getClassLoader(), handler, Test1.class, Serializable.class);
        Test1 proxy = JavassistProxy.newProxyInstance(request);

        Assert.assertEquals(18 - 1, proxy.age()); //这里因为上面age方法代理成-1,因此是17
        Assert.assertEquals(name, proxy.name());
        Assert.assertTrue(proxy instanceof ProxyAccess);
        Assert.assertTrue(atomicBoolean.get());

        //---不支持方法间this调用
        Assert.assertEquals(proxy.getNameAndAge(), "123123-118");  //虽然上面代理了age()方法，但是这里并未生效
    }
}
