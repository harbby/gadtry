/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.ioc;

import com.github.harbby.gadtry.function.Creator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

public class IocFactoryTest
{
    @Test
    public void testNoScopeTestInject()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(TestInject.class).noScope();
        });

        TestInject testInject = iocFactory.getInstance(TestInject.class);
        TestInject testInject2 = iocFactory.getInstance(TestInject.class);
        Assert.assertTrue(testInject != testInject2);
        Assert.assertTrue(testInject == testInject.getTest());
    }

    @Test
    public void testNoScopeList()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(List.class).byCreator(ArrayList::new);  //Single object
        });

        Assert.assertTrue(iocFactory.getInstance(List.class) != iocFactory.getInstance(List.class));
    }

    @Test
    public void testSingleSet()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(Set.class).by(HashSet.class).withSingle();
        });

        Set a1 = iocFactory.getInstance(Set.class);
        Set a2 = iocFactory.getInstance(Set.class);
        Assert.assertTrue(a1 == a2); // Single object
    }

    @Test
    public void testSingleMap()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(Map.class).byCreator(HashMap::new).withSingle();  //Single object
        });

        Map map1 = iocFactory.getInstance(Map.class);
        Map map2 = iocFactory.getInstance(Map.class);
        Assert.assertEquals(true, map1 == map2);  //Single object,单例对象
    }

    @Test
    public void testCreator()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(Set.class).by(HashSet.class).withSingle();
            binder.bind(HashSet.class).withSingle();
            binder.bind(List.class).byCreator(ArrayList::new);  //Single object
            binder.bind(Object.class, new Object());
            binder.bind(Queue.class).byInstance(new ArrayBlockingQueue(100));
            binder.bind(Map.class).byCreator(HashMap::new).withSingle();  //Single object
        });

        Assert.assertNotNull(iocFactory.getInstance(HashSet.class));

        Supplier a5 = iocFactory.getCreator(HashSet.class);
        Supplier a6 = iocFactory.getCreator(HashSet.class);
        Assert.assertTrue(a5 != a6);
        Assert.assertTrue(a5.get() == a6.get());
    }

    @Test
    public void privateCreator()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(StringBuilder.class).byCreator(TestCreator.class).withSingle();
            binder.bind(Set.class).byCreator(HashSet::new).withSingle();
        });

        StringBuilder instance1 = iocFactory.getInstance(StringBuilder.class);
        StringBuilder instance2 = iocFactory.getInstance(StringBuilder.class);
        Assert.assertNotNull(instance1);
        Assert.assertTrue(instance1 == instance2);  // Single

        Set set1 = iocFactory.getInstance(Set.class);
        Set set2 = iocFactory.getInstance(Set.class);
        Assert.assertNotNull(set1);
        Assert.assertTrue(set1 == set2);  //Single
    }

    @Test
    public void privateCreatorClassNoScope()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(StringBuilder.class).byCreator(TestCreator.class);
            binder.bind(Set.class).byCreator(HashSet::new).withSingle();
        });

        StringBuilder instance1 = iocFactory.getInstance(StringBuilder.class);
        StringBuilder instance2 = iocFactory.getInstance(StringBuilder.class);
        Assert.assertNotNull(instance1);
        Assert.assertTrue(instance1 != instance2);  //no Single
    }

    @Test
    public void privateCreatorNoScope()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(Set.class).byCreator(HashSet::new).noScope();
        });

        Set set1 = iocFactory.getInstance(Set.class);
        Set set2 = iocFactory.getInstance(Set.class);
        Assert.assertNotNull(set1);
        Assert.assertTrue(set1 != set2);  //no Single
    }

    private static class TestCreator
            implements Creator<StringBuilder>
    {
        @Autowired
        private Set set;

        @Override
        public StringBuilder get()
        {
            Assert.assertNotNull(set);
            return new StringBuilder();
        }
    }
}
