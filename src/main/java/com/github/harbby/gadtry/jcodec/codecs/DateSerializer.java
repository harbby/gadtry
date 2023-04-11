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
import com.github.harbby.gadtry.jcodec.JcodecException;
import com.github.harbby.gadtry.jcodec.OutputView;
import com.github.harbby.gadtry.jcodec.Serializer;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;

import static com.github.harbby.gadtry.StaticAssert.DEBUG;

public class DateSerializer
        implements Serializer<Date>
{
    @Override
    public boolean isNullable()
    {
        return true;
    }

    @Override
    public void write(Jcodec jcodec, OutputView output, Date value)
    {
        if (value == null) {
            output.writeVarLong(0, true);
        }
        else {
            assert !DEBUG || value.getTime() >= 0 : "time must > -1";
            output.writeVarLong(value.getTime() + 1, true);
        }
    }

    @Override
    public Date read(Jcodec jcodec, InputView input, Class<? extends Date> typeClass)
    {
        long time = input.readVarLong(true);
        if (time == 0) {
            return null;
        }
        time--;
        if (typeClass == Date.class) {
            return new Date(time);
        }
        else if (typeClass == Timestamp.class) {
            return new Timestamp(time);
        }
        else if (typeClass == Time.class) {
            return new Time(time);
        }
        else if (typeClass == java.sql.Date.class) {
            return new java.sql.Date(time);
        }
        throw new JcodecException("unknown date type " + typeClass);
    }

    @Override
    public Comparator<Date> comparator()
    {
        return Date::compareTo;
    }
}
