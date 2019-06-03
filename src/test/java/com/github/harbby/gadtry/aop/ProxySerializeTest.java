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
package com.github.harbby.gadtry.aop;

import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.function.exception.Function;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

@SuppressWarnings("unchecked")
public class ProxySerializeTest
{
    @Test
    public void beforeSerializeTest0()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .whereMethod(methodInfo -> true)
                .before(methodInfo -> {
                    System.out.println("beforeSerializeTest1");
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void beforeSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .before(methodInfo -> {
                    System.out.println("beforeSerializeTest1");
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void beforeSerializeTest2()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .before(methodInfo -> {
                    System.out.println(methodInfo);
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterReturningSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .afterReturning(methodInfo -> {
                    System.out.println("afterReturningSerializeTest1");
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterThrowingSerializeTest2()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .afterThrowing(methodInfo -> {
                    System.out.println(methodInfo);
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void aroundSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .around(proxyContext -> {
                    return proxyContext.proceed();
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void aroundSerializeTest2()
            throws Exception
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .around(proxyContext -> {
                    Object value = proxyContext.proceed();
                    System.out.println(value);
                    return value;
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .after(methodInfo -> {
                    System.out.println("afterSerializeTest1");
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterSerializeTest2()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> function = (str) -> str.length();

        Function<String, Integer> proxy = AopFactory.proxy(Function.class)
                .byInstance(function)
                .after(methodInfo -> {
                    System.out.println(methodInfo);
                });

        byte[] bytes = Serializables.serialize(proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }
}
