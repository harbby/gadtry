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

import com.github.harbby.gadtry.jcodec.InputView;
import com.github.harbby.gadtry.jcodec.Jcodec;
import com.github.harbby.gadtry.jcodec.OutputView;
import com.github.harbby.gadtry.jcodec.Serializer;

import java.util.Comparator;

import static java.util.Objects.requireNonNull;

public class TypeArraySerializer<E>
        implements Serializer<E[]>
{
    private final Serializer<E> serializer;
    private final Class<? extends E> classTag;

    public TypeArraySerializer(Serializer<E> serializer, Class<? extends E> classTag)
    {
        this.serializer = requireNonNull(serializer, "serializer is null");
        this.classTag = requireNonNull(classTag, "classTag is null");
    }

    @Override
    public boolean isNullable()
    {
        return true;
    }

    @Override
    public void write(Jcodec jcodec, OutputView output, E[] values)
    {
        if (values == null) {
            output.writeVarInt(0, true);
            return;
        }
        output.writeVarInt(values.length + 1, true);
        for (E e : values) {
            serializer.write(jcodec, output, e);
        }
    }

    private transient E[] emptry;

    @SuppressWarnings("unchecked")
    @Override
    public E[] read(Jcodec jcodec, InputView input, Class<? extends E[]> typeClass)
    {
        int len = input.readVarInt(true);
        if (len == 0) {
            return null;
        }
        if (len == 1) {
            if (emptry == null) {
                emptry = (E[]) java.lang.reflect.Array.newInstance(classTag, 0);
            }
            return emptry;
        }
        len--;
        E[] values = (E[]) java.lang.reflect.Array.newInstance(classTag, len);
        for (int i = 0; i < len; i++) {
            values[i] = serializer.read(jcodec, input, classTag);
        }
        return values;
    }

    @Override
    public Comparator<E[]> comparator()
    {
        return comparator(serializer.comparator());
    }

    public static <E> Comparator<E[]> comparator(Comparator<E> comparator)
    {
        return (arr1, arr2) -> {
            int len1 = arr1.length;
            int len2 = arr2.length;
            int lim = Math.min(len1, len2);

            int k = 0;
            while (k < lim) {
                E c1 = arr1[k];
                E c2 = arr2[k];
                if (c1 != c2) {
                    return comparator.compare(c1, c2);
                }
                k++;
            }
            return Integer.compare(len1, len2);
        };
    }
}
