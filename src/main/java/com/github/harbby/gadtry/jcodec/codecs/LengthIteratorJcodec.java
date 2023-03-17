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

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public class LengthIteratorJcodec<E>
        implements Jcodec<Iterator<E>>
{
    private final Jcodec<E> eJcodec;

    public LengthIteratorJcodec(Jcodec<E> eJcodec)
    {
        this.eJcodec = requireNonNull(eJcodec, "eEncoder is null");
    }

    @Override
    public void encoder(Iterator<E> value, OutputView output)
    {
        checkState(value instanceof LengthIterator, "only support LengthIterator");
        output.writeLong(((LengthIterator<?>) value).length());
        while (value.hasNext()) {
            eJcodec.encoder(value.next(), output);
        }
    }

    @Override
    public LengthIterator<E> decoder(InputView input)
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
                return eJcodec.decoder(input);
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
