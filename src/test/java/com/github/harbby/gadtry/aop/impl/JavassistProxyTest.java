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

import com.github.harbby.gadtry.base.Streams;
import com.github.harbby.gadtry.memory.UnsafeHelper;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
        Test1 proxy = JavassistProxy.newProxyInstance(Test1.class.getClassLoader(), handler, Test1.class);
        int age = proxy.age();
        Assert.assertEquals(18, age);
        Assert.assertEquals(name, proxy.name());
        Assert.assertTrue(proxy instanceof ProxyHandler);
        Assert.assertTrue(atomicBoolean.get());
        System.out.println(proxy);

        Test1 proxy2 = JavassistProxy.newProxyInstance(Test1.class.getClassLoader(), handler, Test1.class);
        System.out.println(proxy2);
    }

    @Test
    public void getProxyClassTest()
            throws Exception
    {
        File workDir = new File(System.getProperty("java.io.tmpdir"), "pluginLoaderTest_342634345");
        Class<?> proxyClass = JavassistProxy.getProxyClass(this.getClass().getClassLoader(), List.class);
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.makeClass(proxyClass.getName());

        byte[] bytes = ctClass.toBytecode();
        Assert.assertTrue(bytes.length > 0);
    }

    @Test
    public void testConcurrent10()
    {
        Unsafe unsafe = UnsafeHelper.getUnsafe();
        //unsafe.allocateInstance(null); 会让jvm崩溃
        Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).parallel().forEach(x -> {
            Class<?> aClass = null;
            try {
                if (x % 2 == 0) {
                    aClass = JavassistProxy.getProxyClass(Runnable.class.getClassLoader(), Runnable.class);
                }
                else {
                    aClass = JavassistProxy.getProxyClass(Runnable.class.getClassLoader(), Callable.class);
                }
                Object obj = unsafe.allocateInstance(aClass);
                Assert.assertNotNull(obj);
            }
            catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        });
    }

    @Test
    public void testConcurrent10GiveHashSet()
    {
        ClassLoader classLoader = HashSet.class.getClassLoader();
        Streams.range(10).parallel()
                .forEach(x -> {
                    try {
                        Class<?> aClass = JavassistProxy.getProxyClass(classLoader, HashSet.class);
                        Assert.assertTrue(HashSet.class.isAssignableFrom(aClass));
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    public void testConcurrent_CLass()
    {
        Unsafe unsafe = UnsafeHelper.getUnsafe();
        //unsafe.allocateInstance(null); 会让jvm崩溃
        Stream.of(Supplier.class, Supplier.class, Supplier.class, Supplier.class, Supplier.class, Supplier.class,
                HashMap.class, HashSet.class, ArrayList.class, Test.class, Test1.class, JavassistProxyTest.class)
                .parallel()
                .forEach(x -> {
                    try {
                        Class<?> aClass = JavassistProxy.getProxyClass(null, x);
                        Object obj = unsafe.allocateInstance(aClass);

                        Assert.assertEquals(true, x.isInstance(obj));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail(e.getMessage());
                    }
                });
    }

    @Test
    public void genericClassProxyTest()
    {
        final GenericProxyClass genericProxyClass = new GenericProxyClass();
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            if ("get".equals(method.getName())) {
                return "hello";
            }
            return method.invoke(genericProxyClass, args);
        };
        GenericProxyClass proxy = JavassistProxy.newProxyInstance(null, invocationHandler, GenericProxyClass.class);
        Assert.assertEquals(proxy.get(), "hello");
    }

    public static class GenericProxyClass
            implements Supplier<String>, Provider<String>
    {
        @Override
        public String get()
        {
            return null;
        }
    }

    public interface Provider<V>
    {
        V get();
    }
}
