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

import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.base.Streams;
import com.github.harbby.gadtry.collection.ImmutableList;
import com.github.harbby.gadtry.collection.MutableList;
import com.github.harbby.gadtry.collection.MutableSet;
import com.github.harbby.gadtry.collection.tuple.Tuple1;
import com.github.harbby.gadtry.collection.tuple.Tuple4;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.harbby.gadtry.aop.mockgo.MockGoArgument.anyInt;

public class AopGoTest
{
    @Test
    public void testConcurrent10()
    {
        Streams.range(10).parallel().forEach(x -> {
            Set set = AopGo.proxy(HashSet.class).byInstance(new HashSet<String>())
                    .aop(binder -> {
                        binder.doAfter(after -> {
                            String name = after.getName();
                            System.out.println(name);
                        }).returnType(void.class, Boolean.class);
                    }).build();
            set.clear();
        });
    }

    @Test
    public void proxyTestGiveTuple4()
    {
        Tuple1<String> action = new Tuple1<>(null);
        Tuple4<String, Integer, Double, Character> proxy = AopGo.proxy(Tuple4.of("1", 2, 4.0d, 'a'))
                .aop(binder -> {
                    binder.doBefore(before -> {
                        Object[] args = before.getArgs();
                        Assert.assertTrue(args == null || args.length == 0);
                        action.set("before_" + before.getName());
                    }).when().f1();
                    binder.doAfterReturning(returning -> {
                        Object[] args = returning.getArgs();
                        Assert.assertTrue(args == null || args.length == 0);
                        action.set("returning_" + returning.getName() + "_" + returning.getValue());
                    }).when().f2();
                    binder.doAfter(after -> {
                        Object[] args = after.getArgs();
                        Assert.assertTrue(args == null || args.length == 0);
                        Assert.assertTrue(after.isSuccess() && after.getThrowable() == null);
                        action.set("after_" + after.getName() + "_" + after.getValue());
                    }).when().f3();
                    binder.doAround(around -> {
                        Object[] args = around.getArgs();
                        Assert.assertTrue(args == null || args.length == 0);
                        Object value = around.proceed();
                        action.set("around_" + around.getName() + "_" + value);
                        return around.proceed();
                    }).when().f4();
                    binder.doAfterThrowing(throwing -> {
                        Assert.assertArrayEquals(throwing.getArgs(), new Object[] {0});
                        Assert.assertEquals(throwing.<Integer>getArgument(0).intValue(), 0);
                        action.set("afterThrowing_" + throwing.getName() + "_" + throwing.getThrowable().getMessage());
                    }).when().getField(anyInt());
                })
                .build();
        proxy.f1();
        Assert.assertEquals(action.get(), "before_f1");
        proxy.f2();
        Assert.assertEquals(action.get(), "returning_f2_2");
        proxy.f3();
        Assert.assertEquals(action.get(), "after_f3_4.0");
        proxy.f4();
        Assert.assertEquals(action.get(), "around_f4_a");

        Assert.assertEquals(proxy.getField(1), "1");
        try {
            proxy.getField(0);
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertEquals(action.get(), "afterThrowing_getField_0");
        }
    }

    @Test
    public void aopByAfterIsSuccessTestGive999ReturnFalse()
    {
        List<Boolean> checkList = new ArrayList<>();
        Tuple1<String> proxy = AopGo.proxy(Tuple1.of("1"))
                .aop(binder -> {
                    binder.doAfter(after -> {
                        checkList.add(after.isSuccess());
                    }).when().getField(anyInt());
                })
                .build();
        try {
            proxy.getField(999);
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IndexOutOfBoundsException);
        }
        Assert.assertEquals(checkList, ImmutableList.of(false));
    }

    @Test
    public void proxyTestGiveHastSetUseJDKProxy()
    {
        Set<String> actions = new HashSet<>();
        List<String> list = AopGo.proxy(JavaTypes.<List<String>>classTag(List.class))
                .byInstance(new ArrayList<>())
                .aop(binder -> {
                    binder.doAround(cut -> {
                        return (int) cut.proceed() + 1;
                    }).whereMethod(method -> method.getName().startsWith("size"));
                    binder.doBefore(before -> {
                        actions.add(before.getName());
                    }).when().isEmpty();
                })
                .build();
        Assert.assertEquals(list.size(), 1);
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(actions, MutableSet.of("isEmpty"));
    }

    @Test
    public void proxyTestGiveHastSetUseJavassistProxy()
    {
        Set<String> actions = new HashSet<>();
        List<String> list = AopGo.proxy(new ArrayList<String>())
                .aop(binder -> {
                    binder.doAround(cut -> {
                        return (int) cut.proceed() + 1;
                    }).whereMethod(method -> method.getName().startsWith("size"));
                    binder.doBefore(before -> {
                        actions.add(before.getName());
                    }).when().isEmpty();
                })
                .build();
        Assert.assertEquals(list.size(), 1);
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(actions, MutableSet.of("isEmpty"));
    }

    @Test
    public void aopGoUseReturnTypeSelector()
    {
        Set<String> actions = new HashSet<>();
        List<String> list = AopGo
                .proxy(JavaTypes.<List<String>>classTag(ImmutableList.class))
                .byInstance(ImmutableList.<String>of())
                .aop(binder -> {
                    binder.doBefore(before -> {
                        actions.add(before.getName());
                    }).returnType(String.class, int.class);
                }).build();

        Assert.assertEquals(list.size(), 0);
        Assert.assertEquals(list.toString(), "[]");
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(actions, MutableSet.of("size", "toString"));
    }

    @Test
    public void aopGoUseReturnTypeSelector2()
    {
        Set<String> actions = new HashSet<>();
        List<String> list = AopGo
                .proxy(ImmutableList.<String>of())
                .aop(binder -> {
                    binder.doBefore(before -> {
                        before.getMethod().setAccessible(true);
                        actions.add(before.getName());
                    }).returnType(String.class, int.class);
                }).build();

        Assert.assertEquals(list.size(), 0);
        Assert.assertEquals(list.toString(), "[]");
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(actions, MutableSet.of("size", "toString"));
    }

    @Test
    public void aopGoUseAnnotatedSelector()
    {
        Set<String> actions = new HashSet<>();
        AnnotatedClass proxy = AopGo.proxy(new AnnotatedClass())
                .aop(binder -> {
                    binder.doBefore(before -> {
                        actions.add(before.getName());
                    }).annotated(Deprecated.class, Deprecated.class);
                }).build();
        proxy.a1();
        proxy.a2();
        Assert.assertEquals(actions, MutableSet.of("a2"));
    }

    public static class AnnotatedClass
    {
        public void a1() {}

        @Deprecated
        public void a2() {}
    }

    /**
     * 同心圆
     */
    @Test
    public void moreAspectTest()
    {
        List<String> actions = new ArrayList<>();
        Set<String> set = AopGo.proxy(JavaTypes.<Set<String>>classTag(Set.class))
                .byInstance(new HashSet<>())
                .aop(binder -> {
                    binder.doBefore(before -> {
                        actions.add("before1");
                    }).when().size();
                    binder.doAround(cut -> {
                        actions.add("before2");
                        int value = (int) cut.proceed() + 1;
                        actions.add("before3");
                        return value;
                    }).whereMethod(method -> method.getName().startsWith("size"));
                    binder.doBefore(before -> {
                        actions.add("before4");
                    }).when().size();
                }).build();
        Assert.assertEquals(set.size(), 1);
        Assert.assertEquals(MutableList.of("before1", "before2", "before4", "before3"), actions);
    }
}
