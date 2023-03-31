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

public class BooleanSerializer
        implements Serializer<Boolean>
{
    @Override
    public void write(Jcodec jcodec, OutputView output, Boolean value)
    {
        output.writeBoolean(value);
    }

    @Override
    public Boolean read(Jcodec jcodec, InputView input, Class<? extends Boolean> typeClass)
    {
        return input.readBoolean();
    }

    @Override
    public Comparator<Boolean> comparator()
    {
        return Boolean::compare;
    }
}
