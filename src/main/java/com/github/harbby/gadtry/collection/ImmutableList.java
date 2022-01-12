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

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

public abstract class ImmutableList<E>
        extends AbstractList<E>
        implements List<E>, RandomAccess
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
    public static <T> List<T> copy(T[] elements)
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

    @SuppressWarnings("unchecked")
    public static <T> List<T> wrap(T[] array)
    {
        switch (array.length) {
            case 0:
                return (List<T>) ImmutableArrayList.EMPTY;
            case 1:
                return Collections.singletonList(array[0]);
            default:
                return new ImmutableArrayList<>(array);
        }
    }

    public static <T> List<T> of()
    {
        return empty();
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> empty()
    {
        return (List<T>) ImmutableArrayList.EMPTY;
    }

    public static <T> List<T> of(T t1)
    {
        return Collections.singletonList(t1);
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
    public static <T> List<T> of(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T... others)
    {
        if (others.length > 0) {
            @SuppressWarnings("unchecked")
            T[] array = (T[]) new Object[8 + others.length];
            array[0] = t1;
            array[1] = t2;
            array[2] = t3;
            array[3] = t4;
            array[4] = t5;
            array[5] = t6;
            array[6] = t7;
            array[7] = t8;
            System.arraycopy(others, 0, array, 8, others.length);
            return wrap(array);
        }
        return checkedArr(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    @SafeVarargs
    private static <T> List<T> checkedArr(T... array)
    {
        return wrap(array);
    }

    private static class ImmutableArrayList<E>
            extends ImmutableList<E>
            implements RandomAccess, Serializable
    {
        private static final ImmutableArrayList<Object> EMPTY = new ImmutableArrayList<>(new Object[0]);

        private final E[] array;
        private final int fromIndex;
        private final int toIndex;

        private ImmutableArrayList(E[] array)
        {
            this(array, 0, array.length);
        }

        private ImmutableArrayList(E[] array, int fromIndex, int toIndex)
        {
            this.array = array;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        public E get(int index)
        {
            return array[index + fromIndex];
        }

        @Override
        public int size()
        {
            return toIndex - fromIndex;
        }

        @Override
        public Iterator<E> iterator()
        {
            return listIterator();
        }

        @Override
        public ListIterator<E> listIterator(int index)
        {
            if (index < 0) {
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            }
            if (index > this.size()) {
                throw new IndexOutOfBoundsException("index = " + toIndex);
            }
            return new ImmutableListIterator<>(array, fromIndex + index, toIndex);
        }

        @Override
        public void sort(Comparator<? super E> c)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableArrayList<E> subList(int fromIndex, int toIndex)
        {
            if (fromIndex < 0) {
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            }
            if (toIndex > this.size()) {
                throw new IndexOutOfBoundsException("toIndex = " + toIndex);
            }
            checkState(fromIndex <= toIndex, "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
            return new ImmutableArrayList<>(array, this.fromIndex + fromIndex, this.fromIndex + toIndex);
        }

        @Override
        public ListIterator<E> listIterator()
        {
            return listIterator(0);
        }
    }

    private static class ImmutableListIterator<E>
            implements ListIterator<E>
    {
        private final E[] array;
        private int position;
        private final int fromIndex;
        private final int toIndex;

        private ImmutableListIterator(E[] array, int fromIndex, int toIndex)
        {
            this.array = array;
            this.position = fromIndex;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        public boolean hasNext()
        {
            return position < toIndex;
        }

        @Override
        public E next()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return array[position++];
        }

        @Override
        public boolean hasPrevious()
        {
            return position > fromIndex;
        }

        @Override
        public E previous()
        {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return array[--position];
        }

        @Override
        public int nextIndex()
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return position - fromIndex;
        }

        @Override
        public int previousIndex()
        {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return position - 1 - fromIndex;
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
