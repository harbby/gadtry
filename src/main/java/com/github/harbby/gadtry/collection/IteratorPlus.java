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
import com.github.harbby.gadtry.collection.iterator.PeekIterator;
import com.github.harbby.gadtry.function.FilterFunction;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public interface IteratorPlus<E>
        extends Iterator<E>
{
    default PeekIterator<E> asPeekIterator()
    {
        return Iterators.peekIterator(this);
    }

    default Stream<E> toStream()
    {
        return Iterators.toStream(this);
    }

    default long size()
    {
        return Iterators.size(this);
    }

    default Optional<E> reduce(BinaryOperator<E> reducer)
    {
        return Iterators.reduce(this, reducer);
    }

    default <O> IteratorPlus<O> map(Function<E, O> function)
    {
        return Iterators.map(this, function);
    }

    default <O> IteratorPlus<O> flatMap(Function<E, Iterator<O>> flatMap)
    {
        return Iterators.flatMap(this, flatMap);
    }

    default IteratorPlus<E> filter(FilterFunction<E> filter)
    {
        return Iterators.filter(this, filter);
    }

    default IteratorPlus<E> limit(int limit)
    {
        return Iterators.limit(this, limit);
    }

    default IteratorPlus<E> autoClose(Runnable autoClose)
    {
        return Iterators.autoClose(this, autoClose);
    }
}
