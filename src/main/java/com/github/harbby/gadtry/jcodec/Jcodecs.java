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
package com.github.harbby.gadtry.jcodec;

import com.github.harbby.gadtry.jcodec.codecs.AnyArrayJcodec;
import com.github.harbby.gadtry.jcodec.codecs.ArrayJCodecs;
import com.github.harbby.gadtry.jcodec.codecs.AsciiStringJcodec;
import com.github.harbby.gadtry.jcodec.codecs.BooleanJcodec;
import com.github.harbby.gadtry.jcodec.codecs.ByteJcodec;
import com.github.harbby.gadtry.jcodec.codecs.CharJcodec;
import com.github.harbby.gadtry.jcodec.codecs.DoubleJcodec;
import com.github.harbby.gadtry.jcodec.codecs.FloatJcodec;
import com.github.harbby.gadtry.jcodec.codecs.IntJcodec;
import com.github.harbby.gadtry.jcodec.codecs.JavaJcodec;
import com.github.harbby.gadtry.jcodec.codecs.LengthIteratorJcodec;
import com.github.harbby.gadtry.jcodec.codecs.LongJcodec;
import com.github.harbby.gadtry.jcodec.codecs.MapJcodec;
import com.github.harbby.gadtry.jcodec.codecs.ShortJcodec;
import com.github.harbby.gadtry.jcodec.codecs.StringJcodec;
import com.github.harbby.gadtry.jcodec.codecs.VarIntJcodec;
import com.github.harbby.gadtry.jcodec.codecs.VarLongJcodec;
import com.github.harbby.gadtry.jcodec.codecs.VoidJcodec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class Jcodecs
{
    private Jcodecs() {}

    private static final Map<Class<?>, Jcodec<?>> primitiveMap = new HashMap<>();

    static {
        primitiveMap.put(void.class, new VoidJcodec());
        primitiveMap.put(byte.class, jByte());
        primitiveMap.put(boolean.class, jBoolean());
        primitiveMap.put(char.class, jChar());
        primitiveMap.put(short.class, jChar());
        primitiveMap.put(int.class, jInt());
        primitiveMap.put(float.class, jFloat());
        primitiveMap.put(long.class, jLong());
        primitiveMap.put(double.class, jDouble());
    }

    @SuppressWarnings("unchecked")
    public static <E> Jcodec<E> javaEncoder()
    {
        return (Jcodec<E>) new JavaJcodec<>();
    }

    @SuppressWarnings("unchecked")
    public static <E> Jcodec<E> createPrimitiveEncoder(Class<E> aClass)
    {
        requireNonNull(aClass, "aClass is null");
        if (aClass.isPrimitive()) {
            return (Jcodec<E>) primitiveMap.get(aClass);
        }
        else {
            throw new UnsupportedOperationException(" unknown type " + aClass);
        }
    }

    public static <K, V> MapJcodec<K, V> mapEncoder(Jcodec<K> kJcodec, Jcodec<V> vJcodec)
    {
        requireNonNull(kJcodec, "key Encoder is null");
        requireNonNull(vJcodec, "value Encoder is null");
        return new MapJcodec<>(kJcodec, vJcodec);
    }

    public static <V> AnyArrayJcodec<V> arrayEncoder(Jcodec<V> vJcodec, Class<V> aClass)
    {
        requireNonNull(vJcodec, "value Encoder is null");
        return new AnyArrayJcodec<>(vJcodec, aClass);
    }

    public static <K, V> Tuple2Jcodec<K, V> tuple2(Jcodec<K> kJcodec, Jcodec<V> vJcodec)
    {
        requireNonNull(kJcodec, "key Encoder is null");
        requireNonNull(vJcodec, "value Encoder is null");
        return new Tuple2Jcodec.Tuple2KVJcodec<>(kJcodec, vJcodec);
    }

    public static <K> Tuple2Jcodec<K, Void> tuple2OnlyKey(Jcodec<K> kJcodec)
    {
        return tuple2(kJcodec, new VoidJcodec());
    }

    public static <E> Jcodec<Iterator<E>> iteratorEncoder(Jcodec<E> eJcodec)
    {
        return new LengthIteratorJcodec<>(eJcodec);
    }

    public static Jcodec<String> asciiString()
    {
        return new AsciiStringJcodec();
    }

    public static Jcodec<String> string()
    {
        return new StringJcodec();
    }

    public static Jcodec<Boolean> jBoolean()
    {
        return new BooleanJcodec();
    }

    public static Jcodec<Byte> jByte()
    {
        return new ByteJcodec();
    }

    public static Jcodec<Float> jFloat()
    {
        return new FloatJcodec();
    }

    public static Jcodec<Short> jShort()
    {
        return new ShortJcodec();
    }

    public static Jcodec<Character> jChar()
    {
        return new CharJcodec();
    }

    public static Jcodec<Long> jLong()
    {
        return new LongJcodec();
    }

    public static Jcodec<Integer> jInt()
    {
        return new IntJcodec();
    }

    public static Jcodec<Integer> varInt(boolean optimizeNegativeNumber)
    {
        return new VarIntJcodec(optimizeNegativeNumber);
    }

    public static Jcodec<Integer> varInt()
    {
        return new VarIntJcodec();
    }

    public static Jcodec<Long> varLong()
    {
        return new VarLongJcodec();
    }

    public static Jcodec<Long> varLong(boolean optimizeNegativeNumber)
    {
        return new VarLongJcodec(optimizeNegativeNumber);
    }

    public static Jcodec<int[]> jIntArray()
    {
        return new ArrayJCodecs.IntArrayJcodec();
    }

    public static Jcodec<Double> jDouble()
    {
        return new DoubleJcodec();
    }
}
