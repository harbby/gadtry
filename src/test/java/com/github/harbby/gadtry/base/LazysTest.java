/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.base;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazysTest
{
    @Test
    public void goLazyOneRunTest()
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        final Supplier<List<String>> lazy = Lazys.goLazy(() -> {
            atomicInteger.getAndIncrement(); //i++
            return new ArrayList<>();
        });

        Assert.assertTrue(lazy.get() == lazy.get());
        Assert.assertEquals(1, atomicInteger.get());
    }

    @Test
    public void goLazySerializableTest()
            throws IOException, ClassNotFoundException
    {
        final Supplier<List<String>> lazy = Lazys.goLazy(() -> {
            return new ArrayList<>();
        });

        byte[] bytes = Serializables.serialize((Serializable) lazy);
        final Supplier<List<String>> serializableLazy = Serializables.byteToObject(bytes);
        Assert.assertEquals(0, serializableLazy.get().size());
    }

    @Test
    public void goLazyArgsTest()
            throws Exception
    {
        final Function<String, List<String>> lazy = Lazys.goLazy((Serializable & Function<String, List<String>>) (init) -> {
            return Arrays.asList(init);
        });

        Assert.assertEquals(Arrays.asList("init"), lazy.apply("init"));
        Assert.assertTrue(lazy.apply("a1") == lazy.apply("a2"));
        Assert.assertTrue(Serializables.serialize((Serializable) lazy).length > 0);
    }

    @Test
    public void goLazyArgsNoReturnTest()
            throws Exception
    {
        final Function<String, List<String>> lazy = Lazys.goLazy((Serializable & Function<String, List<String>>) (init) -> {
            return Arrays.asList(init);
        });

        Assert.assertEquals(Arrays.asList("init"), lazy.apply("init"));
        Assert.assertTrue(lazy.apply("a1") == lazy.apply("a2"));
        Assert.assertTrue(Serializables.serialize((Serializable) lazy).length > 0);
    }
}
