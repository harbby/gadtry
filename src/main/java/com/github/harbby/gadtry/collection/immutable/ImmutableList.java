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

package com.github.harbby.gadtry.collection.immutable;

import com.github.harbby.gadtry.base.Iterators;
import com.github.harbby.gadtry.collection.mutable.MutableList;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import static java.util.Objects.requireNonNull;

public abstract class ImmutableList<E>
        extends AbstractList<E>
        implements List<E>
{
    private ImmutableList() {}

    private static final List<?> EMPTY = new ImmutableArrayList<>(new Object[0]);

    public static <T> List<T> copy(Iterable<? extends T> iterable)
    {
        if (iterable instanceof Collection) {
            return new ImmutableArrayList<>((((Collection<? extends T>) iterable).toArray()));
        }
        else {
            return new ImmutableArrayList<>(MutableList.copy(iterable).toArray()); //todo: 2 copy 引用
        }
    }

    public static <T> ImmutableList<T> copy(Iterator<? extends T> iterator)
    {
        return new ImmutableArrayList<>(MutableList.copy(iterator).toArray()); //todo: 2 copy 引用
    }

    @SafeVarargs
    public static <T> List<T> of(T... elements)
    {
        switch (elements.length) {
            case 0:
                return (List<T>) ImmutableList.EMPTY;
            case 1:
                return Collections.singletonList(elements[0]);
            default:
                return new ImmutableArrayList<>(elements.clone());
        }
    }

    public static <T> List<T> of(T t1)
    {
        return new ImmutableArrayList<>(new Object[] {t1});
    }

    public static <T> List<T> of(T t1, T t2)
    {
        return new ImmutableArrayList<>(new Object[] {t1, t2});
    }

    public static <T> List<T> of(T t1, T t2, T t3)
    {
        return new ImmutableArrayList<>(new Object[] {t1, t2, t3});
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4)
    {
        return new ImmutableArrayList<>(new Object[] {t1, t2, t3, t4});
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5)
    {
        return new ImmutableArrayList<>(new Object[] {t1, t2, t3, t4, t5});
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5, T t6)
    {
        return new ImmutableArrayList<>(new Object[] {t1, t2, t3, t4, t5, t6});
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5, T t6, T t7)
    {
        return new ImmutableArrayList<>(new Object[] {t1, t2, t3, t4, t5, t6, t7});
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8)
    {
        return new ImmutableArrayList<>(new Object[] {t1, t2, t3, t4, t5, t6, t7, t8});
    }

    static class ImmutableArrayList<E>
            extends ImmutableList<E>
            implements RandomAccess, Serializable
    {
        private final Object[] array;

        public ImmutableArrayList(Object[] array)
        {
            this.array = requireNonNull(array, "array is null");
        }

        @Override
        public E get(int index)
        {
            return (E) array[index];
        }

        @Override
        public int size()
        {
            return array.length;
        }

        @Override
        public Iterator<E> iterator()
        {
            return (Iterator<E>) Iterators.of(array);
        }

        @Override
        public ListIterator<E> listIterator(int index)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sort(Comparator<? super E> c)
        {
            throw new UnsupportedOperationException();
        }
    }
}
