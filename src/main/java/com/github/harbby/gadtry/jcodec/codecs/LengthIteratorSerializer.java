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
package com.github.harbby.gadtry.jcodec.codecs;

import com.github.harbby.gadtry.collection.iterator.LengthIterator;
import com.github.harbby.gadtry.jcodec.InputView;
import com.github.harbby.gadtry.jcodec.Jcodec;
import com.github.harbby.gadtry.jcodec.OutputView;
import com.github.harbby.gadtry.jcodec.Serializer;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class LengthIteratorSerializer<E>
        implements Serializer<Iterator<E>>
{
    private final Serializer<E> eSerializer;
    private final Class<? extends E> typeClass;

    public LengthIteratorSerializer(Class<? extends E> typeClass, Serializer<E> eSerializer)
    {
        this.typeClass = typeClass;
        this.eSerializer = requireNonNull(eSerializer, "eEncoder is null");
    }

    @Override
    public void write(Jcodec jcodec, OutputView output, Iterator<E> value)
    {
        checkState(value instanceof LengthIterator, "only support LengthIterator");
        output.writeLong(((LengthIterator<?>) value).length());
        while (value.hasNext()) {
            eSerializer.write(jcodec, output, value.next());
        }
    }

    @Override
    public Iterator<E> read(Jcodec jcodec, InputView input, Class<? extends Iterator<E>> itClass)
    {
        final int length = input.readInt();
        return new LengthIterator<E>()
        {
            private int index = 0;

            @Override
            public long length()
            {
                return length;
            }

            @Override
            public boolean hasNext()
            {
                return index < length;
            }

            @Override
            public E next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                index++;
                return eSerializer.read(jcodec, input, typeClass);
            }

            @Override
            public long size()
            {
                return length - index;
            }
        };
    }

    @Override
    public Comparator<Iterator<E>> comparator()
    {
        throw new UnsupportedOperationException();
    }
}
