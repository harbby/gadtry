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

import com.github.harbby.gadtry.collection.mutable.MutableList;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import static java.util.Objects.requireNonNull;

public abstract class ImmutableList<E>
        extends AbstractList<E>
        implements List<E>
{
    @SuppressWarnings("unchecked")
    public static <T> List<T> copy(Iterable<? extends T> iterable)
    {
        if (iterable instanceof Collection) {
            return new ImmutableArrayList<>((T[]) ((Collection<? extends T>) iterable).toArray());
        }
        else {
            return new ImmutableArrayList<>((T[]) MutableList.copy(iterable).toArray());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> copy(Iterator<? extends T> iterator)
    {
        return new ImmutableArrayList<>((T[]) MutableList.copy(iterator).toArray());
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> List<T> of(T... elements)
    {
        switch (elements.length) {
            case 0:
                return (List<T>) ImmutableArrayList.EMPTY;
            case 1:
                return Collections.singletonList(elements[0]);
            default:
                return new ImmutableArrayList<>(elements.clone());
        }
    }

    public static <T> List<T> of(T t1)
    {
        return checkedArr(t1);
    }

    public static <T> List<T> of(T t1, T t2)
    {
        return checkedArr(t1, t2);
    }

    public static <T> List<T> of(T t1, T t2, T t3)
    {
        return checkedArr(t1, t2, t3);
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4)
    {
        return checkedArr(t1, t2, t3, t4);
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5)
    {
        return checkedArr(t1, t2, t3, t4, t5);
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5, T t6)
    {
        return checkedArr(t1, t2, t3, t4, t5, t6);
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5, T t6, T t7)
    {
        return checkedArr(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8)
    {
        return checkedArr(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    @SafeVarargs
    private static <T> ImmutableArrayList<T> checkedArr(T... array)
    {
        return new ImmutableArrayList<>(requireNonNull(array, "array is null"));
    }

    private static class ImmutableArrayList<E>
            extends ImmutableList<E>
            implements RandomAccess, Serializable
    {
        private static final ImmutableArrayList<Object> EMPTY = new ImmutableArrayList<>(new Object[0]);

        private final E[] array;

        private ImmutableArrayList(E[] array)
        {
            this.array = array;
        }

        @Override
        public E get(int index)
        {
            return array[index];
        }

        @Override
        public int size()
        {
            return array.length;
        }

        @Override
        public Iterator<E> iterator()
        {
            return listIterator();
        }

        @Override
        public ListIterator<E> listIterator(int index)
        {
            return new ImmutableListIterator<>(array, index);
        }

        @Override
        public void sort(Comparator<? super E> c)
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class ImmutableListIterator<E>
            implements ListIterator<E>
    {
        private final E[] array;
        private int position;

        private ImmutableListIterator(E[] array, int index)
        {
            this.array = array;
            this.position = index;
        }

        @Override
        public boolean hasNext()
        {
            return position < array.length;
        }

        @Override
        public E next()
        {
            if (position < array.length) {
                return array[position++];
            }
            else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean hasPrevious()
        {
            return position > 0;
        }

        @Override
        public E previous()
        {
            if (position > -1) {
                return array[--position];
            }
            else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public int nextIndex()
        {
            return position;
        }

        @Override
        public int previousIndex()
        {
            return position - 1;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e)
        {
            throw new UnsupportedOperationException();
        }
    }
}
