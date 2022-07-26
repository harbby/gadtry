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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

public abstract class ImmutableList<E>
        extends AbstractList<E>
        implements List<E>, RandomAccess
{
    private static final ImmutableList<?> EMPTY = new EmptyImmutableList<>();
    static final Object[] EMPTY_ARRAY = new Object[0];

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> copy(Iterable<? extends T> iterable)
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

    public static <T> ImmutableList<T> copy(T[] elements)
    {
        switch (elements.length) {
            case 0:
                return empty();
            case 1:
                return new SingleImmutableList<>(elements[0]);
            default:
                return new ImmutableArrayList<>(elements.clone());
        }
    }

    public static <T> ImmutableList<T> wrap(T[] array)
    {
        switch (array.length) {
            case 0:
                return empty();
            case 1:
                return ImmutableList.of(array[0]);
            default:
                return new ImmutableArrayList<>(array);
        }
    }

    public static <T> ImmutableList<T> of()
    {
        return empty();
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> empty()
    {
        return (ImmutableList<T>) EMPTY;
    }

    public static <T> ImmutableList<T> of(T t1)
    {
        return new SingleImmutableList<>(t1);
    }

    public static <T> ImmutableList<T> of(T t1, T t2)
    {
        return checkedArr(t1, t2);
    }

    public static <T> ImmutableList<T> of(T t1, T t2, T t3)
    {
        return checkedArr(t1, t2, t3);
    }

    public static <T> ImmutableList<T> of(T t1, T t2, T t3, T t4)
    {
        return checkedArr(t1, t2, t3, t4);
    }

    public static <T> ImmutableList<T> of(T t1, T t2, T t3, T t4, T t5)
    {
        return checkedArr(t1, t2, t3, t4, t5);
    }

    public static <T> ImmutableList<T> of(T t1, T t2, T t3, T t4, T t5, T t6)
    {
        return checkedArr(t1, t2, t3, t4, t5, t6);
    }

    public static <T> ImmutableList<T> of(T t1, T t2, T t3, T t4, T t5, T t6, T t7)
    {
        return checkedArr(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T> ImmutableList<T> of(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8)
    {
        return checkedArr(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    @SafeVarargs
    public static <T> ImmutableList<T> of(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T... others)
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
            return new ImmutableArrayList<>(array);
        }
        return checkedArr(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    @SafeVarargs
    private static <T> ImmutableList<T> checkedArr(T... array)
    {
        return new ImmutableArrayList<>(array);
    }

    private static class ImmutableArrayList<E>
            extends ImmutableList<E>
            implements RandomAccess, Serializable
    {
        private final E[] array;
        private final int fromIndex;
        private final int toIndex;
        private final int size;

        private ImmutableArrayList(E[] array)
        {
            this(array, 0, array.length);
        }

        private ImmutableArrayList(E[] array, int fromIndex, int toIndex)
        {
            this.array = array;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.size = (toIndex - fromIndex);
            for (int i = fromIndex; i < toIndex; i++) {
                requireNonNull(array[i], "value is null");
            }
        }

        @Override
        public E get(int index)
        {
            return array[index + fromIndex];
        }

        @Override
        public boolean contains(Object o)
        {
            for (int i = fromIndex; i < toIndex; i++) {
                if (array[i].equals(o)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void forEach(Consumer<? super E> action)
        {
            for (int i = fromIndex; i < toIndex; i++) {
                action.accept(array[i]);
            }
        }

        @Override
        public int indexOf(Object o)
        {
            for (int i = fromIndex; i < toIndex; i++) {
                if (array[i].equals(o)) {
                    return i - fromIndex;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o)
        {
            for (int i = toIndex - 1; i >= fromIndex; i--) {
                if (array[i].equals(o)) {
                    return i - fromIndex;
                }
            }
            return -1;
        }

        @Override
        public Object[] toArray()
        {
            Object[] objects = new Object[size];
            System.arraycopy(array, fromIndex, objects, 0, size);
            return objects;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a)
        {
            T[] objects = a.length >= size ? a :
                    (T[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), size);
            System.arraycopy(array, fromIndex, objects, 0, size);
            return objects;
        }

        @Override
        public final void sort(Comparator<? super E> c)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size()
        {
            return size;
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
            return new ImmutableListIterator<>(array, fromIndex, toIndex, index + fromIndex);
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
            if (fromIndex > toIndex) {
                throw new IllegalArgumentException("fromIndex(" + fromIndex +
                        ") > toIndex(" + toIndex + ")");
            }
            return new ImmutableArrayList<>(array, this.fromIndex + fromIndex, this.fromIndex + toIndex);
        }

        @Override
        public ListIterator<E> listIterator()
        {
            return listIterator(0);
        }
    }

    private static class SingleImmutableList<E>
            extends ImmutableList<E>
            implements RandomAccess, Serializable
    {
        private final E value;

        private SingleImmutableList(E value)
        {
            this.value = requireNonNull(value, "value is null");
        }

        @Override
        public Iterator<E> iterator()
        {
            return Iterators.of(value);
        }

        @Override
        public E get(int index)
        {
            if (index == 0) {
                return value;
            }
            else {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
            }
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public boolean contains(Object o)
        {
            return value.equals(o);
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            return c.contains(value);
        }

        @Override
        public void forEach(Consumer<? super E> action)
        {
            action.accept(value);
        }

        @Override
        public int indexOf(Object o)
        {
            if (this.contains(0)) {
                return 0;
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o)
        {
            return this.indexOf(o);
        }

        @Override
        public Object[] toArray()
        {
            return new Object[] {value};
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a)
        {
            T[] objects = a.length >= 1 ? a :
                    (T[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), 1);
            objects[0] = (T) value;
            return objects;
        }

        @Override
        public final void sort(Comparator<? super E> c)
        {
        }

        @Override
        public ImmutableList<E> subList(int fromIndex, int toIndex)
        {
            if (fromIndex < 0) {
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            }
            if (toIndex > this.size()) {
                throw new IndexOutOfBoundsException("toIndex = " + toIndex);
            }
            if (fromIndex > toIndex) {
                throw new IllegalArgumentException("fromIndex(" + fromIndex +
                        ") > toIndex(" + toIndex + ")");
            }
            int size = toIndex - fromIndex;
            if (size == 1) {
                return this;
            }
            else {
                return ImmutableList.empty();
            }
        }
    }

    private static class EmptyImmutableList<E>
            extends ImmutableList<E>
            implements RandomAccess, Serializable
    {
        @Override
        public E get(int index)
        {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 0");
        }

        @Override
        public Iterator<E> iterator()
        {
            return Iterators.empty();
        }

        @Override
        public int size()
        {
            return 0;
        }

        @Override
        public void forEach(Consumer<? super E> action)
        {
            requireNonNull(action, "action is null");
        }

        @Override
        public boolean contains(Object o)
        {
            return false;
        }

        @Override
        public Object[] toArray()
        {
            return EMPTY_ARRAY;
        }

        @Override
        public <T> T[] toArray(T[] a)
        {
            return a;
        }

        @Override
        public void sort(Comparator<? super E> c)
        {
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator)
        {
            requireNonNull(operator);
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter)
        {
            requireNonNull(filter);
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            return c.isEmpty();
        }

        @Override
        public Spliterator<E> spliterator()
        {
            return Spliterators.emptySpliterator();
        }

        @Override
        public ImmutableList<E> subList(int fromIndex, int toIndex)
        {
            if (fromIndex < 0) {
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            }
            if (toIndex > this.size()) {
                throw new IndexOutOfBoundsException("toIndex = " + toIndex);
            }
            if (fromIndex > toIndex) {
                throw new IllegalArgumentException("fromIndex(" + fromIndex +
                        ") > toIndex(" + toIndex + ")");
            }
            return this;
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter)
    {
        requireNonNull(filter);
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean remove(Object o)
    {
        return super.remove(o);
    }

    @Override
    public final boolean addAll(Collection<? extends E> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean add(E e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final E set(int index, E element)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(int index, E element)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final E remove(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(int index, Collection<? extends E> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final void removeRange(int fromIndex, int toIndex)
    {
        throw new UnsupportedOperationException();
    }

    private static class ImmutableListIterator<E>
            implements ListIterator<E>
    {
        private final E[] array;
        private int position;
        private final int fromIndex;
        private final int toIndex;

        private ImmutableListIterator(E[] array, int fromIndex, int toIndex, int position)
        {
            this.array = array;
            this.position = position;
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

    public static <E> Builder<E> builder()
    {
        return new Builder<>();
    }

    public static class Builder<E>
    {
        private final List<E> list = new ArrayList<>();

        public Builder<E> add(E value)
        {
            list.add(value);
            return this;
        }

        public Builder<E> addAll(Collection<E> collection)
        {
            list.addAll(collection);
            return this;
        }

        public final Builder<E> addAll(E[] collection)
        {
            for (E e : collection) {
                this.add(e);
            }
            return this;
        }

        public ImmutableList<E> build()
        {
            return ImmutableList.copy(list);
        }
    }
}
