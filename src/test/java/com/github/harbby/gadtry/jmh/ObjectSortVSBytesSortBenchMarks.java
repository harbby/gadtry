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
package com.github.harbby.gadtry.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Benchmark                                        (size)   Mode  Cnt      Score      Error   Units
 * ObjectSortVSBytesSortBenchMarks.sortByteArray        10  thrpt    5  29286.245 ±   99.037  ops/ms
 * ObjectSortVSBytesSortBenchMarks.sortByteArray       100  thrpt    5   3053.511 ±   16.759  ops/ms
 * ObjectSortVSBytesSortBenchMarks.sortByteArray      1000  thrpt    5    279.532 ±    0.929  ops/ms
 * ObjectSortVSBytesSortBenchMarks.sortByteArray2       10  thrpt    5  52917.614 ± 1004.581  ops/ms
 * ObjectSortVSBytesSortBenchMarks.sortByteArray2      100  thrpt    5   5677.172 ±   86.064  ops/ms
 * ObjectSortVSBytesSortBenchMarks.sortByteArray2     1000  thrpt    5    615.640 ±   16.115  ops/ms
 * ObjectSortVSBytesSortBenchMarks.sortIntegerList      10  thrpt    5  55235.249 ±  364.555  ops/ms
 * ObjectSortVSBytesSortBenchMarks.sortIntegerList     100  thrpt    5   7900.422 ±  209.066  ops/ms
 * ObjectSortVSBytesSortBenchMarks.sortIntegerList    1000  thrpt    5    732.763 ±  207.371  ops/ms
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Fork(jvmArgsPrepend = {}, value = 1)
public class ObjectSortVSBytesSortBenchMarks
{
    private List<Integer> integerList;
    private byte[] byteArray;
    private List<Integer> indexList;

    @Param({"1024"})
    private int size;

    @Setup
    public void setup()
    {
        integerList = ThreadLocalRandom.current().ints(size).boxed().collect(Collectors.toList());
        this.byteArray = new byte[size << 2];
        indexList = new ArrayList<>(size);
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        for (int i = 0; i < size; i++) {
            int value = integerList.get(i);
            byteBuffer.putInt(value);
            indexList.add(i << 2);
        }
    }

    @Benchmark
    public Integer sortIntegerList()
    {
        integerList.sort(Integer::compareTo);
        return integerList.get(0);
    }

    @Benchmark
    public Integer sortByteArray()
    {
        indexList.sort((x, y) -> Arrays.compare(byteArray, x, x + 4, byteArray, y, y + 4));
        return integerList.get(0);
    }

    @Benchmark
    public Integer sortByteArray2()
    {
        indexList.sort((x, y) -> {
            for (int i = 0; i < 4; i++) {
                byte b1 = byteArray[x];
                byte b2 = byteArray[y];
                if (b1 > b2) {
                    return 1;
                }
                else if (b1 < b2) {
                    return -1;
                }
            }
            return 0;
        });
        return integerList.get(0);
    }

//    public static void main(String[] args)
//    {
//        ObjectSortVSBytesSortBenchMarks benchMarks = new ObjectSortVSBytesSortBenchMarks();
//        benchMarks.size = 10;
//        benchMarks.setup();
//        benchMarks.sortIntegerList();
//        benchMarks.sortByteArray2();
//        int index = benchMarks.indexList.get(0);
//        int out = ByteBuffer.wrap(benchMarks.byteArray).getInt(index);
//        if (out != benchMarks.integerList.get(0)) {
//            throw new IllegalStateException("sort code error");
//        }
//    }
}
