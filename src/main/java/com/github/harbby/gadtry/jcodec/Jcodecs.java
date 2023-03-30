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

import com.github.harbby.gadtry.jcodec.codecs.AnyArraySerializer;
import com.github.harbby.gadtry.jcodec.codecs.ArraySerializers;
import com.github.harbby.gadtry.jcodec.codecs.BooleanSerializer;
import com.github.harbby.gadtry.jcodec.codecs.ByteSerializer;
import com.github.harbby.gadtry.jcodec.codecs.CharSerializer;
import com.github.harbby.gadtry.jcodec.codecs.DoubleSerializer;
import com.github.harbby.gadtry.jcodec.codecs.FloatSerializer;
import com.github.harbby.gadtry.jcodec.codecs.IntSerializer;
import com.github.harbby.gadtry.jcodec.codecs.JavaSerializer;
import com.github.harbby.gadtry.jcodec.codecs.LengthIteratorSerializer;
import com.github.harbby.gadtry.jcodec.codecs.LongSerializer;
import com.github.harbby.gadtry.jcodec.codecs.MapSerializer;
import com.github.harbby.gadtry.jcodec.codecs.ShortSerializer;
import com.github.harbby.gadtry.jcodec.codecs.StringSerializer;
import com.github.harbby.gadtry.jcodec.codecs.VarIntSerializer;
import com.github.harbby.gadtry.jcodec.codecs.VarLongSerializer;
import com.github.harbby.gadtry.jcodec.codecs.VoidSerializer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class Jcodecs
{
    private Jcodecs() {}

    private static final Map<Class<?>, Serializer<?>> primitiveMap = new HashMap<>();

    static {
        primitiveMap.put(void.class, new VoidSerializer());
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
    public static <E> Serializer<E> javaEncoder()
    {
        return (Serializer<E>) new JavaSerializer<>();
    }

    @SuppressWarnings("unchecked")
    public static <E> Serializer<E> createPrimitiveEncoder(Class<E> aClass)
    {
        requireNonNull(aClass, "aClass is null");
        if (aClass.isPrimitive()) {
            return (Serializer<E>) primitiveMap.get(aClass);
        }
        else {
            throw new UnsupportedOperationException(" unknown type " + aClass);
        }
    }

    public static <K, V> MapSerializer<K, V> mapEncoder(Serializer<K> kSerializer, Serializer<V> vSerializer)
    {
        requireNonNull(kSerializer, "key Encoder is null");
        requireNonNull(vSerializer, "value Encoder is null");
        return new MapSerializer<>(kSerializer, vSerializer);
    }

    public static <V> AnyArraySerializer<V> arrayEncoder(Serializer<V> vSerializer, Class<V> aClass)
    {
        requireNonNull(vSerializer, "value Encoder is null");
        return new AnyArraySerializer<>(vSerializer, aClass);
    }

    public static <K, V> Tuple2Serializer<K, V> tuple2(Serializer<K> kSerializer, Serializer<V> vSerializer)
    {
        requireNonNull(kSerializer, "key Encoder is null");
        requireNonNull(vSerializer, "value Encoder is null");
        return new Tuple2Serializer.Tuple2KVSerializer<>(kSerializer, vSerializer);
    }

    public static <K> Tuple2Serializer<K, Void> tuple2OnlyKey(Serializer<K> kSerializer)
    {
        return tuple2(kSerializer, new VoidSerializer());
    }

    public static <E> Serializer<Iterator<E>> iteratorEncoder(Serializer<E> eSerializer)
    {
        return new LengthIteratorSerializer<>(eSerializer);
    }

    public static Serializer<String> string()
    {
        return new StringSerializer();
    }

    public static Serializer<Boolean> jBoolean()
    {
        return new BooleanSerializer();
    }

    public static Serializer<Byte> jByte()
    {
        return new ByteSerializer();
    }

    public static Serializer<Float> jFloat()
    {
        return new FloatSerializer();
    }

    public static Serializer<Short> jShort()
    {
        return new ShortSerializer();
    }

    public static Serializer<Character> jChar()
    {
        return new CharSerializer();
    }

    public static Serializer<Long> jLong()
    {
        return new LongSerializer();
    }

    public static Serializer<Integer> jInt()
    {
        return new IntSerializer();
    }

    public static Serializer<Integer> varInt(boolean optimizeNegativeNumber)
    {
        return new VarIntSerializer(optimizeNegativeNumber);
    }

    public static Serializer<Integer> varInt()
    {
        return new VarIntSerializer();
    }

    public static Serializer<Long> varLong()
    {
        return new VarLongSerializer();
    }

    public static Serializer<Long> varLong(boolean optimizeNegativeNumber)
    {
        return new VarLongSerializer(optimizeNegativeNumber);
    }

    public static Serializer<int[]> jIntArray()
    {
        return new ArraySerializers.IntArraySerializer();
    }

    public static Serializer<Double> jDouble()
    {
        return new DoubleSerializer();
    }
}
