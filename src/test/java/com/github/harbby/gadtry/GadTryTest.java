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

import com.github.harbby.gadtry.aop.AopFactory;
import com.github.harbby.gadtry.base.Streams;
import com.github.harbby.gadtry.collection.MutableMap;
import com.github.harbby.gadtry.ioc.IocFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GadTryTest
{
    @Test
    public void aopTest()
    {
        IocFactory iocFactory = GadTry.create(binder -> {
            binder.bind(Map.class).byCreator(HashMap::new).withSingle();
            binder.bind(HashSet.class).by(HashSet.class).withSingle();
        }).aop(binder -> {
            binder.bind("point1")
                    .withPackage("com.github.harbby")
                    //.subclassOf(Map.class)
                    .classAnnotated()
                    .classes(HashMap.class, HashSet.class)
                    .whereMethod(methodInfo -> methodInfo.getName().startsWith("add"))
                    .build()
                    .before((info) -> {
                        Assert.assertEquals("add", info.getName());
                        System.out.println("before1");
                    })
                    .after(methodInfo -> {
                        Assert.assertTrue(true);
                        System.out.println("after2");
                    });
        }).setConfigurationProperties(MutableMap.of())
                .initialize();

        Set set = iocFactory.getInstance(HashSet.class);
        System.out.println("************");
        set.add("a1");
        System.out.println("************");
        System.out.println(set);
    }

    @Test
    public void testConcurrent10()
    {
        Streams.range(10).parallel().forEach(x -> {
            Set set = AopFactory.proxy(HashSet.class).byInstance(new HashSet<String>())
                    .returnType(void.class, Boolean.class)
                    .after(after -> {
                        String name = after.getName();
                    });
        });
    }
}
