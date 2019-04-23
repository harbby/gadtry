/*
 * Copyright (C) 2018 The Harbby Authors
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class Iterators
{
    private Iterators() {}

    public static <E> Iterator<E> of(E... values)
    {
        return new Iterator<E>()
        {
            private int cnt = 0;

            @Override
            public boolean hasNext()
            {
                return cnt++ < values.length;
            }

            @Override
            public E next()
            {
                return values[cnt];
            }
        };
    }

    public static boolean isEmpty(Iterable<?> iterable)
    {
        if (iterable instanceof Collection) {
            return ((Collection<?>) iterable).isEmpty();
        }
        return isEmpty(iterable.iterator());
    }

    public static boolean isEmpty(Iterator<?> iterator)
    {
        return !iterator.hasNext();
    }

    public static <T> T getLast(Iterator<T> iterator)
    {
        T value = iterator.next();
        while (iterator.hasNext()) {
            value = iterator.next();
        }
        return value;
    }

    public static <T> T getLast(Iterator<T> iterator, T defaultValue)
    {
        if (!iterator.hasNext()) {
            return defaultValue;
        }
        return getLast(iterator);
    }

    public static <T> T getLast(Iterable<T> iterable)
    {
        if (iterable instanceof List) {
            List<T> list = (List<T>) iterable;
            if (list.isEmpty()) {
                throw new NoSuchElementException();
            }
            return list.get(list.size() - 1);
        }
        else {
            return getLast(iterable.iterator());
        }
    }

    public static <T> T getLast(Iterable<T> iterable, T defaultValue)
    {
        if (iterable instanceof List) {
            List<T> list = (List<T>) iterable;
            if (list.isEmpty()) {
                return defaultValue;
            }
            return list.get(list.size() - 1);
        }
        else {
            return getLast(iterable.iterator(), defaultValue);
        }
    }

    public static long size(Iterator iterator)
    {
        long i;
        for (i = 0; iterator.hasNext(); i++) {
            iterator.next();
        }
        return i;
    }

    public static <F1, F2> Iterable<F2> map(Iterable<F1> iterable, Function<F1, F2> function)
    {
        return () -> map(iterable.iterator(), function);
    }

    public static <F1, F2> Iterator<F2> map(Iterator<F1> iterator, Function<F1, F2> function)
    {
        return new Iterator<F2>()
        {
            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public F2 next()
            {
                return function.apply(iterator.next());
            }

            @Override
            public void remove()
            {
                iterator.remove();
            }
        };
    }

    public static <E> Iterator<E> empty()
    {
        return new Iterator<E>()
        {
            @Override
            public boolean hasNext()
            {
                return false;
            }

            @Override
            public E next()
            {
                throw new NoSuchElementException();
            }
        };
    }

    public static <F1, F2> F2 reduce(Iterator<F1> iterator, Function<F1, F2> mapper, BinaryOperator<F2> reducer)
    {
        F2 lastValue = null;
        while (iterator.hasNext()) {
            F2 value = mapper.apply(iterator.next());
            if (lastValue != null) {
                lastValue = reducer.apply(lastValue, value);
            }
            else {
                lastValue = value;
            }
        }
        return lastValue;
    }

    public static <T> T reduce(Iterator<T> iterator, BinaryOperator<T> reducer)
    {
        T lastValue = null;
        while (iterator.hasNext()) {
            T value = iterator.next();
            if (lastValue != null) {
                lastValue = reducer.apply(lastValue, value);
            }
            else {
                lastValue = value;
            }
        }
        return lastValue;
    }

    public static <F1, F2> Iterator<F2> flatMap(Iterator<F1> iterator, Function<F1, F2[]> function)
    {
        throw new UnsupportedOperationException("this method have't support!");
    }
}
