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
import com.github.harbby.gadtry.function.Function1;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static java.util.Objects.requireNonNull;

public interface IteratorPlus<E>
        extends Iterator<E>, Serializable
{
    IteratorPlus<?> EMPTY = new IteratorPlus<Object>()
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

    @SuppressWarnings("unchecked")
    static <E> IteratorPlus<E> empty()
    {
        return (IteratorPlus<E>) EMPTY;
    }

    default boolean isEmpty()
    {
        return !this.hasNext();
    }

    default Stream<E> toStream()
    {
        return Iterators.toStream(this);
    }

    default long size()
    {
        long i;
        for (i = 0; this.hasNext(); i++) {
            this.next();
        }
        return i;
    }

    default Optional<E> reduce(BinaryOperator<E> reducer)
    {
        return Iterators.reduce(this, reducer);
    }

    default <O> IteratorPlus<O> map(Function<E, O> function)
    {
        requireNonNull(function);
        return new IteratorPlus<O>()
        {
            @Override
            public boolean hasNext()
            {
                return IteratorPlus.this.hasNext();
            }

            @Override
            public O next()
            {
                return function.apply(IteratorPlus.this.next());
            }

            @Override
            public void remove()
            {
                IteratorPlus.this.remove();
            }
        };
    }

    default <O> IteratorPlus<O> flatMap(Function<E, Iterator<O>> flatMap)
    {
        requireNonNull(flatMap, "flatMap is null");
        return new IteratorPlus<O>()
        {
            private Iterator<O> child = empty();

            @Override
            public boolean hasNext()
            {
                if (child.hasNext()) {
                    return true;
                }
                while (IteratorPlus.this.hasNext()) {
                    this.child = requireNonNull(flatMap.apply(IteratorPlus.this.next()), "user flatMap not return null");
                    if (child.hasNext()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public O next()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return child.next();
            }
        };
    }

    default IteratorPlus<E> filter(Function1<E, Boolean> filter)
    {
        requireNonNull(filter, "filter is null");
        return new IteratorPlus<E>()
        {
            private final StateOption<E> option = StateOption.empty();

            @Override
            public boolean hasNext()
            {
                if (option.isDefined()) {
                    return true;
                }
                while (IteratorPlus.this.hasNext()) {
                    E e = IteratorPlus.this.next();
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

    default IteratorPlus<E> limit(int limit)
    {
        checkArgument(limit >= 0, "limit must >= 0");
        return new IteratorPlus<E>()
        {
            private int number = 0;

            @Override
            public boolean hasNext()
            {
                return number < limit && IteratorPlus.this.hasNext();
            }

            @Override
            public E next()
            {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                else {
                    number++;
                    return IteratorPlus.this.next();
                }
            }
        };
    }
}
