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
package com.github.harbby.gadtry.collection;

import com.github.harbby.gadtry.base.Iterators;
import com.github.harbby.gadtry.collection.tuple.Tuple1;
import com.github.harbby.gadtry.function.Function1;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * 另一种形式的迭代器，和java.util.Iterator不同另一种迭代器，常用在IO等高性能场景。
 * 经典用例: java.sql.Result, hbase scanResult
 * 相比java.util.Iterator，每次迭代减少一次HasNext()方法调用
 */
public interface EachIterator<E>
        extends AutoCloseable, Serializable
{
    EachIterator<?> EMPTY = new EachIterator<Object>()
    {
        @Override
        public boolean next()
        {
            return false;
        }

        @Override
        public Object current()
        {
            throw new NoSuchElementException();
        }
    };

    @SuppressWarnings("unchecked")
    static <E> EachIterator<E> empty()
    {
        return (EachIterator<E>) EMPTY;
    }

    boolean next();

    E current();

    @Override
    default void close()
            throws Exception
    {}

    default Stream<E> toStream()
    {
        return Iterators.toStream(toIterator());
    }

    default IteratorPlus<E> toIterator()
    {
        return new IteratorPlus<E>()
        {
            private final StateOption<E> option = StateOption.empty();

            @Override
            public boolean hasNext()
            {
                if (option.isDefined()) {
                    return true;
                }
                if (EachIterator.this.next()) {
                    option.update(EachIterator.this.current());
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

            @Override
            public void close()
                    throws Exception
            {
                EachIterator.this.close();
            }
        };
    }

    default <O> EachIterator<O> map(Function<E, O> function)
    {
        requireNonNull(function);
        return new EachIterator<O>()
        {
            @Override
            public boolean next()
            {
                return EachIterator.this.next();
            }

            @Override
            public O current()
            {
                return function.apply(EachIterator.this.current());
            }

            @Override
            public void close()
                    throws Exception
            {
                EachIterator.this.close();
            }
        };
    }

    default <O> EachIterator<O> flatMap(Function<E, EachIterator<O>> flatMap)
    {
        requireNonNull(flatMap, "flatMap is null");
        return new EachIterator<O>()
        {
            private EachIterator<O> child = empty();

            @Override
            public boolean next()
            {
                if (child.next()) {
                    return true;
                }
                while (EachIterator.this.next()) {
                    this.child = requireNonNull(flatMap.apply(EachIterator.this.current()), "user flatMap not return null");
                    if (child.next()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public O current()
            {
                return child.current();
            }
        };
    }

    default EachIterator<E> filter(Function1<E, Boolean> filter)
    {
        requireNonNull(filter, "filter is null");
        return new EachIterator<E>()
        {
            private Tuple1<E> option = null;

            @Override
            public boolean next()
            {
                while (EachIterator.this.next()) {
                    E e = EachIterator.this.current();
                    if (filter.apply(e)) {
                        if (option == null) {
                            option = Tuple1.of(e);
                        }
                        else {
                            option.f1 = e;
                        }
                        return true;
                    }
                }
                return false;
            }

            @Override
            public E current()
            {
                if (option == null) {
                    throw new NoSuchElementException();
                }
                return option.f1;
            }

            @Override
            public void close()
                    throws Exception
            {
                EachIterator.this.close();
            }
        };
    }

    default EachIterator<E> limit(int limit)
    {
        checkArgument(limit >= 0, "limit must >= 0");
        return new EachIterator<E>()
        {
            private int i = 0;

            @Override
            public boolean next()
            {
                return i++ < limit && EachIterator.this.next();
            }

            @Override
            public E current()
            {
                return EachIterator.this.current();
            }

            @Override
            public void close()
                    throws Exception
            {
                EachIterator.this.close();
            }
        };
    }

    default Optional<E> reduce(BinaryOperator<E> reducer)
    {
        requireNonNull(reducer);
        if (!this.next()) {
            return Optional.empty();
        }
        E lastValue = this.current();
        while (this.next()) {
            lastValue = reducer.apply(lastValue, this.current());
        }
        return Optional.ofNullable(lastValue);
    }
}
