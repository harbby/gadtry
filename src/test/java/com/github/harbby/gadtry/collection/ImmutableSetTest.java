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
package com.github.harbby.gadtry.collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.IntStream;

public class ImmutableSetTest
{
    @Test
    public void ofTest()
    {
        Arrays.stream(ImmutableSet.class.getMethods())
                .filter(x -> "of".equals(x.getName()))
                .forEach(method -> {
                    int c = method.getParameterCount();
                    try {
                        ImmutableSet<Integer> set;
                        if (c > 0 && method.getParameterTypes()[c - 1].isArray()) {
                            Object[] objects = new Object[c];
                            for (int i = 0; i < c - 1; i++) {
                                objects[i] = i;
                            }
                            objects[c - 1] = new Integer[] {c - 1};
                            set = (ImmutableSet<Integer>) method.invoke(null, objects);
                        }
                        else {
                            Integer[] intArr = IntStream.range(0, c).mapToObj(x -> x).toArray(Integer[]::new);
                            set = (ImmutableSet<Integer>) method.invoke(null, (Object[]) intArr);
                        }
                        Assertions.assertEquals(set.size(), c);
                        Assertions.assertEquals(set, new HashSet<>(Arrays.asList(IntStream.range(0, c).mapToObj(x -> x).toArray(Integer[]::new))));
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        Assertions.fail();
                    }
                });
    }

    @Test
    public void emptyTest()
    {
        Assertions.assertEquals(ImmutableSet.of(), new HashSet<>());
    }

    @Test
    public void buildTest()
    {
        ImmutableSet<Integer> immutableSet = ImmutableSet.<Integer>builder()
                .add(1)
                .addAll(Arrays.asList(1, 1, 2, 2, 2))
                .build();
        Assertions.assertEquals(immutableSet, new HashSet<>(Arrays.asList(1, 2)));
    }
}
