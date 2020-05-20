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
package com.github.harbby.gadtry.aop.serialize;

import com.github.harbby.gadtry.aop.AopGo;
import com.github.harbby.gadtry.aop.impl.ProxyHandler;
import com.github.harbby.gadtry.aop.mock.AopInvocationHandler;
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.collection.mutable.MutableList;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AopGoSerializeTest
{
    private final Function<String, Integer> function =
            (Serializable & Function<String, Integer>) (str) -> str.length();

    @Test
    public void aopGoSerializeTest1() throws Exception
    {
        AbstractClass extendsClass = new AbstractClass.ExtendsClass();

        AbstractClass proxy = AopGo
                .proxy(AbstractClass.class)
                .byInstance(extendsClass)
                .aop(binder -> {
                    binder.doBefore(before -> {
                        System.out.println("beforeSerializeTest1");
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize(proxy);
        AbstractClass serialized = Serializables.byteToObject(bytes);
        AopInvocationHandler handler = (AopInvocationHandler) ((ProxyHandler) proxy).getHandler();
        AopInvocationHandler handler2 = (AopInvocationHandler) ((ProxyHandler) serialized).getHandler();
        Map<Method, Object> map1 = getAopInvocationHandlerMethodMap(handler);
        Map<Method, Object> map2 = getAopInvocationHandlerMethodMap(handler2);

        Assert.assertEquals(map1.keySet(), map2.keySet());
        Assert.assertEquals("desc", serialized.getDesc());
        Assert.assertEquals(18, serialized.getAge());
    }

    private Map<Method, Object> getAopInvocationHandlerMethodMap(AopInvocationHandler handler) throws IllegalAccessException, NoSuchFieldException
    {
        Field field = AopInvocationHandler.class.getDeclaredField("mockMethods");
        field.setAccessible(true);
        Map<Method, Object> map = (Map<Method, Object>) field.get(handler);
        return map;
    }

    @Test
    public void beforeSerializeTest0()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doBefore(before -> {
                        System.out.println("beforeSerializeTest1");
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void serializeJdkProxyObjectTest()
            throws IOException, ClassNotFoundException
    {
        Function<String, List<String>> proxy = AopGo
                .proxy(JavaTypes.<Function<String, List<String>>>classTag(Function.class))
                .byInstance((Serializable & Function<String, List<String>>) (str) -> MutableList.of(str))
                .aop(binder -> {
                    binder.doAround(joinPoint -> {
                        List<String> value = (List<String>) joinPoint.proceed();
                        value.add("doAround");
                        return value;
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Function<String, Integer> serializableProxy = Serializables.byteToObject(bytes);
        Assert.assertEquals(serializableProxy.apply("1"), Arrays.asList("1", "doAround"));
        Assert.assertTrue(serializableProxy.apply("1") != serializableProxy.apply("1"));
    }

    @Test
    public void serializeJavassistProxyProxyObjectTest()
            throws IOException, ClassNotFoundException
    {
        Function<String, List<String>> proxy = AopGo
                .proxy(JavaTypes.<Function<String, List<String>>>classTag(Function.class))
                .byInstance((Serializable & Function<String, List<String>>) (str) -> MutableList.of(str))
                .basePackage("gadtry.aop.javassist")
                .aop(binder -> {
                    binder.doAround(joinPoint -> {
                        List<String> value = (List<String>) joinPoint.proceed();
                        value.add("doAround");
                        return value;
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Function<String, Integer> serializableProxy = Serializables.byteToObject(bytes);
        Assert.assertEquals(serializableProxy.apply("1"), Arrays.asList("1", "doAround"));
        Assert.assertTrue(serializableProxy.apply("1") != serializableProxy.apply("1"));
    }

    @Test
    public void beforeSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doBefore(before -> {
                        System.out.println("beforeSerializeTest1");
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void beforeSerializeTest2()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doBefore(before -> {
                        System.out.println(before);
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterReturningSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doAfterReturning(before -> {
                        System.out.println("afterReturningSerializeTest1");
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterThrowingSerializeTest2()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doAfterThrowing(before -> {
                        System.out.println("afterReturningSerializeTest1");
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void aroundSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doAround(joinPoint -> {
                        return joinPoint.proceed();
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void aroundSerializeTest2()
            throws Exception
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doAround(joinPoint -> {
                        Object value = joinPoint.proceed();
                        System.out.println(value);
                        return value;
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doAfter(joinPoint -> {
                        System.out.println("afterSerializeTest1");
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterSerializeTest2()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo
                .proxy(JavaTypes.<Function<String, Integer>>classTag(Function.class))
                .byInstance(function)
                .aop(binder -> {
                    binder.doAfter(after -> {
                        System.out.println(after);
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }
}
