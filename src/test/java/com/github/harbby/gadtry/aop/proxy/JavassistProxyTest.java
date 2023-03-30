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

import com.github.harbby.gadtry.aop.mockgo.MockGoException;
import com.github.harbby.gadtry.aop.model.Test1;
import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.base.Streams;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JavassistProxyTest
{
    private final ProxyFactory factory = JavassistProxy.javassistProxy;

    @Test
    public void moreSuperclass()
            throws Exception
    {
        try {
            factory.getProxyClass(getClass().getClassLoader(), ArrayList.class, Serializable.class,
                    ProxyAccess.class, HashMap.class);
            Assertions.fail();
        }
        catch (IllegalStateException e) {
            Assertions.assertEquals(e.getMessage(), "java.util.HashMap not is Interface");
        }
    }

    @Test
    public void finalSuperclass()
            throws Exception
    {
        try {
            factory.getProxyClass(getClass().getClassLoader(), Boolean.class, Serializable.class, ProxyAccess.class);
            Assertions.fail();
        }
        catch (MockGoException e) {
            Assertions.assertEquals(e.getCause().getMessage(), "class java.lang.Boolean is final");
        }
    }

    @Test
    public void getInvocationHandler()
            throws Exception
    {
        List<String> list = new ArrayList<>();
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            if ("get".equals(method.getName())) {
                return "hello";
            }
            return method.invoke(list, args);
        };
        List<String> proxy = factory.newProxyInstance(getClass().getClassLoader(), invocationHandler,
                ArrayList.class, Serializable.class, ProxyAccess.class);
        Assertions.assertTrue(invocationHandler == factory.getInvocationHandler(proxy));
    }

    @Test
    public void getProxyClassTest()
            throws Exception
    {
        File workDir = new File(System.getProperty("java.io.tmpdir"), "pluginLoaderTest_342634345");
        Class<?> proxyClass = factory.getProxyClass(this.getClass().getClassLoader(), List.class);
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.makeClass(proxyClass.getName());

        byte[] bytes = ctClass.toBytecode();
        Assertions.assertTrue(bytes.length > 0);
    }

    @Test
    public void testConcurrent10()
    {
        Unsafe unsafe = Platform.getUnsafe();
        Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).parallel().forEach(x -> {
            Class<?> aClass = null;
            try {
                if (x % 2 == 0) {
                    aClass = factory.getProxyClass(Runnable.class.getClassLoader(), Runnable.class);
                }
                else {
                    aClass = factory.getProxyClass(Runnable.class.getClassLoader(), Callable.class);
                }
                Object obj = unsafe.allocateInstance(aClass);
                Assertions.assertNotNull(obj);
            }
            catch (Exception e) {
                e.printStackTrace();
                Assertions.fail();
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
                        Class<?> aClass = factory.getProxyClass(classLoader, HashSet.class);
                        Assertions.assertTrue(HashSet.class.isAssignableFrom(aClass));
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    public void proxyHashMapTest()
            throws InstantiationException
    {
        Unsafe unsafe = Platform.getUnsafe();
        Class<?> aClass = factory.getProxyClass(null, HashMap.class);
        Object obj = unsafe.allocateInstance(aClass);

        Assertions.assertEquals(true, HashMap.class.isInstance(obj));
    }

    @Test
    public void testConcurrent_CLass()
    {
        Unsafe unsafe = Platform.getUnsafe();
        //unsafe.allocateInstance(null); 会让jvm崩溃
        Stream.of(Supplier.class, Supplier.class, Supplier.class, Supplier.class, Supplier.class, Supplier.class,
                        HashMap.class, HashSet.class, ArrayList.class, Test.class, Test1.class, JavassistProxyTest.class)
                .parallel()
                .forEach(x -> {
                    try {
                        Class<?> aClass = factory.getProxyClass(null, x);
                        Object obj = unsafe.allocateInstance(aClass);

                        Assertions.assertEquals(true, x.isInstance(obj));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Assertions.fail(e.getMessage());
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
        GenericProxyClass proxy = factory.newProxyInstance(null, invocationHandler, GenericProxyClass.class);
        Assertions.assertEquals(proxy.get(), "hello");
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
