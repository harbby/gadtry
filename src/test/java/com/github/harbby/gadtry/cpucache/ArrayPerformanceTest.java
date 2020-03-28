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

import java.util.Random;

public class ArrayPerformanceTest
{
    private ArrayPerformanceTest() {}

    private static final int COUNT = 1024 * 1024 * 16;
    private static Object[] objectArr = new Object[COUNT];  //性能差
    private static int[] intArray = new int[COUNT];
    private static boolean[] booleanArray = new boolean[COUNT];

    public static void main(String[] args)
    {
        Random random = new Random();
        for (int i = 0; i < COUNT; i++) {
            objectArr[i] = i;
        }

        for (int i = 0; i < COUNT; i++) {
            intArray[i] = i;
        }

        for (int i = 0; i < COUNT; i++) {
            //booleanArray[i] = ((i % 2) == 0);      //分析预测成功
            booleanArray[i] = random.nextBoolean();  //分支预测失败
        }

        for (int i = 0; i < 20; i++) {
            objectArray(i); //chache 100%
        }
        System.out.println("*********************************************************************");
        for (int i = 0; i < 20; i++) {
            intArray(i);  // 0%
        }
        System.out.println("*********************************************************************");
        for (int i = 0; i < 20; i++) {
            booleanArray(i);  // 0%
        }
    }

    private static void objectArray(int number)
    {
        long sum = 0L;
        long marked = System.currentTimeMillis();

        for (int i = 0; i < COUNT; i++) {
            sum += (int) objectArr[i];
        }
        System.out.println("objectArray test " + number + " Loop times:" + (System.currentTimeMillis() - marked) + "ms");
    }

    private static void intArray(int number)
    {
        long sum = 0L;
        long marked = System.currentTimeMillis();

        for (int row = 0; row < COUNT; row += 1) {
            sum += intArray[row];
        }
        System.out.println("intArray test " + number + " Loop times:" + (System.currentTimeMillis() - marked) + "ms");
    }

    private static void booleanArray(int number)
    {
        long sum = 0L;
        long marked = System.currentTimeMillis();

        for (int row = 0; row < COUNT; row += 1) {
            if (booleanArray[row]) {
                sum += 1;
            }
            else {
                sum += -1;
            }
        }
        System.out.println("booleanArray test " + number + " Loop times:" + (System.currentTimeMillis() - marked) + "ms");
    }
}
