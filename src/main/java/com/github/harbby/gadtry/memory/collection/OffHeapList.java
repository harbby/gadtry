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

import com.github.harbby.gadtry.memory.MemoryBlock;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class OffHeapList<E>
        extends AbstractList<E>
{
    private final List<MemoryBlock> list;
    private final Function<E, byte[]> serialization;
    private final Function<byte[], E> deserialization;

    public OffHeapList(
            Function<E, byte[]> serialization,
            Function<byte[], E> deserialization
    )
    {
        this(serialization, deserialization, ArrayList::new);
    }

    @SuppressWarnings("unchecked")
    public OffHeapList(
            Function<E, byte[]> serialization,
            Function<byte[], E> deserialization,
            Supplier<List<?>> listSupplier
    )
    {
        this.serialization = requireNonNull(serialization, "serialization is null");
        this.deserialization = requireNonNull(deserialization, "serialization is null");
        this.list = (List<MemoryBlock>) listSupplier.get();
    }

    @Override
    public E get(int index)
    {
        return deserialization.apply(list.get(index).getByteValue());
    }

    @Override
    public void add(int index, E element)
    {
        MemoryBlock block = new MemoryBlock(serialization.apply(element));
        list.add(index, block);
    }

    @Override
    public E set(int index, E element)
    {
        MemoryBlock block = new MemoryBlock(serialization.apply(element));
        try (MemoryBlock oldBlock = list.set(index, block)) {
            return deserialization.apply(oldBlock.getByteValue());
        }
    }

    @Override
    public E remove(int index)
    {
        try (MemoryBlock oldBlock = list.remove(index)) {
            return deserialization.apply(oldBlock.getByteValue());
        }
    }

    @Override
    public int size()
    {
        return list.size();
    }
}
