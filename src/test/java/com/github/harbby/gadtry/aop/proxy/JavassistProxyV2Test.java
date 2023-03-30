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
package com.github.harbby.gadtry.aop.proxy;

import com.github.harbby.gadtry.aop.ProxyRequest;
import com.github.harbby.gadtry.aop.model.Test1;
import com.github.harbby.gadtry.collection.MutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavassistProxyV2Test
{
    @Test
    public void javassistProxyV2Test()
    {
        // v2 method 名字加了前缀$_
        String name = "abc-";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

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
                .setNewInstance(proxyClass -> {
                    Constructor<? extends Test1> constructor = proxyClass.getConstructor(String.class);
                    return constructor.newInstance(name);
                })
                .build();
        Test1 proxy = JavassistProxyV2.javassistProxyV2.newProxyInstance(request);

        Assertions.assertEquals(18 - 1, proxy.age());
        Assertions.assertEquals(name, proxy.name());
        Assertions.assertTrue(proxy instanceof ProxyAccess);
        Assertions.assertTrue(atomicBoolean.get());
        //---支持方法间this调用
        Assertions.assertEquals(proxy.getNameAndAge(), "abc-17");
        System.out.println(proxy);
    }

    @Test
    public void javassistProxyV1Test()
    {
        String name = "abc-";
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
        //使用v1代理，不支持this super支持，不支持方法间this调用支持
        ProxyRequest<Test1> request = ProxyRequest.builder(Test1.class)
                .setClassLoader(Test1.class.getClassLoader())
                .addInterface(Serializable.class)
                .setInvocationHandler(handler)
                .build();
        //Test1 proxy = JavassistProxy.newProxyInstance(Test1.class.getClassLoader(), handler, Test1.class, Serializable.class);
        Test1 proxy = JavassistProxy.javassistProxy.newProxyInstance(request);

        Assertions.assertEquals(18 - 1, proxy.age()); //这里因为上面age方法代理成-1,因此是17
        Assertions.assertEquals(name, proxy.name());
        Assertions.assertTrue(proxy instanceof ProxyAccess);
        Assertions.assertTrue(atomicBoolean.get());

        //---不支持方法间this调用
        Assertions.assertEquals(proxy.getNameAndAge(), "abc-18");  //虽然上面代理了age()方法，但是这里并未生效
    }

    @Test
    public void extendsTest()
    {
        People proxy = new PeopleA();
        Assertions.assertEquals(proxy.age, proxy.getAge());
        Assertions.assertEquals(proxy.name, proxy.getName());
        Assertions.assertEquals(proxy.isActive, proxy.isActive());
        Assertions.assertEquals(proxy.list, proxy.getList());
    }

    @Test
    public void v2proxyTest()
    {
        InvocationHandler invocationHandler = (proxy, method, args) -> method.invoke(proxy, args);
        ProxyRequest<People> request = ProxyRequest.builder(People.class)
                .setInvocationHandler(invocationHandler)
                .setClassLoader(getClass().getClassLoader())
                .setNewInstance(aClass -> aClass.getConstructor().newInstance())
                .build();
        People proxy = JavassistProxyV2.javassistProxyV2.newProxyInstance(request);
        Assertions.assertEquals(proxy.age, proxy.getAge());
        Assertions.assertEquals(proxy.name, proxy.getName());
        Assertions.assertEquals(proxy.isActive, proxy.isActive());
        Assertions.assertEquals(proxy.list, proxy.getList());
    }

    public static class PeopleA
            extends People
    {
    }

    public static class People
    {
        public final List<String> list = MutableList.of("1", "2");
        protected final String name = "name";
        final int age = 18;
        private final boolean isActive = true;

        public List<String> getList()
        {
            return list;
        }

        public String getName()
        {
            return name;
        }

        public int getAge()
        {
            return age;
        }

        public boolean isActive()
        {
            return isActive;
        }
    }
}
