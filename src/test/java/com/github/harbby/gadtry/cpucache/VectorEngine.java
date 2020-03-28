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
package com.github.harbby.gadtry.cpucache;

import com.github.harbby.gadtry.collection.tuple.Tuple8;

/**
 * 缓存行
 * Cache是由很多个cache line组成的。每个cache line通常是64字节，并且它有效地引用主内存中的一块儿地址。
 * 一个Java的long类型变量是8字节，因此在一个缓存行中可以存8个long类型的变量。 16个int
 **/
public class VectorEngine
{
    private VectorEngine() {}

    private static final Tuple8<Long, Long, Long, Long, Long, Long, Long, Long>[] arr = new Tuple8[1024 * 1024];

    private static final VectorizedColumnBatch batchColumns = new VectorizedColumnBatch();

    public class Row
    {
        Object[] values;
    }

    public static class LongVector
    {
        double[] vector = new double[1024 * 1024];  //1024行
    }

    public static class VectorizedColumnBatch
    {
        public int numCols = 8;  //8列
        public LongVector[] cols = new LongVector[8];  //data
        public int size = 1024 * 1024;  //和hive默认值一样
    }

    public static void main(String[] args)
    {
        //填充行存数据,模拟行存数据
        for (int i = 0; i < 1024 * 1024; i++) {
            arr[i] = new Tuple8<>((long) i, (long) i, (long) i, (long) i, (long) i, (long) i, (long) i, (long) i);
        }
        //-----模拟列存数据
        for (int j = 0; j < 8; j++) {
            LongVector longVector = new LongVector();
            batchColumns.cols[j] = longVector;
        }
        for (int column = 0; column < 8; column++) {  // 8列
            LongVector longVector = batchColumns.cols[column];
            for (int row = 0; row < 1024 * 1024; row++) {
                longVector.vector[row] = (long) row;
            }
        }

        for (int i = 0; i < 20; i++) {
            columnBatchRead(i); //chache 100%
        }
        System.out.println("*********************************************************************");
        for (int i = 0; i < 20; i++) {
            rowRead(i);  // 0%
        }
    }

    private static void columnBatchRead(int number)
    {
        long sum = 0L;
        long marked = System.currentTimeMillis();

        LongVector longVector = batchColumns.cols[0];  //取第一列
        for (int i = 0; i < 1024 * 1024; i++) {
            sum += longVector.vector[i];
        }
        System.out.println("columnBatchRead test " + number + " Loop times:" + (System.currentTimeMillis() - marked) + "ms");
    }

    private static void rowRead(int number)
    {
        long sum = 0L;
        long marked = System.currentTimeMillis();
        for (int row = 0; row < 1024 * 1024; row += 1) {
            sum += arr[row].<Long>getField(1);
        }
        System.out.println("rowsRead test " + number + " Loop times:" + (System.currentTimeMillis() - marked) + "ms");
    }
}
