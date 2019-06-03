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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class AopFactoryTest
        implements Supplier<String>
{
    @Override
    @Ignore
    public String get()
    {
        return "hello";
    }

    @Test
    public void aopCreate()
    {
        List<String> actions = new ArrayList<>();
        AopFactory aopTest = AopFactory.create(binder -> {
            binder.bind("anyMethod")
                    .withPackage("com.github.harbby")
                    .classes(HashSet.class) // or Set
                    .whereMethod(method -> method.getName().startsWith(""))
                    .build()
                    .before(methodInfo -> {
                        Assert.assertTrue(actions.isEmpty());
                        actions.add("before");
                    })
                    .afterReturning(methodInfo -> {
                        Assert.assertEquals(actions, Arrays.asList("before"));
                        actions.add("afterReturning");
                    })
                    .afterThrowing(methodInfo -> {
                        Assert.assertEquals(actions, Arrays.asList("before"));
                        actions.add("afterThrowing");
                    })
                    .after(methodInfo -> {
                        Assert.assertEquals(actions.size(), 2);
                        actions.add("after");
                    });
        });
        Set set = aopTest.proxy(Set.class, new HashSet()
        {
            @Override
            public void clear()
            {
                throw new UnsupportedOperationException("aop_test");
            }
        });
        set.isEmpty();
        Assert.assertEquals(actions, Arrays.asList("before", "afterReturning", "after"));

        //
        actions.clear();
        try {
            set.clear();
            Assert.fail();
        }
        catch (UnsupportedOperationException e) {
            Assert.assertEquals(e.getMessage(), "aop_test");
        }
        Assert.assertEquals(actions, Arrays.asList("before", "afterThrowing", "after"));
    }

    @Test
    public void aopAroundTest()
    {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AopFactory aopFactory = AopFactory.create(binder -> {
            binder.bind("anyMethod")
                    .withPackage("com.github.harbby")
                    .classes(AopFactoryTest.class) // or Set
                    .methodAnnotated(Ignore.class)
                    .whereMethod(method -> method.getName().startsWith("get"))
                    .build()
                    .around(proxyContext -> {
                        atomicBoolean.set(true);
                        Assert.assertEquals(proxyContext.getInfo().getName(), "get");
                        return proxyContext.proceed();
                    });
        });

        AopFactoryTest aopTest = aopFactory.proxy(AopFactoryTest.class, new AopFactoryTest());

        Assert.assertEquals(aopTest.get(), "hello");
        Assert.assertTrue(atomicBoolean.get());
    }

    @Test
    public void aopAfterReturningTest()
    {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AopFactory aopFactory = AopFactory.create(binder -> {
            binder.bind("anyMethod")
                    .withPackage("com.github.harbby")
                    .classes(AopFactoryTest.class) // or Set
                    .methodAnnotated(Ignore.class)
                    .whereMethod(method -> method.getName().startsWith("get"))
                    .build()
                    .afterReturning(methodInfo -> {
                        atomicBoolean.set(true);
                        Assert.assertEquals(methodInfo.getName(), "get");
                    });
        });

        AopFactoryTest aopTest = aopFactory.proxy(AopFactoryTest.class, new AopFactoryTest());

        Assert.assertEquals(aopTest.get(), "hello");
        Assert.assertTrue(atomicBoolean.get());
    }

    @Test
    public void aopBeforeTest()
    {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AopFactory aopFactory = AopFactory.create(binder -> {
            binder.bind("anyMethod")
                    .withPackage("com.github.harbby")
                    .classes(AopFactoryTest.class) // or Set
                    .methodAnnotated(Ignore.class)
                    .whereMethod(method -> method.getName().startsWith("get"))
                    .build()
                    .before(methodInfo -> {
                        atomicBoolean.set(true);
                        Assert.assertEquals(methodInfo.getName(), "get");
                    });
        });

        AopFactoryTest aopTest = aopFactory.proxy(AopFactoryTest.class, new AopFactoryTest());

        Assert.assertEquals(aopTest.get(), "hello");
        Assert.assertTrue(atomicBoolean.get());
    }
}
