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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImmutableMapTest
{
    private final Map<String, Integer> dist = new HashMap<String, Integer>()
    {
        {
            this.put("a", 1);
            this.put("c", 3);
            this.put("b", 2);

            this.put("e", 9);
        }
    };

    @Test
    public void baseTest()
    {
        ImmutableMap<String, Integer> immutableMap = ImmutableMap.copy(dist);
        Assert.assertEquals(immutableMap, dist);
    }

    @Test
    public void baseBuilderTest()
    {
        ImmutableMap<String, Integer> immutableMap = ImmutableMap.<String, Integer>builder()
                .put("a", 1)
                .put("c", 3)
                .put("b", 2)
                .putAll(Collections.singletonMap("e", 9))
                .build();
        Assert.assertEquals(immutableMap, dist);
    }

    @Test
    public void redBlackTreeTest()
    {
        int size = 12;
        ImmutableMap.Builder<Integer, String> builder = ImmutableMap.builder();
        List<Integer> keys1 = IntStream.range(0, 9).mapToObj(i -> i * 16 + 4).collect(Collectors.toList());
        List<Integer> keys2 = ImmutableList.of(1, 2, 3);

        keys1.forEach(i -> builder.put(i, "value" + i));
        keys2.forEach(i -> builder.put(i, "value" + i));
        Map<Integer, String> map = builder.build();
        Assert.assertEquals(map.size(), size);
        keys1.forEach(i -> Assert.assertEquals(map.get(i), "value" + i));
        keys2.forEach(i -> Assert.assertEquals(map.get(i), "value" + i));
    }

    private static final String temple = "public static <K, V> ImmutableMap<K, V> of(%s)\n" +
            "{\n" +
            "    @SuppressWarnings(\"unchecked\")\n" +
            "    EntryNode<K, V>[] nodes = new EntryNode[] {%s};\n" +
            "    return copyOfNodes(nodes);\n" +
            "}";

    @Ignore
    @Test
    public void MethodOfCodeGen()
    {
        for (int i = 2; i <= 8; i++) {
            String args = IntStream.range(1, i + 1).mapToObj(index -> {
                return String.format("K k%s, V v%s", index, index);
            }).collect(Collectors.joining(", "));

            String nodes = IntStream.range(1, i + 1).mapToObj(index -> {
                return String.format("new Node<>(k%s, v%s)", index, index);
            }).collect(Collectors.joining(", "));

            String code = String.format(temple, args, nodes);
            System.out.println(code);
        }
    }
}
