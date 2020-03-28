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

import com.github.harbby.gadtry.base.Iterators;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class NoNextMode
{
    private NoNextMode() {}

    private static final Function<Integer, Integer> mapFunc1 = (in) -> in + 1;
    private static final Function<Object[], Integer> mapFunc11 = (in) -> (Integer) in[0] + 1;

    private static final Function<Integer, Integer> mapFunc2 = (in) -> in % 2;
    private static final Function<Integer, Boolean> mapFun3 = (in) -> in % 2 == 0;  //if
    private static final Function<Integer, Double> mapFunc4 = (in) -> in / 3.0;

    private static final int[] money2 = new int[1024];
    private static final Object[][] money1 = new Object[1024][1];

    public static void main(String[] args)
    {
        Random random = new Random();
        for (int i = 0; i < 1024; i++) {
            int value = random.nextInt(100);
            money2[i] = value;
            money1[i] = new Object[] {value};
        }
        final int batchNumber = 10240;
        //----------------------------------------------
        for (int i = 0; i < 100; i++) {
            long marked = System.currentTimeMillis();
            for (int t = 0; t < batchNumber; t++) {
                sparkv();
            }
            System.out.println("sparkv test " + i + " Loop times:" + (System.currentTimeMillis() - marked) + "ms");
        }
        System.out.println("*********************************************************************");

        for (int i = 0; i < 100; i++) {
            long marked = System.currentTimeMillis();
            for (int t = 0; t < batchNumber; t++) {
                hiveTest1();
            }
            System.out.println("hiveTest1 test " + i + " Loop times:" + (System.currentTimeMillis() - marked) + "ms");
        }
        System.out.println("*********************************************************************");
        for (int i = 0; i < 100; i++) {
            long marked = System.currentTimeMillis();
            for (int t = 0; t < batchNumber; t++) {
                rowTest1();
            }
            System.out.println("rowTest1 test " + i + " Loop times:" + (System.currentTimeMillis() - marked) + "ms");
        }
    }

    public static void sparkv()
    {
        for (int i = 0; i < money2.length; i++) {
            int out1 = money2[i] + 1;              // Integer out1 = mapFunc1.apply(money2[i]);
            int out2 = out1 % 2;               //  Integer out2 = mapFunc2.apply(out1);
            if (out2 % 2 == 0) {              //if (mapFun3.apply(out2)) {
                double out3 = out2 / 3.0;     //Double out3 = mapFunc4.apply(out2);
                //other Operator
                //write sink out3
                if (out3 > 9999999) {
                    //这里模拟空sink, 防止被javac优化掉
                    System.out.println(out3);
                }
            }
        }
    }

    public static void hiveTest1()
    {
        int[] out1 = new int[money2.length];
        for (int i = 0; i < money2.length; i++) {
            out1[i] = money2[i] + 1;            // mapFunc1.apply(money2[i]);
        }
        //
        out1 = Arrays.asList(out1).iterator().next();
        int[] out2 = new int[money2.length];
        for (int i = 0; i < out1.length; i++) {
            out2[i] = out1[i] % 2;                //mapFunc2.apply(out1[i]);
        }

        out2 = Arrays.asList(out1).iterator().next();
        int[] filtered = new int[money2.length];
        int filteredSize = 0;
        for (int i = 0; i < out2.length; i++) {
            if (out2[i] % 2 == 0) {                    // if (mapFun3.apply(out2[i])) {
                filtered[filteredSize++] = out2[i];
            }
        }

        filtered = Arrays.asList(filtered).iterator().next();
        double[] out3 = new double[filteredSize];
        for (int i = 0; i < filteredSize; i++) {
            out3[i] = filtered[i] / 3.0;          //mapFunc4.apply(filtered[i]);
        }

        out3 = Arrays.asList(out3).iterator().next();
        for (int i = 0; i < out3.length; i++) {
            if (out3[i] > 9999999) {
                //这里模拟空sink, 防止被javac优化掉
                System.out.println(out3[i]);
            }
        }
    }

    /**
     * 火山迭代
     */
    public static void rowTest1()
    {
        Iterator<Object[]> input = Stream.of(money1).iterator();

        Iterator<Integer> out1 = Iterators.map(input, mapFunc11);
        Iterator<Integer> out2 = Iterators.map(out1, mapFunc2);
        Iterator<Integer> filtered = Iterators.filter(out2, mapFun3::apply);
        Iterator<Double> out3 = Iterators.map(filtered, mapFunc4);

        //other Operator
        //action
        // write sink out3
        while (out3.hasNext()) {
            //write sink
            if (out3.next() > 9999999) {
                //这里模拟空sink, 防止被javac优化掉
                System.out.println(out3);
            }
        }
    }
}
