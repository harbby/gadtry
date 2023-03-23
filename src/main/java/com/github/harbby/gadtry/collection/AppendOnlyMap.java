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

import com.github.harbby.gadtry.base.Maths;
import com.github.harbby.gadtry.function.Reducer;

import java.util.Arrays;

/**
 * This is a class that implements an append-only map data structure
 * An append-only map allows adding new key-value pairs or updating existing values
 * But it does not allow removing or replacing any key-value pairs
 */
public class AppendOnlyMap<K, V>
{
    /** This is a constant that defines the default load factor for the map
     * The load factor is the ratio of the number of elements to the capacity of the map
     * When the load factor exceeds a certain threshold, the map needs to be resized
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.7f;
    /**
     * This is an array that stores the keys and values of the map in alternating positions
     * For example, heap[0] is a key and heap[1] is its corresponding value
     */
    private final Object[] heap;
    /**
     * This is an interface that defines a reducer function for values of type V
     * A reducer function takes two values of type V and returns a single value of type V
     * The reducer function is used to update existing values in the map when appending new values
     */
    private final Reducer<V> reducer;

    /**
     * This is a mask that is used to calculate the index of a key in the heap array
     * The mask is equal to one less than the capacity of the map
     */
    private final int mask;
    /**
     * This is a threshold that determines when the map needs to be resized
     * The threshold is equal to the product of the default load factor and the capacity of the map
     */
    private final int threshold;

    // This is a special value that stores the value associated with a null key, if any
    private V nullKeyValue;
    // This is a boolean flag that indicates whether a null key has been defined in the map or not
    private boolean definedNullKey;
    // This is an integer that stores the current number of elements in the map
    private int size;

    /**
     * This is a constructor that creates an append-only map with a given reducer function and initial capacity
     * @param reducer The reducer function for values of type V
     * @param initialCapacity The initial capacity of the map
     */
    public AppendOnlyMap(Reducer<V> reducer, int initialCapacity)
    {
        this.reducer = reducer;
        // The capacity of the map is rounded up to the next power of two for efficiency reasons
        int capacity = Maths.nextPowerOfTwo(initialCapacity);
        this.heap = new Object[capacity << 1];
        this.mask = capacity - 1;
        this.threshold = (int) (DEFAULT_LOAD_FACTOR * capacity);
    }

    /**
     * This is a method that returns the current number of elements in the map
     * @return The current number of elements in the map
     */
    public int size()
    {
        return size;
    }

    /**
     * This is a method that returns true if the map is empty, false otherwise
     * @return True if the map is empty, false otherwise
     */
    public boolean isEmpty()
    {
        return size == 0;
    }

    /**
     * This is a method that appends a new key-value pair to the map or updates an existing value if the key already exists
     * @param key The key to append or update
     * @param value The value to append or update
     */
    public void append(K key, V value)
    {
        if (key == null) {
            if (definedNullKey) {
                // If a null key has been defined before, update its value using the reducer function
                this.nullKeyValue = reducer.reduce(nullKeyValue, value);
            }
            else {
                // If a null key has not been defined before, set its value and mark it as defined
                definedNullKey = true;
                this.nullKeyValue = value;
                size++;
            }
            return;
        }
        // Calculate the hash code of the key using its hashCode() method
        int hashCode = key.hashCode() & 0x7FFFFFFF;
        // Calculate the index of the key in the heap array using bitwise operations with mask
        int index = (hashCode & mask) << 1;
        int mod = heap.length - 1;
        do {
            // If there is no key at this index, store it along with its value and increment size
            if (heap[index] == null) {
                heap[index] = key;
                heap[index + 1] = value;
                size++;
                return;
            }
            else if (key.equals(heap[index])) {
                heap[index + 1] = reducer.reduce((V) heap[index + 1], value);
                return;
            }
            else {
                index = (index + 2) & mod;
            }
        }
        while (true);
    }

    /**
     * This is a method that checks if the map needs to be resized.
     * @return True if the map needs to be resized, false otherwise.
     */
    public boolean ensureCapacity()
    {
        return this.size > threshold;
    }

    /**
     * This is a method that clears all the key-value pairs in the map.
     */
    public void clear()
    {
        this.size = 0;
        this.definedNullKey = false;
        this.nullKeyValue = null;
        Arrays.fill(heap, null);
    }

    /**
     * This is a method that moves all the key-value pairs in the map to the beginning of the array.
     * This is done to make the map more compact and to facilitate sorting, copying, or storing to a file.
     * @return The compressed map as an array of objects.
     */
    public Object[] compress()
    {
        int nonNullKeySize = definedNullKey ? size - 1 : size;
        int movedNumber = 0;
        int nextIndex;
        for (int i = 0; i < heap.length; i += 2) {
            if (heap[i] != null) {
                nextIndex = movedNumber << 1;
                if (nextIndex != i) {
                    heap[nextIndex] = heap[i];
                    heap[nextIndex + 1] = heap[i + 1];
                }
                movedNumber++;
                if (movedNumber == nonNullKeySize) {
                    break;
                }
            }
        }
        if (definedNullKey) {
            nextIndex = movedNumber << 1;
            heap[nextIndex] = null;
            heap[nextIndex + 1] = nullKeyValue;
        }
        return heap;
    }
}
