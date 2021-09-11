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
package com.github.harbby.gadtry;

import com.github.harbby.gadtry.aop.AopGo;
import com.github.harbby.gadtry.base.Streams;
import com.github.harbby.gadtry.collection.MutableMap;
import com.github.harbby.gadtry.ioc.IocFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GadTryTest
{
    @Test
    public void aopTest()
    {
        List<String> actions = new ArrayList<>();
        IocFactory iocFactory = GadTry.create(binder -> {
            binder.bind(Map.class).byCreator(HashMap::new).withSingle();
            binder.bind(HashSet.class).by(HashSet.class).withSingle();
        }).aop(binder -> {
            binder.bind(Map.class).doBefore(before -> {
                actions.add("before1");
            }).when().size();
            binder.bind(Map.class).doAround(cut -> {
                actions.add("before2");
                int value = (int) cut.proceed() + 1;
                actions.add("before3");
                return value;
            }).whereMethod(method -> method.getName().startsWith("size"));
            binder.bind(Map.class).doBefore(before -> {
                actions.add("before4");
            }).methodName("size");
        }).setConfigurationProperties(MutableMap.of())
                .initialize();

        Map<?, ?> map = iocFactory.getInstance(Map.class);
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(Arrays.asList("before1", "before2", "before4", "before3"), actions);
        Assert.assertSame(iocFactory.getInstance(HashSet.class), iocFactory.getInstance(HashSet.class));
    }

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
}
