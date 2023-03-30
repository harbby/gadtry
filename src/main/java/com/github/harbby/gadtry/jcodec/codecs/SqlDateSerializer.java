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

import java.sql.Date;
import java.util.Comparator;

/**
 * @author ivan
 * @date 2021.02.09 10:01:00
 * sql date Serialize
 */
public class SqlDateSerializer
        implements Serializer<Date>
{
    @Override
    public void write(OutputView output, Date value)
    {
        if (value == null) {
            output.writeLong(-1);
        }
        else {
            output.writeLong(value.getTime());
        }
    }

    @Override
    public Date read(InputView input)
    {
        final long l = input.readLong();
        if (l == -1) {
            return null;
        }
        else {
            return new Date(l);
        }
    }

    @Override
    public Comparator<Date> comparator()
    {
        return Date::compareTo;
    }
}
