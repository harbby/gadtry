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

import java.util.Comparator;

/**
 * A generic class that implements a priority queue based on an array.
 * It can store a pair of key-value elements, where the key is used to compare the priority of the elements, and the value is used to store data.
 * A comparator can be used to customize the comparison rule of the keys.
 * Elements can be added, removed or reset at the head of the queue.
 * <p>
 * &lt;K&gt; The type of the key
 * &lt;V&gt; The type of the value
 *
 * @author harbby
 * @version 1.0
 * @see java.util.PriorityQueue
 */
public class FastPriorityQueue
{
    private final DataFormat<?> format;

    public abstract static class DataFormat<K>
    {
        protected int size;

        protected DataFormat(int initSize)
        {
            this.size = initSize;
        }

        // helper method to swap two elements in an array
        public abstract void swap(int i1, int i2);

        public abstract int compare(int i1, int i2);

        public abstract K getHead();
    }

    public static final class PairDataFormat<K, V>
            extends DataFormat<K>
    {
        private final Comparator<K> keyComparator;
        private final Object[] heap;

        public PairDataFormat(Object[] heap, int initSize, Comparator<K> keyComparator)
        {
            super(initSize);
            this.keyComparator = keyComparator;
            this.heap = heap;
            this.size = initSize;
        }

        @Override
        public void swap(int id1, int id2)
        {
            int i1 = id1 << 1;
            int i2 = id2 << 1;
            Object temp = heap[i1];
            heap[i1] = heap[i2];
            heap[i2] = temp;
            // swap v-----
            i1++;
            i2++;
            temp = heap[i1];
            heap[i1] = heap[i2];
            heap[i2] = temp;
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compare(int i1, int i2)
        {
            return keyComparator.compare((K) heap[i1 << 1], (K) heap[i2 << 1]);
        }

        public void add(K k, V v, FastPriorityQueue priorityQueue)
        {
            // ensureCapacity();
            heap[size << 1] = k;
            heap[(size << 1) + 1] = v;
            size++;
            priorityQueue.siftUp(this.size - 1);
        }

        public void replaceHead(K k, V v, FastPriorityQueue priorityQueue)
        {
            heap[0] = k;
            heap[1] = v;
            priorityQueue.siftDown(0);
        }

        @SuppressWarnings("unchecked")
        @Override
        public K getHead()
        {
            return (K) heap[0];
        }

        @SuppressWarnings("unchecked")
        public V getHeadValue()
        {
            return (V) heap[1];
        }

        private void ensureCapacity()
        {
            assert this.size * 2 + 2 <= heap.length;
        }

        private void ensureNotEmpty()
        {
            assert this.size > 0;
        }
    }

    /**
     * Constructs a new priority queue with the given array, size and comparator.
     * The array is adjusted to a min-heap according to the comparator.
     *
     * @param dataFormat The array to store the elements
     */
    public FastPriorityQueue(final DataFormat<?> dataFormat)
    {
        this(dataFormat, false);
    }

    /**
     * Constructs a new priority queue with the given array, size and comparator.
     * The array is adjusted to a min-heap according to the comparator.
     *
     * @param dataFormat The array to store the elements
     * @param isSorted   input elements is sorted by key
     */
    public FastPriorityQueue(final DataFormat<?> dataFormat, boolean isSorted)
    {
        this.format = dataFormat;
        if (!isSorted) {
            // adjust the array to a min-heap
            this.heapify();
        }
    }

    /**
     * Establishes the heap invariant (described above) in the entire tree,
     * assuming nothing about the order of the elements prior to the call.
     * This classic algorithm due to Floyd (1964) is known to be O(size).
     */
    private void heapify()
    {
        for (int i = (format.size - 2) / 2; i >= 0; i--) {
            siftDown(i);
        }
    }

    /**
     * Adds a new element to the queue with the given key and value.
     * The element is inserted according to its priority based on the comparator.
     *
     * @param k The key of the element
     * @throws AssertionError if the queue is full
     * @see PairDataFormat#add(Object)
     */
    public void add(Object k)
    {
        throw new UnsupportedOperationException();
        //1. format.heap[size] = k
        //2. format.size++;
        //3. siftUp(format.size - 1);
    }

    /**
     * Resets the head element of the queue with the given key and value.
     * The element is adjusted according to its priority based on the comparator.
     *
     * @param k The new key of the head element
     * @throws AssertionError if the queue is empty
     * @see PairDataFormat#replaceHead(Object, Object, FastPriorityQueue)
     */
    public void replaceHead(Object k)
    {
        throw new UnsupportedOperationException();
        // 1. ensureNotEmpty();
        // 2. format.replaceHead(k, v);
        // adjust the heap again
        // 3. siftDown(0);
    }

    /**
     * Checks if the queue is empty or not.
     *
     * @return True if the queue is empty, false otherwise
     */
    public boolean isEmpty()
    {
        return format.size == 0;
    }

    /**
     * Removes the head element of the queue and returns it.
     * The next smallest element becomes the new head of the queue.
     *
     * @throws AssertionError if the queue is empty
     */
    public final void removeHead()
    {
        // ensureNotEmpty();
        // remove the minimum element from the heap by swapping it with the last element and reducing the size
        format.swap(0, format.size - 1);
        format.size--;

        // adjust the heap again
        siftDown(0);
    }

    /**
     * Returns the size of the queue, which is equal to
     * the number of elements in the array.
     *
     * @return The size of queue
     */
    public int size()
    {
        return format.size;
    }

    // helper method to move an element up in a min-heap until it satisfies the heap property
    public final void siftUp(int index)
    {
        while (index > 0) {
            int parent = (index - 1) / 2; // parent index
            if (format.compare(parent, index) > 0) {
                // swap with parent if smaller
                format.swap(index, parent);
                index = parent; // update index
            }
            else {
                break; // break loop
            }
        }
    }

    // helper method to move an element down in a min-heap until it satisfies the heap property
    public final void siftDown(int index)
    {
        int size = format.size;
        while (true) {
            int leftChild = 2 * index + 1; //left child index
            int rightChild = 2 * index + 2; //right child index
            int smallest = index; //smallest element index

            if (leftChild < size && format.compare(leftChild, smallest) < 0) {
                smallest = leftChild; //update smallest
            }

            if (rightChild < size && format.compare(rightChild, smallest) < 0) {
                smallest = rightChild; //update smallest
            }

            if (smallest != index) { //if smallest is not equal to index then swap
                format.swap(index, smallest);
                index = smallest; //update index
            }
            else {
                break; //break loop
            }
        }
    }
}
