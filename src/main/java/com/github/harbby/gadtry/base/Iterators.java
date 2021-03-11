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

import com.github.harbby.gadtry.collection.ImmutableList;
import com.github.harbby.gadtry.collection.StateOption;
import com.github.harbby.gadtry.collection.tuple.Tuple2;
import com.github.harbby.gadtry.function.Function1;
import com.github.harbby.gadtry.function.Reducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
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

    public static final Runnable EMPTY_CLOSE = () -> {};
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

    public interface ResetIterator<E>
            extends Iterator<E>
    {
        public void reset();
    }

    @SafeVarargs
    public static <E> ResetIterator<E> of(E... values)
    {
        return new ResetIterator<E>()
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

            @Override
            public void reset()
            {
                this.index = 0;
            }
        };
    }

    @SafeVarargs
    public static <E> ResetIterator<E> warp(E... values)
    {
        return of(values);
    }

    public static <E> ResetIterator<E> warp(List<E> values)
    {
        return new ResetIterator<E>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return index < values.size();
            }

            @Override
            public E next()
            {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return values.get(index++);
            }

            @Override
            public void reset()
            {
                this.index = 0;
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

    public static <T> Optional<T> reduce(Iterator<T> iterator, BinaryOperator<T> reducer)
    {
        requireNonNull(iterator);
        requireNonNull(reducer);
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        T lastValue = iterator.next();
        while (iterator.hasNext()) {
            lastValue = reducer.apply(lastValue, iterator.next());
        }
        return Optional.ofNullable(lastValue);
    }

    public static <T> Iterator<T> limit(Iterator<T> iterator, int limit)
    {
        return limit(iterator, limit, EMPTY_CLOSE);
    }

    public static <T> Iterator<T> limit(Iterator<T> iterator, int limit, Runnable autoClose)
    {
        requireNonNull(iterator);
        checkArgument(limit >= 0, "limit must >= 0");
        return new Iterator<T>()
        {
            private int number = 0;
            private boolean done = false;

            @Override
            public boolean hasNext()
            {
                boolean hasNext = number < limit && iterator.hasNext();
                if (!hasNext && !done) {
                    autoClose.run();
                    done = true;
                }
                return hasNext;
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
        return flatMap(iterator, flatMap, EMPTY_CLOSE);
    }

    public static <E1, E2> Iterator<E2> flatMap(Iterator<E1> iterator,
            Function<E1, Iterator<E2>> flatMap,
            Runnable autoClose)
    {
        requireNonNull(iterator, "iterator is null");
        requireNonNull(flatMap, "flatMap is null");
        requireNonNull(autoClose, "autoClose is null");
        return new Iterator<E2>()
        {
            private Iterator<E2> child = empty();
            private boolean done = false;

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
                if (!done) {
                    done = true;
                    autoClose.run();
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

    public static <E> Iterator<E> concat(Iterator<? extends Iterator<E>> iterators)
    {
        requireNonNull(iterators, "iterators is null");
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

    @SafeVarargs
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

    public static <T> Iterator<T> mergeSorted(Comparator<T> comparator, List<Iterator<T>> inputs)
    {
        requireNonNull(comparator, "comparator is null");
        requireNonNull(inputs, "inputs is null");
        if (inputs.size() == 0) {
            return Iterators.empty();
        }
        if (inputs.size() == 1) {
            return inputs.get(0);
        }
        final PriorityQueue<Tuple2<T, Iterator<T>>> priorityQueue = new PriorityQueue<>(inputs.size() + 1, (o1, o2) -> comparator.compare(o1.f1, o2.f1));
        for (Iterator<T> iterator : inputs) {
            if (iterator.hasNext()) {
                priorityQueue.add(Tuple2.of(iterator.next(), iterator));
            }
        }

        return new Iterator<T>()
        {
            private Tuple2<T, Iterator<T>> node;

            @Override
            public boolean hasNext()
            {
                if (node != null) {
                    return true;
                }
                if (priorityQueue.isEmpty()) {
                    return false;
                }
                this.node = priorityQueue.poll();
                return true;
            }

            @Override
            public T next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T value = node.f1();
                Iterator<? extends T> iterator = node.f2;
                if (iterator.hasNext()) {
                    node.f1 = iterator.next();
                    priorityQueue.add(node);
                }
                this.node = null;
                return value;
            }
        };
    }

    @SafeVarargs
    public static <T> Iterator<T> mergeSorted(Comparator<T> comparator, Iterator<T>... inputs)
    {
        return mergeSorted(comparator, ImmutableList.of(inputs));
    }

    public static <K, V> Iterator<Tuple2<K, V>> reduceSorted(Iterator<Tuple2<K, V>> input, Reducer<V> reducer)
    {
        return reduceSorted(input, reducer, EMPTY_CLOSE);
    }

    public static <K, V> Iterator<Tuple2<K, V>> reduceSorted(Iterator<Tuple2<K, V>> input, Reducer<V> reducer, Runnable autoClose)
    {
        requireNonNull(reducer, "reducer is null");
        requireNonNull(input, "input iterator is null");
        if (!input.hasNext()) {
            return Iterators.empty();
        }
        return new Iterator<Tuple2<K, V>>()
        {
            private final StateOption<Tuple2<K, V>> option = StateOption.empty();
            private Tuple2<K, V> lastRow = input.next();
            private boolean done = false;

            @Override
            public boolean hasNext()
            {
                if (option.isDefined()) {
                    return true;
                }
                if (done) {
                    return false;
                }
                while (input.hasNext()) {
                    Tuple2<K, V> tp = input.next();
                    if (!tp.f1.equals(lastRow.f1)) {
                        option.update(lastRow);
                        this.lastRow = tp;
                        return true;
                    }
                    lastRow.f2 = reducer.reduce(lastRow.f2, tp.f2);
                }
                option.update(lastRow);
                this.lastRow = null;
                this.done = true;
                autoClose.run();
                return true;
            }

            @Override
            public Tuple2<K, V> next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return option.remove();
            }
        };
    }

    public static <K, V1, V2> Iterator<Tuple2<K, Tuple2<V1, V2>>> mergeJoin(
            Comparator<K> comparator,
            Iterator<Tuple2<K, V1>> leftStream,
            Iterator<Tuple2<K, V2>> rightStream)
    {
        if (!leftStream.hasNext() || !rightStream.hasNext()) {
            return Iterators.empty();
        }
        return new Iterator<Tuple2<K, Tuple2<V1, V2>>>()
        {
            private Tuple2<K, V1> leftNode = leftStream.next();
            private Tuple2<K, V2> rightNode = null;

            private final List<Tuple2<K, V1>> leftSameKeys = new ArrayList<>();
            private final ResetIterator<Tuple2<K, V1>> leftSameIterator = Iterators.warp(leftSameKeys);
            private final Iterator<Tuple2<K, Tuple2<V1, V2>>> child = Iterators.map(leftSameIterator, x -> Tuple2.of(x.f1, Tuple2.of(x.f2, rightNode.f2)));

            @Override
            public boolean hasNext()
            {
                if (child.hasNext()) {
                    return true;
                }
                if (!rightStream.hasNext()) {
                    return false;
                }
                this.rightNode = rightStream.next();

                if (!leftSameKeys.isEmpty() && Objects.equals(leftSameKeys.get(0).f1, rightNode.f1)) {
                    leftSameIterator.reset();
                    return true;
                }
                while (true) {
                    int than = comparator.compare(leftNode.f1, rightNode.f1);
                    if (than == 0) {
                        leftSameKeys.clear();
                        do {
                            leftSameKeys.add(leftNode);
                            if (leftStream.hasNext()) {
                                leftNode = leftStream.next();
                            }
                            else {
                                break;
                            }
                        }
                        while (Objects.equals(leftNode.f1, rightNode.f1));
                        leftSameIterator.reset();
                        return true;
                    }
                    else if (than > 0) {
                        if (!rightStream.hasNext()) {
                            return false;
                        }
                        this.rightNode = rightStream.next();
                    }
                    else {
                        if (!leftStream.hasNext()) {
                            return false;
                        }
                        this.leftNode = leftStream.next();
                    }
                }
            }

            @Override
            public Tuple2<K, Tuple2<V1, V2>> next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return child.next();
            }
        };
    }
}
