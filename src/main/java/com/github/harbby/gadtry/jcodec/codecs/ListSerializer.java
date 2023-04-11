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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ListSerializer<T>
        implements Serializer<List<T>>
{
    @Override
    public void write(Jcodec jcodec, OutputView output, List<T> list)
    {
        if (list == null) {
            output.writeVarInt(0, true);
            return;
        }
        final int size = list.size();
        //write size on the head
        output.writeVarInt(size + 1, true);
        //write key and value
        for (T value : list) {
            jcodec.writeClassAndObject(output, value);
        }
    }

    @Override
    public List<T> read(Jcodec jcodec, InputView input, Class<? extends List<T>> typeClass)
    {
        int size = input.readVarInt(true);
        if (size == 0) {
            return null;
        }
        size--;
        Class<?> listClass = typeClass;
        List<T> list = listClass == LinkedList.class ? new LinkedList<>() : new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(jcodec.readClassAndObject(input));
        }
        return list;
    }

    @Override
    public boolean isNullable()
    {
        return true;
    }

    @Override
    public Comparator<List<T>> comparator()
    {
        throw new UnsupportedOperationException("list obj not support comparator");
    }
}
