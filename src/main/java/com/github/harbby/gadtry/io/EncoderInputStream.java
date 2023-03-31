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
package com.github.harbby.gadtry.io;

import com.github.harbby.gadtry.collection.iterator.CloseIterator;
import com.github.harbby.gadtry.jcodec.InputView;
import com.github.harbby.gadtry.jcodec.Jcodec;
import com.github.harbby.gadtry.jcodec.Serializer;

import java.util.NoSuchElementException;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class EncoderInputStream<E>
        implements CloseIterator<E>
{
    private final Jcodec jcodec;
    private final Class<? extends E> typeClass;
    private final InputView dataInput;
    private final Serializer<E> serializer;
    private final long count;
    private long index = 0;

    public EncoderInputStream(Class<? extends E> typeClass, long count, Serializer<E> serializer, InputView dataInput)
    {
        this(Jcodec.of(), typeClass, count, serializer, dataInput);
    }

    public EncoderInputStream(Jcodec jcodec, Class<? extends E> typeClass, long count, Serializer<E> serializer, InputView dataInput)
    {
        checkState(count >= 0, "row count >= 0");
        this.count = count;
        this.jcodec = requireNonNull(jcodec, "jcodec is null");
        this.typeClass = requireNonNull(typeClass, "typeClass is null");
        this.serializer = requireNonNull(serializer, "encoder is null");
        this.dataInput = requireNonNull(dataInput, "dataInput is null");
    }

    @Override
    public boolean hasNext()
    {
        boolean hasNext = index < count;
        if (!hasNext) {
            this.close();
        }
        return hasNext;
    }

    @Override
    public E next()
    {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
        return serializer.read(jcodec, dataInput, typeClass);
    }

    @Override
    public void close()
    {
        dataInput.close();
    }
}
