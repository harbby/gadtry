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
package com.github.harbby.gadtry.collection.mutable;

import com.github.harbby.gadtry.collection.MutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MutableMapTest
{
    @Test
    public void copyCreateTest()
    {
        Map<String, Integer> map = new HashMap<>();
        map.put("a1", 1);

        Map<String, Integer> create = MutableMap.copy(map);
        Assertions.assertEquals(create, map);
    }

    @Test
    public void builderCreateTest()
    {
        Map<String, Integer> map = new HashMap<>();
        map.put("a1", 1);
        map.put("a2", 2);

        Map<String, Integer> create = MutableMap.<String, Integer>builder()
                .put("a3", 3)
                .putAll(map)
                .build();
        Map<String, Integer> check = new HashMap<>(map);
        check.put("a3", 3);

        Assertions.assertEquals(create, check);
    }

    @Test
    public void ofCreateTest()
    {
        Assertions.assertEquals(MutableMap.of(), Collections.emptyMap());
    }

    @Test
    public void of1CreateTest()
    {
        Map<String, Integer> check = new HashMap<>();
        check.put("a1", 1);

        Map<String, Integer> create = MutableMap.of("a1", 1);
        Assertions.assertEquals(create, check);
    }

    @Test
    public void of2CreateTest()
    {
        Map<String, Integer> check = new HashMap<>();
        check.put("a1", 1);
        check.put("a2", 2);

        Map<String, Integer> create = MutableMap.of("a1", 1, "a2", 2);
        Assertions.assertEquals(create, check);
    }

    @Test
    public void of3CreateTest()
    {
        Map<String, Integer> check = new HashMap<>();
        check.put("a1", 1);
        check.put("a2", 2);
        check.put("a3", 3);

        Map<String, Integer> create = MutableMap.of("a1", 1, "a2", 2, "a3", 3);
        Assertions.assertEquals(create, check);
    }

    @Test
    public void of4CreateTest()
    {
        Map<String, Integer> check = new HashMap<>();
        check.put("a1", 1);
        check.put("a2", 2);
        check.put("a3", 3);
        check.put("a4", 4);

        Map<String, Integer> create = MutableMap.of("a1", 1, "a2", 2, "a3", 3, "a4", 4);
        Assertions.assertEquals(create, check);
    }

    @Test
    public void of5CreateTest()
    {
        Map<String, Integer> check = new HashMap<>();
        check.put("a1", 1);
        check.put("a2", 2);
        check.put("a3", 3);
        check.put("a4", 4);
        check.put("a5", 5);

        Map<String, Integer> create = MutableMap.of("a1", 1, "a2", 2, "a3", 3, "a4", 4, "a5", 5);
        Assertions.assertEquals(create, check);
    }

    @Test
    public void of6CreateTest()
    {
        Map<String, Integer> check = new HashMap<>();
        check.put("a1", 1);
        check.put("a2", 2);
        check.put("a3", 3);
        check.put("a4", 4);
        check.put("a5", 5);
        check.put("a6", 6);

        Map<String, Integer> create = MutableMap.of("a1", 1,
                "a2", 2,
                "a3", 3,
                "a4", 4,
                "a5", 5,
                "a6", 6);
        Assertions.assertEquals(create, check);
    }

    @Test
    public void of7CreateTest()
    {
        Map<String, Integer> check = new HashMap<>();
        check.put("a1", 1);
        check.put("a2", 2);
        check.put("a3", 3);
        check.put("a4", 4);
        check.put("a5", 5);
        check.put("a6", 6);
        check.put("a7", 7);

        Map<String, Integer> create = MutableMap.of("a1", 1,
                "a2", 2,
                "a3", 3,
                "a4", 4,
                "a5", 5,
                "a6", 6,
                "a7", 7);
        Assertions.assertEquals(create, check);
    }

    @Test
    public void of8CreateTest()
    {
        Map<String, Integer> check = new HashMap<>();
        check.put("a1", 1);
        check.put("a2", 2);
        check.put("a3", 3);
        check.put("a4", 4);
        check.put("a5", 5);
        check.put("a6", 6);
        check.put("a7", 7);
        check.put("a8", 8);

        Map<String, Integer> create = MutableMap.of("a1", 1,
                "a2", 2,
                "a3", 3,
                "a4", 4,
                "a5", 5,
                "a6", 6,
                "a7", 7,
                "a8", 8);
        Assertions.assertEquals(create, check);
    }
}
