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
import com.github.harbby.gadtry.aop.proxy.ProxyAccess;
import com.github.harbby.gadtry.aop.proxy.ProxyFactory;
import com.github.harbby.gadtry.base.JavaTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class AsmProxyTest
{
    @Test
    public void asmProxyV1Test()
            throws Exception
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
        ProxyRequest<Test1> request = ProxyRequest.builder(Test1.class)
                .setClassLoader(Test1.class.getClassLoader())
                .addInterface(Serializable.class)
                .addInterface(java.util.function.DoubleConsumer.class)
                .setInvocationHandler(handler)
                .build();

        ProxyFactory factory = ProxyFactory.getAsmProxy();
        Test1 proxy = factory.newProxyInstance(request);

        Assertions.assertEquals(18 - 1, proxy.age()); //这里因为上面age方法代理成-1,因此是17
        Assertions.assertEquals(name, proxy.name());
        Assertions.assertEquals(9.14D, proxy.sum("abc", 1, 2L, 3.14F), 0.000001);
        Assertions.assertTrue(proxy instanceof ProxyAccess);
        Assertions.assertTrue(atomicBoolean.get());

        //---不支持方法间this调用
        Assertions.assertEquals(proxy.getNameAndAge(), "abc-18");  //虽然上面代理了age()方法，但是这里并未生效
    }

    @Test
    public void proxyHashSetUseAsmProxy()
    {
        Set<String> set = new HashSet<>();
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            return method.invoke(set, args);
        };
        ProxyRequest<HashSet<String>> request = ProxyRequest.builder(JavaTypes.<HashSet<String>>classTag(HashSet.class))
                .setInvocationHandler(invocationHandler)
                .addInterface(Supplier.class)
                .addInterface(Supplier.class)
                .build();
        ProxyFactory proxyFactory = ProxyFactory.getAsmProxy();
        Set<String> obj = proxyFactory.newProxyInstance(request);
        Assertions.assertTrue(obj instanceof ProxyAccess);
    }
}
