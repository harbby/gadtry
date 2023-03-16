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
public class PairPriorityQueue<K, V>
{
    private final Comparator<K> keyComparator;

    private final Object[] elements;

    private int count;

    /**
     * Constructs a new priority queue with the given array, size and comparator.
     * The array is adjusted to a min-heap according to the comparator.
     *
     * @param elements The array to store the elements
     * @param count The number of elements in the array
     * @param keyComparator The comparator to compare the keys
     */
    public PairPriorityQueue(final Object[] elements, final int count, final Comparator<K> keyComparator)
    {
        this(elements, count, keyComparator, false);
    }

    /**
     * Constructs a new priority queue with the given array, size and comparator.
     * The array is adjusted to a min-heap according to the comparator.
     *
     * @param elements The array to store the elements
     * @param count The number of elements in the array
     * @param keyComparator The comparator to compare the keys
     * @param isSorted input elements is sorted by key
     */
    public PairPriorityQueue(final Object[] elements, final int count, final Comparator<K> keyComparator, boolean isSorted)
    {
        this.elements = elements;
        this.count = count;
        this.keyComparator = keyComparator;
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
        for (int i = ((count - 2) / 2 - 1) / 2; i >= 0; i--) {
            siftDown(elements, keyComparator, count, i * 2);
        }
    }

    /**
     * Returns the key of the head element of the queue without removing it.
     *
     * @return The key of the head element
     * @throws AssertionError if the queue is empty
     */
    @SuppressWarnings("unchecked")
    public K getHeapKey()
    {
        // ensureNotEmpty();
        return (K) elements[0];
    }

    /**
     * Returns the value of the head element of the queue without removing it.
     *
     * @return The value of the head element
     * @throws AssertionError if the queue is empty
     */
    @SuppressWarnings("unchecked")
    public V getHeapValue()
    {
        // ensureNotEmpty();
        return (V) elements[1];
    }

    private void ensureCapacity() // ensure enough space in the array
    {
        assert count + 2 <= elements.length;
    }

    private void ensureNotEmpty() // ensure enough space in the array
    {
        assert count > 0;
    }

    /**
     * Adds a new element to the queue with the given key and value.
     * The element is inserted according to its priority based on the comparator.
     *
     * @param k The key of the element
     * @param v The value of the element
     * @throws AssertionError if the queue is full
     */
    public void add(K k, V v)
    {
        // ensureCapacity();
        elements[count++] = k;
        elements[count++] = v;
        siftUp(elements, keyComparator, count - 2);
    }

    /**
     * Checks if the queue is empty or not.
     *
     * @return True if the queue is empty, false otherwise
     */
    public boolean isEmpty()
    {
        return count == 0;
    }

    /**
     * Resets the head element of the queue with the given key and value.
     * The element is adjusted according to its priority based on the comparator.
     *
     * @param k The new key of the head element
     * @param v The new value of the head element
     * @throws AssertionError if the queue is empty
     */
    public void replaceHead(K k, V v)
    {
        // ensureNotEmpty();
        elements[0] = k;
        elements[1] = v;
        // adjust the heap again
        siftDown(elements, keyComparator, count, 0);
    }

    /**
     * Removes the head element of the queue and returns it.
     * The next smallest element becomes the new head of the queue.
     *
     * @throws AssertionError if the queue is empty
     */
    public void removeHead()
    {
        // ensureNotEmpty();
        // remove the minimum element from the heap by swapping it with the last element and reducing the size
        swap(elements, 0, count - 2);
        swap(elements, 1, count - 1);
        count -= 2;

        // adjust the heap again
        siftDown(elements, keyComparator, count, 0);
    }

    /**
     * Returns the size of the queue, which is equal to
     * the number of elements in the array.
     *
     * @return The size of queue
     */
    public int size()
    {
        return count;
    }

    // helper method to move an element up in a min-heap until it satisfies the heap property
    @SuppressWarnings("unchecked")
    private void siftUp(Object[] heap, Comparator<K> comparator, int index)
    {
        while (index > 0) {
            int parent = (index / 2 - 1) / 2 * 2; // parent index
            if (comparator.compare((K) heap[parent], (K) heap[index]) > 0) {
                // swap with parent if smaller
                swap(heap, index, parent); // swap values
                swap(heap, index + 1, parent + 1); // swap indices

                index = parent; // update index
            }
            else {
                break; // break loop
            }
        }
    }

    // helper method to swap two elements in an array
    private void swap(Object[] array, int i, int j)
    {
        Object temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    // helper method to move an element down in a min-heap until it satisfies the heap property
    @SuppressWarnings("unchecked")
    private void siftDown(Object[] heap, Comparator<K> comparator, int size, int index)
    {
        while (true) {
            int leftChild = 2 * index + 2; //left child index
            int rightChild = 2 * index + 4; //right child index
            int smallest = index; //smallest element index

            if (leftChild < size && comparator.compare((K) heap[leftChild], (K) heap[smallest]) < 0) {
                smallest = leftChild; //update smallest
            }

            if (rightChild < size && comparator.compare((K) heap[rightChild], (K) heap[smallest]) < 0) {
                smallest = rightChild; //update smallest
            }

            if (smallest != index) { //if smallest is not equal to index then swap
                swap(heap, index, smallest); //swap values
                swap(heap, index + 1, smallest + 1); //swap indices
                index = smallest; //update index
            }
            else {
                break; //break loop
            }
        }
    }
}
