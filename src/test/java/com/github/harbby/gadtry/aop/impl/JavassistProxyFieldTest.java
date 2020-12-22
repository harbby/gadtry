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
import com.github.harbby.gadtry.collection.MutableList;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.util.List;

public class JavassistProxyFieldTest
{
    @Test
    public void extendsTest()
    {
        People proxy = new PeopleA();
        Assert.assertEquals(proxy.age, proxy.getAge());
        Assert.assertEquals(proxy.name, proxy.getName());
        Assert.assertEquals(proxy.isActive, proxy.isActive());
        Assert.assertEquals(proxy.list, proxy.getList());
    }

    @Test
    public void proxy()
    {
        People people = new People();
        InvocationHandler invocationHandler = (proxy, method, args) -> method.invoke(proxy, args);
        ProxyRequest<People> request = ProxyRequest.builder(People.class)
                .setInvocationHandler(invocationHandler)
                .setClassLoader(getClass().getClassLoader())
                .setTarget(people)
                .build();
        People proxy = JavassistProxy.newProxyInstance(request);

        Assert.assertEquals(proxy.age, proxy.getAge());
        Assert.assertEquals(proxy.name, proxy.getName());
        Assert.assertEquals(proxy.isActive, proxy.isActive());
        Assert.assertEquals(proxy.list, proxy.getList());
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
