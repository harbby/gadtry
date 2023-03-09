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

import java.util.Comparator;

import static java.util.Objects.requireNonNull;

public class EnumJcodec
        implements Jcodec<Enum>
{
    private final Enum<?>[] enums;

    private EnumJcodec(Class<? extends Enum> enumClass)
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
    public void encoder(Enum value, OutputView output)
    {
        if (value == null) {
            output.writeVarInt(0, false);
        }
        else {
            output.writeVarInt(value.ordinal() + 1, false);
        }
    }

    @Override
    public Enum<?> decoder(InputView input)
    {
        int ordinal = input.readVarInt(false);
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
