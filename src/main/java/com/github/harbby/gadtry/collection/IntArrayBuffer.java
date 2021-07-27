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

import com.github.harbby.gadtry.base.Platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.function.IntConsumer;

import static java.util.Objects.requireNonNull;

public class IntArrayBuffer
        implements Iterable<Integer>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final int DEFAULT_CAPACITY = 10;
    private static final int[] EMPTY_ELEMENTDATA = {};
    private static final int[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private transient int modCount = 0;
    private transient int[] elementData;
    private int size;

    public IntArrayBuffer(int initialCapacity)
    {
        if (initialCapacity > 0) {
            this.elementData = new int[initialCapacity];
        }
        else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        }
        else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    public IntArrayBuffer()
    {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    public int size()
    {
        return size;
    }

    public boolean isEmpty()
    {
        return size == 0;
    }

    @Override
    public Iterator<Integer> iterator()
    {
        return new Iterator<Integer>()
        {
            private int i = 0;

            @Override
            public boolean hasNext()
            {
                return i < size;
            }

            @Override
            public Integer next()
            {
                return elementData[i++];
            }
        };
    }

    public void forEach(IntConsumer action)
    {
        requireNonNull(action);
        for (int i = 0; i < size; i++) {
            action.accept(elementData[i]);
        }
    }

    @Override
    public IntArrayBuffer clone()
    {
        try {
            IntArrayBuffer v = (IntArrayBuffer) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        }
        catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    public int[] toArray()
    {
        return Arrays.copyOfRange(elementData, 0, size);
    }

    private static int calculateCapacity(int[] elementData, int minCapacity)
    {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    private void ensureCapacityInternal(int minCapacity)
    {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

    private void ensureExplicitCapacity(int minCapacity)
    {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity)
    {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity)
    {
        if (minCapacity < 0) { // overflow
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public void trimToSize()
    {
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0)
                    ? EMPTY_ELEMENTDATA
                    : Arrays.copyOf(elementData, size);
        }
    }

    private String outOfBoundsMsg(int index)
    {
        return "Index: " + index + ", Size: " + this.size;
    }

    private void rangeCheck(int index)
    {
        if (index >= size) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    public int get(int index)
    {
        rangeCheck(index);

        return elementData[index];
    }

    public boolean add(int e)
    {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }

    public int set(int index, int element)
    {
        rangeCheck(index);

        int oldValue = elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    public boolean contains(int o)
    {
        for (int i = 0; i < size; i++) {
            if (elementData[i] == o) {
                return true;
            }
        }
        return false;
    }

    public boolean addAll(int[] c)
    {
        for (int i : c) {
            this.add(i);
        }
        return true;
    }

    public boolean addAll(Collection<? extends Integer> c)
    {
        for (int i : c) {
            this.add(i);
        }
        return true;
    }

    public void clear()
    {
        modCount++;
        for (int i = 0; i < size; i++) {
            elementData[i] = 0;
        }
        size = 0;
    }

    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }
        if (!(o instanceof IntArrayBuffer)) {
            return false;
        }
        IntArrayBuffer other = (IntArrayBuffer) o;
        if (size != other.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (elementData[i] != other.elementData[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 1;
        for (int i = 0; i < size; i++) {
            hashCode = 31 * hashCode + elementData[i];
        }
        return hashCode;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException
    {
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i = 0; i < size; i++) {
            s.writeInt(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException
    {
        elementData = EMPTY_ELEMENTDATA;

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            int capacity = calculateCapacity(elementData, size);
            //SharedSecrets.getJavaOISAccess().checkArray(s, int[].class, capacity);
            try {
                Method method = java.io.ObjectInputStream.class.getDeclaredMethod("checkArray", Class.class, int.class);
                method.setAccessible(true);
                method.invoke(s, int[].class, capacity);
            }
            catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                Platform.throwException(e);
            }

            ensureCapacityInternal(size);

            int[] a = elementData;
            // Read in all elements in the proper order.
            for (int i = 0; i < size; i++) {
                a[i] = s.readInt();
            }
        }
    }
}
