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

public class EnumSerializer
        implements Serializer<Enum>
{
    private final Enum<?>[] enums;

    private EnumSerializer(Class<? extends Enum> enumClass)
    {
        Enum<?>[] enums = enumClass.getEnumConstants();
        this.enums = requireNonNull(enums, enumClass + " not is Enum");
    }

    @Override
    public boolean isNullable()
    {
        return true;
    }

    @Override
    public void write(Jcodec jcodec, OutputView output, Enum value)
    {
        if (value == null) {
            output.writeVarInt(0, true);
        }
        else {
            output.writeVarInt(value.ordinal() + 1, true);
        }
    }

    @Override
    public Enum<?> read(Jcodec jcodec, InputView input, Class<? extends Enum> typeClass)
    {
        int ordinal = input.readVarInt(true);
        if (ordinal == 0) {
            return null;
        }
        else {
            return enums[ordinal - 1];
        }
    }

    @Override
    public Comparator<Enum> comparator()
    {
        return Enum::compareTo;
    }
}
