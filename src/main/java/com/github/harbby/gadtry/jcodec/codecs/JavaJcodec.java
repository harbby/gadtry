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

import com.github.harbby.gadtry.base.Serializables;
import com.github.harbby.gadtry.base.Throwables;
import com.github.harbby.gadtry.jcodec.HashCodeComparator;
import com.github.harbby.gadtry.jcodec.InputView;
import com.github.harbby.gadtry.jcodec.Jcodec;
import com.github.harbby.gadtry.jcodec.OutputView;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Comparator;

public class JavaJcodec<E extends Serializable>
        implements Jcodec<E>
{
    @Override
    public boolean isNullable()
    {
        return true;
    }

    @Override
    public void encoder(E value, OutputView output)
    {
        try {
            OutputStream outputStream = (OutputStream) output;
            Serializables.serialize(outputStream, value);
        }
        catch (Exception e) {
            Throwables.throwThrowable(e);
        }
    }

    @Override
    public E decoder(InputView input)
    {
        try {
            InputStream inputStream = (InputStream) input;
            return Serializables.byteToObject(inputStream);
        }
        catch (Exception e) {
            throw Throwables.throwThrowable(e);
        }
    }

    @Override
    public Comparator<E> comparator()
    {
        return new HashCodeComparator<>();
    }
}
