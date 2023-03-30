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
package com.github.harbby.gadtry.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazysTest
{
    @Test
    public void goLazyOneRunTest()
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        final Supplier<List<String>> lazy = Lazys.of(() -> {
            atomicInteger.getAndIncrement(); //i++
            return new ArrayList<>();
        });

        Assertions.assertTrue(lazy.get() == lazy.get());
        Assertions.assertEquals(1, atomicInteger.get());
        Assertions.assertTrue(lazy.toString().contains("Lazys.of"));
    }

    @Test
    public void getLazyGiveLazySupplier()
    {
        Lazys.LazySupplier<String> lazySupplier = new Lazys.LazySupplier<>(() -> "done");
        Assertions.assertEquals(Lazys.of(lazySupplier).get(), "done");
    }

    @Test
    public void goLazySerializableTest()
            throws IOException, ClassNotFoundException
    {
        final Supplier<List<String>> lazy = Lazys.of((Supplier<List<String>> & Serializable) () -> {
            return Arrays.asList("1", "2", "3");
        });

        byte[] bytes = Serializables.serialize((Serializable) lazy);
        final Supplier<List<String>> serializableLazy = Serializables.byteToObject(bytes);

        Assertions.assertTrue(serializableLazy != lazy);
        Assertions.assertEquals(Arrays.asList("1", "2", "3"), serializableLazy.get());
    }

    @Test
    public void goLazy2SerializableTest()
            throws IOException, ClassNotFoundException
    {
        final Function<String, List<String>> lazy = Lazys.of((Function<String, List<String>> & Serializable) init -> Arrays.asList(init));

        byte[] bytes = Serializables.serialize((Serializable) lazy);
        final Function<String, List<String>> serializableLazy = Serializables.byteToObject(bytes);

        Assertions.assertEquals(Arrays.asList("init"), serializableLazy.apply("init"));
        Assertions.assertEquals(serializableLazy.apply("a1"), serializableLazy.apply("a2"));
    }

    @Test
    public void goLazyArgsTest()
            throws IOException
    {
        final Function<String, List<String>> lazy = Lazys.of((Function<String, List<String>> & Serializable) init -> Arrays.asList(init));

        Assertions.assertEquals(Arrays.asList("init"), lazy.apply("init"));
        Assertions.assertTrue(lazy.apply("a1") == lazy.apply("a2"));
        Assertions.assertTrue(Serializables.serialize((Serializable) lazy).length > 0);
        Assertions.assertTrue(Lazys.of(lazy).apply("a3") == lazy.apply("a5"));
        Assertions.assertTrue(lazy.toString().contains("Lazys.of"));
    }

    @Test
    public void forkFunctionCreateTest()
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        final Function<String, List<String>> lazy = Lazys.of(init -> {
            atomicInteger.getAndIncrement();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            }
            catch (InterruptedException ignored) {
            }
            return Arrays.asList(init);
        });
        Streams.range(1, 5).parallel().forEach(x -> lazy.apply("init"));
        Assertions.assertEquals(atomicInteger.get(), 1);
    }

    @Test
    public void forkCreatorCreateTest()
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        final Supplier<List<String>> lazy = Lazys.of(() -> {
            atomicInteger.getAndIncrement();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            catch (InterruptedException ignored) {
            }
            return Arrays.asList("init");
        });
        Streams.range(1, 5).parallel().forEach(x -> lazy.get());
        Assertions.assertEquals(atomicInteger.get(), 1);
    }
}
