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
package com.github.harbby.gadtry.memory.collection;

import com.github.harbby.gadtry.collection.MutableMap;
import com.github.harbby.gadtry.collection.MutableSet;
import com.github.harbby.gadtry.collection.offheap.OffHeapMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OffHeapMapTest
{
    private static final ExecutorService pool = Executors.newFixedThreadPool(5);
    private static final String msg = "this off head value dwah jdawhdaw dhawhdawhdawhjdawjd dhawdhaw djawdjaw";

    @Test
    public void testOffHeapMap()
    {
        final Map<String, String> offHeapMap = new OffHeapMap<>(
                (String str) -> str.getBytes(UTF_8),
                (byte[] bytes) -> new String(bytes, UTF_8),
                ConcurrentHashMap::new
        );
        offHeapMap.put("a1", msg);
        Assert.assertEquals(offHeapMap.get("a1"), msg);
        Assert.assertNull(offHeapMap.get("????"));
        Assert.assertNull(offHeapMap.remove("????"));
        offHeapMap.clear();
    }

    @Test
    public void putAllTest()
    {
        final Map<String, Integer> offHeapMap = new OffHeapMap<>(
                (Integer str) -> String.valueOf(str).getBytes(UTF_8),
                (byte[] bytes) -> Integer.valueOf(new String(bytes, UTF_8)),
                IdentityHashMap::new
        );
        Map<String, Integer> integerMap = MutableMap.of("a1", 123);
        offHeapMap.putAll(integerMap);

        Assert.assertEquals(offHeapMap, integerMap);
    }

    @Test
    public void isEmptyTestReturnFalse()
    {
        final Map<String, Integer> offHeapMap = new OffHeapMap<>(
                (Integer str) -> String.valueOf(str).getBytes(UTF_8),
                (byte[] bytes) -> Integer.valueOf(new String(bytes, UTF_8)));
        offHeapMap.put("a1", 123);

        Assert.assertFalse(offHeapMap.isEmpty());

        Assert.assertEquals(offHeapMap.size(), 1);
    }

    @Test
    public void containsTest()
    {
        final Map<String, Integer> offHeapMap = new OffHeapMap<>(
                (Integer str) -> String.valueOf(str).getBytes(UTF_8),
                (byte[] bytes) -> Integer.valueOf(new String(bytes, UTF_8)));
        offHeapMap.put("a1", 123);

        Assert.assertTrue(offHeapMap.containsKey("a1"));

        try {
            offHeapMap.containsValue(123);
            Assert.fail();
        }
        catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void valuesTest()
    {
        final Map<String, Integer> offHeapMap = new OffHeapMap<>(
                (Integer str) -> String.valueOf(str).getBytes(UTF_8),
                (byte[] bytes) -> Integer.valueOf(new String(bytes, UTF_8))
        );
        offHeapMap.put("a1", 123);
        offHeapMap.put("a1", 456);
        Assert.assertEquals(offHeapMap.values(), Arrays.asList(456));
        Assert.assertEquals(offHeapMap.keySet(), MutableSet.of("a1"));
    }
}
