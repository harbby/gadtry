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
package com.github.harbby.gadtry.ioc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

public class IocFactoryTest
{
    @Test
    public void IocFactoryCreateTest()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(HashSet.class).withSingle();
            binder.bind(LinkedHashSet.class).noScope();
            binder.bind(String.class, "done");
            binder.bind(Queue.class).byInstance(new ArrayBlockingQueue(100));

            binder.bind(Set.class).by(HashSet.class).withSingle();
            binder.bind(List.class).byCreator(ArrayList::new);  //Single object
            binder.bind(Object.class, new Object());
            binder.bind(Map.class).byCreator(HashMap::new).withSingle();  //Single object
            binder.bind(StringBuilder.class).byCreator(StringBuilderCreator.class).withSingle();  //Single object
        });
        Assertions.assertEquals(9, iocFactory.getAllBeans().getAllBeans().size());
        Assertions.assertTrue(iocFactory.analyze().printShow().size() > 0);
    }

    @Test
    public void DeadDependencyAnalysis()
    {
        IocFactory.create().analyze();
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(DeadDependency1.class).withSingle();
        });
        try {
            iocFactory.analyze();
            Assertions.fail();
        }
        catch (IllegalArgumentException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Find Circular dependency"));
        }
    }

    public static class DeadDependency1
    {
        @Autowired private DeadDependency2 deadDependency2;
        @Autowired private DeadDependency1 deadDependency1;
    }

    public static class DeadDependency2
    {
        @Autowired private DeadDependency3 deadDependency3;
    }

    public static class DeadDependency3
    {
        @Autowired private DeadDependency1 deadDependency1;
    }

    @Test
    public void testNoScopeTestInject()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(TestInject.class).noScope();
        });

        TestInject testInject = iocFactory.getInstance(TestInject.class);
        TestInject testInject2 = iocFactory.getInstance(TestInject.class);
        Assertions.assertTrue(testInject != testInject2);
        Assertions.assertTrue(testInject == testInject.getTest());
    }

    @Test
    public void testNoScopeList()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(List.class).byCreator(ArrayList::new);  //Single object
        });

        Assertions.assertTrue(iocFactory.getInstance(List.class) != iocFactory.getInstance(List.class));
    }

    @Test
    public void testSingleSet()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(Set.class).by(HashSet.class).withSingle();
        });

        Set a1 = iocFactory.getInstance(Set.class);
        Set a2 = iocFactory.getInstance(Set.class);
        Assertions.assertTrue(a1 == a2); // Single object
    }

    @Test
    public void testSingleMap()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(Map.class).byCreator(HashMap::new).withSingle();  //Single object
        });

        Map map1 = iocFactory.getInstance(Map.class);
        Map map2 = iocFactory.getInstance(Map.class);
        Assertions.assertEquals(true, map1 == map2);  //Single object,单例对象
    }

    @Test
    public void iocFactoryGetCreator()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(Set.class).byCreator(HashSet::new).withSingle();
        });

        Supplier a5 = iocFactory.getCreator(StringBuilderCreator.class);
        Supplier a6 = iocFactory.getCreator(StringBuilderCreator.class);
        Assertions.assertTrue(a5 != a6);
        Assertions.assertTrue(a5.get() instanceof StringBuilderCreator);
    }

    @Test
    public void privateCreator()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(StringBuilder.class).byCreator(StringBuilderCreator.class).withSingle();
            binder.bind(Set.class).byCreator(HashSet::new).withSingle();
        });

        StringBuilder instance1 = iocFactory.getInstance(StringBuilder.class);
        StringBuilder instance2 = iocFactory.getInstance(StringBuilder.class);
        Assertions.assertNotNull(instance1);
        Assertions.assertTrue(instance1 == instance2);  // Single

        Set set1 = iocFactory.getInstance(Set.class);
        Set set2 = iocFactory.getInstance(Set.class);
        Assertions.assertNotNull(set1);
        Assertions.assertTrue(set1 == set2);  //Single
    }

    @Test
    public void privateCreatorClassNoScope()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(StringBuilder.class).byCreator(StringBuilderCreator.class);
            binder.bind(Set.class).byCreator(HashSet::new).withSingle();
        });

        StringBuilder instance1 = iocFactory.getInstance(StringBuilder.class);
        StringBuilder instance2 = iocFactory.getInstance(StringBuilder.class);
        Assertions.assertNotNull(instance1);
        Assertions.assertTrue(instance1 != instance2);  //no Single
    }

    @Test
    public void privateCreatorNoScope()
    {
        IocFactory iocFactory = IocFactory.create(binder -> {
            binder.bind(Set.class).byCreator(HashSet::new).noScope();
        });

        Set set1 = iocFactory.getInstance(Set.class);
        Set set2 = iocFactory.getInstance(Set.class);
        Assertions.assertNotNull(set1);
        Assertions.assertTrue(set1 != set2);  //no Single
    }

    private static class StringBuilderCreator
            implements Supplier<StringBuilder>
    {
        @Autowired
        private Set<?> set;

        @Autowired
        public StringBuilderCreator(ArrayList<String> arrayList)
        {
            Assertions.assertNotNull(arrayList);
        }

        @Override
        public StringBuilder get()
        {
            Assertions.assertNotNull(set);
            return new StringBuilder();
        }
    }

    @Test
    public void getNotRegisteredMoreConstructorReturnError()
    {
        Assertions.assertThrows(IllegalStateException.class, ()-> {
            IocFactory iocFactory = IocFactory.create();
            iocFactory.getInstance(PrintStream.class);
        });
    }

    @Test
    public void getNotRegisteredReturnError()
    {
        IocFactory iocFactory = IocFactory.create();
        try {
            iocFactory.getInstance(Supplier.class);
            Assertions.fail();
        }
        catch (IllegalStateException ignored) {
        }
    }
}
