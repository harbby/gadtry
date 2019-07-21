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

import com.github.harbby.gadtry.aop.impl.JavassistProxy;
import com.github.harbby.gadtry.aop.impl.JdkProxy;
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.collection.mutable.MutableList;
import com.github.harbby.gadtry.collection.mutable.MutableSet;
import com.github.harbby.gadtry.collection.tuple.Tuple1;
import com.github.harbby.gadtry.collection.tuple.Tuple4;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AopGoTest
{
    @Test
    public void proxyTestGiveSet()
    {
        Tuple1<String> action = new Tuple1<>(null);
        Tuple4<String, Integer, Double, Character> proxy = AopGo.proxy(Tuple4.of("1", 2, 4.0d, 'a'))
                .aop(binder -> {
                    binder.doBefore(before -> {
                        action.set("before_" + before.getName());
                    }).when().f1();
                    binder.doAfterReturning(returning -> {
                        action.set("returning_" + returning.getName() + "_" + returning.getValue());
                    }).when().f2();
                    binder.doAfter(after -> {
                        action.set("after_" + after.getName() + "_" + after.getValue());
                    }).when().f3();
                    binder.doAround(around -> {
                        Object value = around.proceed();
                        action.set("around_" + around.getName() + "_" + value);
                        return around.proceed();
                    }).when().f4();
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
    }

    @Test
    public void proxyTestGiveHastSetUseJDKProxy()
    {
        Set<String> actions = new HashSet<>();
        List<String> list = AopGo.proxy(JavaTypes.<List<String>>classTag(List.class))
                .byInstance(new ArrayList<>())
                .aop(binder -> {
                    binder.doAround(cut ->
                            (int) cut.proceed() + 1)
                            .whereMethod(method -> method.getName().startsWith("size"));
                    binder.doBefore(before -> {
                        actions.add(before.getName());
                    }).when().isEmpty();
                })
                .build();
        Assert.assertTrue(JdkProxy.isProxyClass(list.getClass()));
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
                    binder.doAround(cut ->
                            (int) cut.proceed() + 1)
                            .whereMethod(method -> method.getName().startsWith("size"));
                    binder.doBefore(before -> {
                        actions.add(before.getName());
                    }).when().isEmpty();
                })
                .build();
        Assert.assertTrue(JavassistProxy.isProxyClass(list.getClass()));
        Assert.assertEquals(list.size(), 1);
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(actions, MutableSet.of("isEmpty"));
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
                })
                .build();
        Assert.assertEquals(set.size(), 1);
        Assert.assertEquals(MutableList.of("before1", "before2", "before4", "before3"), actions);
    }
}
