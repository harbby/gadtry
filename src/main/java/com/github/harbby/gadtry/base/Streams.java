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
package com.github.harbby.gadtry.base;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

/**
 * java8 range Iterator
 * Consistent with python features
 */
public class Streams
{
    private Streams() {}

    /**
     * between [start,end)
     *
     * @param start start number
     * @param end   end number
     * @param step  default 1
     * @return IntStream
     */
    public static IntStream range(final int start, int end, int step)
    {
        checkState(step != 0, "step must not 0");
        int limit = (end - start + Math.abs(step) - 1) / step;
        if (limit <= 0) {
            return IntStream.empty();
        }

        PrimitiveIterator.OfInt ofInt = new PrimitiveIterator.OfInt()
        {
            private int next = start;
            private int cnt;

            @Override
            public boolean hasNext()
            {
                return cnt < limit;
            }

            @Override
            public int nextInt()
            {
                int tmp = next;
                next += step;
                cnt++;
                return tmp;
            }
        };
        Spliterator.OfInt a1 = Spliterators.spliterator(ofInt, limit, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        return StreamSupport.intStream(a1, false);
    }

    public static IntStream range(final int start, int end)
    {
        return range(start, end, 1);
    }

    public static IntStream range(int end)
    {
        return range(0, end, 1);
    }
}
