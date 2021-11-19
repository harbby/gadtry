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
package com.github.harbby.gadtry.cache;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LRUHashCacheTest
{
    public static void main(String[] args)
            throws InterruptedException
    {
        Cache<String, String> cache = new LRUHashCache<>(
                10,
                (k, v, cause) -> {
                    switch (cause) {
                        case OVERFLOW:
                            System.out.println(String.format("clear data for OVERFLOW k: %s, v: %s", k, v));
                            break;
                        case TIME_OUT:
                            System.out.println(String.format("clear data for TIME_OUT k: %s, v: %s", k, v));
                            break;
                        default:
                    }
                },
                TimeUnit.SECONDS.toMillis(5));

        //show OVERFLOW
        for (int i = 1; i <= 15; i++) {
            cache.put("key" + i, "value" + i);
        }

        //show TIME_OUT
        TimeUnit.SECONDS.sleep(5);
        cache.put("key16", "value16");
        for (int i = 1; i <= 15; i++) {
            assertNull(cache.getIfPresent("key" + i));
        }
        cache.put("key17", "value17");
        assertEquals("value16", cache.getIfPresent("key16"));
        assertEquals("value17", cache.getIfPresent("key17"));
        assertEquals(2, cache.size());
    }

    @Test
    public void overFlowCallBackTest()
    {
        List<String> evictList = new ArrayList<>();
        LRUHashCache<String, String> cache = new LRUHashCache<>(
                10,
                (k, v, cause) -> evictList.add(String.format("%s:%s cause:%s", k, v, cause)),
                TimeUnit.SECONDS.toMillis(60));
        for (int i = 1; i <= 10; i++) {
            cache.put("key" + i, "value" + i);
        }
        Assert.assertTrue(evictList.isEmpty());

        String value = cache.get("key11", () -> "value11");
        Assert.assertEquals(value, "value11");
        Assert.assertEquals(evictList, Collections.singletonList("key1:value1 cause:OVERFLOW"));

        Assert.assertEquals(cache.put("key2", "value22"), "value2");
        assertNull(cache.put("key12", "value12"));
        Assert.assertEquals(evictList, Arrays.asList("key1:value1 cause:OVERFLOW", "key3:value3 cause:OVERFLOW"));
    }

    @Test
    public void timeOutCallbackTest()
            throws InterruptedException
    {
        List<String> evictList = new ArrayList<>();
        Cache<String, String> cache = new LRUHashCache<>(
                10,
                (k, v, cause) -> evictList.add(String.format("%s:%s cause:%s", k, v, cause)),
                TimeUnit.MILLISECONDS.toMillis(300));
        cache.put("k1", "v1");
        cache.get("k2", () -> "v2");
        Assert.assertEquals(cache.size(), 2);
        TimeUnit.MILLISECONDS.sleep(301);

        Assert.assertTrue(evictList.isEmpty()); //check lazy clear
        assertNull(cache.getIfPresent("k1"));
        Assert.assertEquals(evictList, Collections.singletonList("k1:v1 cause:TIME_OUT")); //check lazy clear
        assertNull(cache.getIfPresent("k2"));
        Assert.assertEquals(evictList, Arrays.asList("k1:v1 cause:TIME_OUT", "k2:v2 cause:TIME_OUT"));
    }

    @Test
    public void timeOutGetAllTest()
            throws InterruptedException
    {
        List<String> evictList = new ArrayList<>();
        Cache<String, String> cache = new LRUHashCache<>(
                10,
                (k, v, cause) -> evictList.add(String.format("%s:%s cause:%s", k, v, cause)),
                TimeUnit.MILLISECONDS.toMillis(300));
        cache.put("k1", "v1");
        cache.get("k2", () -> "v2");
        Assert.assertEquals(cache.size(), 2);
        TimeUnit.MILLISECONDS.sleep(301);

        Assert.assertTrue(cache.getAllPresent().isEmpty());
    }
}
