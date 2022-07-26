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
package com.github.harbby.gadtry.collection.tuple;

import com.github.harbby.gadtry.base.Streams;
import com.github.harbby.gadtry.base.Try;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class TupleTest
{
    private final Class<?>[] tupleClass = new Class[] {Tuple1.class, JTuple2.class, Tuple3.class,
            Tuple4.class, Tuple5.class, Tuple6.class, Tuple7.class, Tuple8.class, Tuple9.class};

    @Test
    public void tupleSizeTest()
            throws Exception
    {
        for (Class aClass : tupleClass) {
            Constructor[] constructors = aClass.getConstructors();
            Assert.assertEquals(1, constructors.length);
            //------------------
            int columnCnt = constructors[0].getParameterCount();
            Object[] array = Streams.range(1, columnCnt + 1).mapToObj(x -> x).toArray();
            Tuple tuple = (Tuple) constructors[0].newInstance(array);
            //---toString test
            String str = "(" + Arrays.stream(array).reduce((x, y) -> x + ", " + y).get() + ")";
            Assert.assertEquals(str, tuple.toString());
            System.out.println(tuple);
            //------fields length test
            Assert.assertEquals(columnCnt, tuple.getArity());
            //---- copy test
            Tuple copy = tuple.copy();
            Assert.assertEquals(tuple, copy);
            Assert.assertTrue(aClass.isInstance(copy));
            Assert.assertEquals(tuple.hashCode(), copy.hashCode());
            //---------------------------------------
            Assert.assertEquals(tuple, tuple);
            Assert.assertFalse(tuple.equals(null));
            Assert.assertFalse(tuple.equals(""));
        }
    }

    @Test
    public void tupleEqTest()
            throws Exception
    {
        for (Class aClass : tupleClass) {
            Constructor[] constructors = aClass.getConstructors();
            Assert.assertEquals(1, constructors.length);
            int columnCnt = constructors[0].getParameterCount();
            Object[] array = Streams.range(0, columnCnt).mapToObj(x -> x).toArray();
            Tuple tuple = (Tuple) constructors[0].newInstance(array);

            for (int i = 0; i < columnCnt; i++) {
                Object[] newArray = Arrays.copyOf(array, columnCnt);
                newArray[i] = -9999;
                Tuple tuple2 = (Tuple) constructors[0].newInstance(newArray);
                Assert.assertNotEquals(tuple, tuple2);
            }
        }
    }

    @Test
    public void getFieldTest()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        for (Class aClass : tupleClass) {
            Constructor[] constructors = aClass.getConstructors();
            Assert.assertEquals(1, constructors.length);
            int columnCnt = constructors[0].getParameterCount();
            Method of = aClass.getMethod("of", Streams.range(0, columnCnt).mapToObj(x -> Object.class).toArray(Class[]::new));
            String[] args = Streams.range(0, columnCnt).mapToObj(x -> "str" + (x + 1)).toArray(String[]::new);
            Tuple tuple = (Tuple) of.invoke(null, (Object[]) args);
            Method getField = aClass.getMethod("getField", int.class);
            for (int i = 1; i <= columnCnt; i++) {
                Assert.assertEquals("str" + i, getField.invoke(tuple, i));
                Assert.assertEquals("str" + i, aClass.getMethod("f" + i).invoke(tuple));
            }
            Try.of(() -> getField.invoke(tuple, columnCnt + 1))
                    .onSuccess(Assert::fail)
                    .matchException(InvocationTargetException.class, e -> {
                        IndexOutOfBoundsException e1 = (IndexOutOfBoundsException) e.getTargetException();
                        Assert.assertEquals(e1.getMessage(), String.valueOf(columnCnt + 1));
                    }).doTry();
        }
    }
}
