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

public class VarIntSerializer
        implements Serializer<Integer>
{
    /**
     * value:    1 | 2  3  4  5  6  7  8  9  10
     * mapping: -1 | 1 -2  2 -3  3 -4  4 -5  5
     */
    private final boolean optimizePositive;

    public VarIntSerializer(boolean optimizeNegativeNumber)
    {
        this.optimizePositive = optimizeNegativeNumber;
    }

    public VarIntSerializer()
    {
        this(true);
    }

    @Override
    public void write(Jcodec jcodec, OutputView output, Integer value)
    {
        output.writeVarInt(value, optimizePositive);
    }

    @Override
    public Integer read(Jcodec jcodec, InputView input, Class<? extends Integer> typeClass)
    {
        return input.readVarInt(optimizePositive);
    }

    @Override
    public Comparator<Integer> comparator()
    {
        return Integer::compare;
    }
}
