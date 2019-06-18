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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

/**
 * Love Iterator
 */
public class Iterators
{
    private Iterators() {}

    private static final Iterator EMPTY_ITERATOR = new Iterator()
    {
        @Override
        public boolean hasNext()
        {
            return false;
        }

        @Override
        public Object next()
        {
            throw new NoSuchElementException();
        }
    };

    public static <E> Iterator<E> of(E... values)
    {
        return new Iterator<E>()
        {
            private int cnt = 0;

            @Override
            public boolean hasNext()
            {
                return cnt < values.length;
            }

            @Override
            public E next()
            {
                return values[cnt++];
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> empty()
    {
        return (Iterator<E>) EMPTY_ITERATOR;
    }

    public static boolean isEmpty(Iterable<?> iterable)
    {
        requireNonNull(iterable);
        if (iterable instanceof Collection) {
            return ((Collection<?>) iterable).isEmpty();
        }
        return isEmpty(iterable.iterator());
    }

    public static boolean isEmpty(Iterator<?> iterator)
    {
        requireNonNull(iterator);
        return !iterator.hasNext();
    }

    public static <T> Stream<T> toStream(Iterator<T> iterator)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    public static <T> Stream<T> toStream(Iterator<T> iterator, Runnable close)
    {
        return toStream(iterator).onClose(close);
    }

    public static <T> T getFirst(Iterator<T> iterator, int index, T defaultValue)
    {
        return getFirst(iterator, index, (Supplier<T>) () -> defaultValue);
    }

    public static <T> T getFirst(Iterator<T> iterator, int index, Supplier<T> defaultValue)
    {
        requireNonNull(iterator);
        requireNonNull(defaultValue);
        checkState(index >= 0, "must index >= 0");
        T value;
        int number = 0;
        while (iterator.hasNext()) {
            value = iterator.next();
            if (number++ == index) {
                return value;
            }
        }
        return defaultValue.get();
    }

    public static <T> T getFirst(Iterator<T> iterator, int index)
    {
        return getFirst(iterator, index, (Supplier<T>) () -> {
            throw new NoSuchElementException();
        });
    }

    public static <T> T getLast(Iterator<T> iterator)
    {
        requireNonNull(iterator);
        T value = iterator.next();
        while (iterator.hasNext()) {
            value = iterator.next();
        }
        return value;
    }

    public static <T> T getLast(Iterator<T> iterator, T defaultValue)
    {
        requireNonNull(iterator);
        if (!iterator.hasNext()) {
            return defaultValue;
        }
        return getLast(iterator);
    }

    public static <T> T getLast(Iterable<T> iterable)
    {
        requireNonNull(iterable);
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
        requireNonNull(iterable);
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

    public static long size(Iterator<?> iterator)
    {
        requireNonNull(iterator);
        long i;
        for (i = 0; iterator.hasNext(); i++) {
            iterator.next();
        }
        return i;
    }

    public static <F1, F2> Iterable<F2> map(Iterable<F1> iterable, Function<F1, F2> function)
    {
        requireNonNull(iterable);
        requireNonNull(function);
        return () -> map(iterable.iterator(), function);
    }

    public static <F1, F2> Iterator<F2> map(Iterator<F1> iterator, Function<F1, F2> function)
    {
        requireNonNull(iterator);
        requireNonNull(function);
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

    public static <F1, F2> F2 reduce(Iterator<F1> iterator, Function<F1, F2> mapper, BinaryOperator<F2> reducer)
    {
        return reduce(map(iterator, mapper), reducer);
    }

    public static <T> T reduce(Iterator<T> iterator, BinaryOperator<T> reducer)
    {
        requireNonNull(iterator);
        requireNonNull(reducer);
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

    public static <T> Iterator<T> limit(Iterator<T> iterator, int limit)
    {
        requireNonNull(iterator);
        checkArgument(limit >= 0, "limit must >= 0");
        return new Iterator<T>()
        {
            private int number;

            @Override
            public boolean hasNext()
            {
                return number < limit && iterator.hasNext();
            }

            @Override
            public T next()
            {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                else {
                    number++;
                    return iterator.next();
                }
            }
        };
    }

    public static <T> void foreach(Iterator<T> iterator, Consumer<T> function)
    {
        requireNonNull(iterator);
        iterator.forEachRemaining(function);
    }

    public static <F1, F2> Iterator<F2> flatMap(Iterator<F1> iterator, Function<F1, F2[]> function)
    {
        throw new UnsupportedOperationException("this method have't support!");
    }
}
