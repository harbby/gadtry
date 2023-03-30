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
import com.github.harbby.gadtry.jcodec.OutputView;
import com.github.harbby.gadtry.jcodec.Serializer;

import java.util.Comparator;

/**
 * @author ivan
 * @date 2021.02.09 10:01:00
 * byte Serialize
 */
public class ByteSerializer
        implements Serializer<Byte>
{
    @Override
    public void write(OutputView output, Byte value)
    {
        output.writeByte(value);
    }

    @Override
    public Byte read(InputView input)
    {
        return input.readByte();
    }

    @Override
    public Comparator<Byte> comparator()
    {
        return Byte::compare;
    }
}
