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

import com.github.harbby.gadtry.collection.MutableList;
import com.github.harbby.gadtry.collection.StateOption;
import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.function.Function1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
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

    private static final Iterable<?> EMPTY_ITERABLE = () -> new Iterator<Object>()
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

    @SafeVarargs
    public static <E> Iterator<E> of(E... values)
    {
        return new Iterator<E>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return index < values.length;
            }

            @Override
            public E next()
            {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return values[index++];
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> empty()
    {
        return (Iterator<E>) EMPTY_ITERABLE.iterator();
    }

    @SuppressWarnings("unchecked")
    public static <E> Iterable<E> emptyIterable()
    {
        return (Iterable<E>) EMPTY_ITERABLE;
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

    public static <T> Stream<T> toStream(Iterable<T> iterable)
    {
        if (iterable instanceof Collection) {
            return ((Collection<T>) iterable).stream();
        }
        return StreamSupport.stream(iterable.spliterator(), false);
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

    public static <E1, E2> Iterator<E2> flatMap(Iterator<E1> iterator, Function<E1, Iterator<E2>> flatMap)
    {
        requireNonNull(iterator, "iterator is null");
        requireNonNull(flatMap, "flatMap is null");
        return new Iterator<E2>()
        {
            private Iterator<E2> child = empty();

            @Override
            public boolean hasNext()
            {
                if (child.hasNext()) {
                    return true;
                }
                while (iterator.hasNext()) {
                    this.child = requireNonNull(flatMap.apply(iterator.next()), "user flatMap not return null");
                    if (child.hasNext()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public E2 next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return child.next();
            }
        };
    }

    public static <E> Iterator<E> concat(Iterator<Iterator<E>> iterators)
    {
        checkArgument(iterators != null, "iterators is null");
        if (!iterators.hasNext()) {
            return empty();
        }
        return new Iterator<E>()
        {
            private Iterator<E> child = requireNonNull(iterators.next(), "user flatMap not return null");

            @Override
            public boolean hasNext()
            {
                if (child.hasNext()) {
                    return true;
                }
                while (iterators.hasNext()) {
                    this.child = requireNonNull(iterators.next(), "user flatMap not return null");
                    if (child.hasNext()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public E next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return child.next();
            }
        };
    }

    public static <E> Iterator<E> concat(Iterator<E>... iterators)
    {
        requireNonNull(iterators, "iterators is null");
        return concat(Iterators.of(iterators));
    }

    public static <E> Iterator<E> filter(Iterator<E> iterator, Function1<E, Boolean> filter)
    {
        requireNonNull(iterator, "iterator is null");
        requireNonNull(filter, "filter is null");
        return new Iterator<E>()
        {
            private final StateOption<E> option = StateOption.empty();

            @Override
            public boolean hasNext()
            {
                if (option.isDefined()) {
                    return true;
                }
                while (iterator.hasNext()) {
                    E e = iterator.next();
                    if (filter.apply(e)) {
                        option.update(e);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public E next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return option.remove();
            }
        };
    }

    /**
     * double fraction = setp / max
     *
     * @param iterator 待抽样的Iterator
     * @param setp     setp
     * @param max      max
     * @param seed     随机因子
     * @param <E>      type
     * @return 抽样后的Iterator
     */
    public static <E> Iterator<E> sample(Iterator<E> iterator, int setp, int max, long seed)
    {
        return sample(iterator, setp, max, new Random(seed));
    }

    public static <E> Iterator<E> sample(Iterator<E> iterator, int setp, int max, Random random)
    {
        requireNonNull(iterator, "iterators is null");
        return new Iterator<E>()
        {
            private final StateOption<E> option = StateOption.empty();

            @Override
            public boolean hasNext()
            {
                if (option.isDefined()) {
                    return true;
                }
                while (iterator.hasNext()) {
                    E e = iterator.next();
                    if (random.nextInt(max) < setp) {
                        option.update(e);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public E next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return option.remove();
            }
        };
    }

    public static <E> Iterator<Tuple2<E, Integer>> zipIndex(Iterator<E> iterator, int startIndex)
    {
        return new Iterator<Tuple2<E, Integer>>()
        {
            private int i = startIndex;

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Tuple2<E, Integer> next()
            {
                return new Tuple2<>(iterator.next(), i++);
            }
        };
    }

    public static <E> Iterator<Tuple2<E, Long>> zipIndex(Iterator<E> iterator, long startIndex)
    {
        return new Iterator<Tuple2<E, Long>>()
        {
            private long i = startIndex;

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Tuple2<E, Long> next()
            {
                return new Tuple2<>(iterator.next(), i++);
            }
        };
    }

    public static <T> Iterator<T> mergeSorted(Iterable<? extends Iterator<? extends T>> inputs, Comparator<? super T> comparator)
    {
        final List<? extends Iterator<? extends T>> iterators = MutableList.copy(inputs);

        final List<Tuple2<T, Integer>> sortArr = new ArrayList<>();
        for (int i = 0; i < iterators.size(); i++) {
            Iterator<? extends T> iterator = iterators.get(i);
            if (iterator.hasNext()) {
                sortArr.add(new Tuple2<>(iterator.next(), i));
            }
        }

        return new Iterator<T>()
        {
            private Tuple2<T, Integer> node;

            @Override
            public boolean hasNext()
            {
                if (node != null) {
                    return true;
                }
                if (sortArr.isEmpty()) {
                    return false;
                }
                if (sortArr.size() > 1) {
                    sortArr.sort((x1, x2) -> comparator.compare(x1.f1(), x2.f1()));
                }
                this.node = sortArr.get(0);
                return true;
            }

            @Override
            public T next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T value = node.f1();
                Iterator<? extends T> iterator = iterators.get(node.f2);
                if (iterator.hasNext()) {
                    node.f1 = iterator.next();
                }
                else {
                    sortArr.remove(0);
                }
                this.node = null;
                return value;
            }
        };
    }
}
