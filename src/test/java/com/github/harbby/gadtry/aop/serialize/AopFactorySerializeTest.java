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
import com.github.harbby.gadtry.base.Serializables;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class AopFactorySerializeTest
{
    private final Function<String, Integer> function =
            (Serializable & Function<String, Integer>) (str) -> str.length();

    @Test
    public void beforeSerializeTest0()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doBefore(methodInfo -> {
                        System.out.println("beforeSerializeTest1");
                    }).whereMethod(methodInfo -> true);
                })
                .build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void beforeSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doBefore(methodInfo -> {
                        System.out.println("beforeSerializeTest1");
                    }).allMethod();
                })
                .build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void beforeSerializeTest2()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doBefore(methodInfo -> {
                        System.out.println(methodInfo);
                    }).allMethod();
                })
                .build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterReturningSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doAfterReturning(methodInfo -> {
                        System.out.println("afterReturningSerializeTest1");
                    }).allMethod();
                })
                .build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void afterThrowingSerializeTest2()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doAfterThrowing(methodInfo -> {
                        System.out.println(methodInfo);
                    }).allMethod();
                })
                .build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void aroundSerializeTest1()
            throws IOException, ClassNotFoundException
    {
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doAround(proxyContext -> {
                        return proxyContext.proceed();
                    }).allMethod();
                })
                .build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }

    @Test
    public void aroundSerializeTest2()
            throws Exception
    {
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doAround(proxyContext -> {
                        Object value = proxyContext.proceed();
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
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doAfter(methodInfo -> {
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
        Function<String, Integer> proxy = AopGo.proxy(Function.class)
                .byInstance(function)
                .aop(binder -> {
                    binder.doAfter(methodInfo -> {
                        System.out.println(methodInfo);
                    }).allMethod();
                }).build();

        byte[] bytes = Serializables.serialize((Serializable) proxy);
        Assert.assertTrue(Serializables.byteToObject(bytes) instanceof Function);
    }
}
