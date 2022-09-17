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
package com.github.harbby.gadtry.aop.asm;

import com.github.harbby.gadtry.aop.ProxyRequest;
import com.github.harbby.gadtry.aop.model.Test1;
import com.github.harbby.gadtry.aop.proxy.ProxyFactory;
import com.github.harbby.gadtry.aop.proxy2.AsmProxyV3;
import com.github.harbby.gadtry.aop.proxy2.Interceptor;
import com.github.harbby.gadtry.aop.proxy2.MockAccess;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsmProxyTestV3
{
    public abstract static class AbstractUser
    {
        public abstract String getName();

        public abstract int getAge();

        public double merge(String a, int b, float c, double d)
        {
            return a.length() + b + c + d;
        }

        protected String getNameAge()
        {
            return this.getName() + ":" + this.getAge();
        }
    }

    @Test
    public void v3BaseTest()
            throws Exception
    {
        ProxyFactory factory = AsmProxyV3.asmProxyV3;
        Class<? extends AbstractUser> aClass = factory.getProxyClass(ClassLoader.getSystemClassLoader(), AbstractUser.class);
        AbstractUser user = aClass.getConstructor().newInstance();
        Field field = user.getClass().getDeclaredField("handler");
        field.setAccessible(true);
        field.set(user, (Interceptor) (proxy, method, caller) -> caller.call());
        System.out.println(user.getNameAge());
        System.out.println(user.merge("b", 1, 1, 1));
    }

    @Test
    public void asmProxyV3Test()
            throws Exception
    {
        String name = "abc-";
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Interceptor handler = (proxy, method, caller) -> {
            System.out.println("before " + method.getName());
            if ("name".equals(method.getName())) {
                atomicBoolean.set(true);
            }
            else if ("age".equals(method.getName())) {
                return (int) caller.call() - 1;
            }
            return caller.call();
        };
        ProxyRequest<Test1> request = ProxyRequest.builder(Test1.class)
                .setClassLoader(Test1.class.getClassLoader())
                .addInterface(Serializable.class)
                .addInterface(java.util.function.DoubleConsumer.class)
                .setNewInstance(proxyClass -> {
                    Constructor<? extends Test1> constructor = proxyClass.getConstructor(String.class);
                    return constructor.newInstance(name);
                })
                //.setInvocationHandler(handler)
                .build();
        ProxyFactory factory = AsmProxyV3.asmProxyV3;
        Class<? extends Test1> proxyClass = factory.getProxyClass(request);
        Constructor<? extends Test1> constructor = proxyClass.getConstructor(String.class);
        Test1 proxy = constructor.newInstance(name);
        Field field = proxy.getClass().getDeclaredField("handler");
        field.setAccessible(true);
        field.set(proxy, handler);

        Assert.assertEquals(18 - 1, proxy.age());
        Assert.assertEquals(name, proxy.name());
        Assert.assertEquals(9.14D, proxy.sum("abc", 1, 2L, 3.14F), 0.000001);
        Assert.assertTrue(proxy instanceof MockAccess);
        Assert.assertTrue(atomicBoolean.get());
        //---支持方法间this调用
        Assert.assertEquals(proxy.getNameAndAge(), "abc-17");
        System.out.println(proxy.toString());
    }
}
