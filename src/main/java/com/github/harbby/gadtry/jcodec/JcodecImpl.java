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

import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.collection.CuckooStashHashMap;
import com.github.harbby.gadtry.jcodec.codecs.ArraySerializers;
import com.github.harbby.gadtry.jcodec.codecs.BooleanSerializer;
import com.github.harbby.gadtry.jcodec.codecs.ByteSerializer;
import com.github.harbby.gadtry.jcodec.codecs.CharSerializer;
import com.github.harbby.gadtry.jcodec.codecs.DateSerializer;
import com.github.harbby.gadtry.jcodec.codecs.DoubleSerializer;
import com.github.harbby.gadtry.jcodec.codecs.EnumSerializer;
import com.github.harbby.gadtry.jcodec.codecs.FloatSerializer;
import com.github.harbby.gadtry.jcodec.codecs.JavaInternals;
import com.github.harbby.gadtry.jcodec.codecs.ListSerializer;
import com.github.harbby.gadtry.jcodec.codecs.MapSerializer;
import com.github.harbby.gadtry.jcodec.codecs.ObjectArraySerializer;
import com.github.harbby.gadtry.jcodec.codecs.ShortSerializer;
import com.github.harbby.gadtry.jcodec.codecs.StringSerializer;
import com.github.harbby.gadtry.jcodec.codecs.TreeMapSerializer;
import com.github.harbby.gadtry.jcodec.codecs.VarIntSerializer;
import com.github.harbby.gadtry.jcodec.codecs.VarLongSerializer;
import com.github.harbby.gadtry.jcodec.codecs.VoidSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.github.harbby.gadtry.StaticAssert.DEBUG;
import static java.util.Objects.requireNonNull;

final class JcodecImpl
        implements Jcodec
{
    private static final int NULL = 0;
    private static final int NOT_NULL = 1;

    private final Map<Class<?>, SerializerWrapper> registerMap = new CuckooStashHashMap<>();
    private final List<SerializerWrapper> registerIndex = new ArrayList<>();
    private int nextClassId;

    private final Map<Class<?>, Integer> classNameWriteCache = new CuckooStashHashMap<>();
    private final List<SerializerWrapper> classNameReadCache = new ArrayList<>();

    private final SerializerManager serializerManager = new SerializerManager();

    JcodecImpl()
    {
        // Primitives
        register(byte.class, ByteSerializer.class);
        register(boolean.class, BooleanSerializer.class);
        register(short.class, ShortSerializer.class);
        register(char.class, CharSerializer.class);
        register(float.class, FloatSerializer.class);
        register(int.class, VarIntSerializer.class);
        register(long.class, VarLongSerializer.class);
        register(double.class, DoubleSerializer.class);
        register(String.class, StringSerializer.class);
        // array
        addSerializer(byte[].class, ArraySerializers.ByteArraySerializer.class);
        addSerializer(boolean[].class, ArraySerializers.BooleanArraySerializer.class);
        addSerializer(short[].class, ArraySerializers.ShortArraySerializer.class);
        addSerializer(char[].class, ArraySerializers.CharArraySerializer.class);
        addSerializer(int[].class, ArraySerializers.IntArraySerializer.class);
        addSerializer(float[].class, ArraySerializers.FloatArraySerializer.class);
        addSerializer(long[].class, ArraySerializers.LongArraySerializer.class);
        addSerializer(double[].class, ArraySerializers.DoubleArraySerializer.class);
        addSerializer(String[].class, ArraySerializers.StringArraySerializer.class);
        addSerializer(Void.class, VoidSerializer.class);
        // other
        addSerializer(Arrays.asList(null, null).getClass(), JavaInternals.ArrayList.class);
        addSerializer(Collections.singletonMap(null, null).getClass(), JavaInternals.SingletonMapSerializer.class);
        addSerializer(TreeMap.class, TreeMapSerializer.class);
        addSerializer(Map.class, MapSerializer.class);
        addSerializer(List.class, ListSerializer.class);
        addSerializer(Date.class, DateSerializer.class);
        addSerializer(Enum.class, EnumSerializer.class);
        addSerializer(Object[].class, ObjectArraySerializer.class);
    }

    @Override
    public <T> T newInstance(Class<? extends T> type)
    {
        try {
            Constructor<? extends T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        catch (Exception e) {
            throw new JcodecException("new Instance failed", e);
        }
    }

    @Override
    public <T> void register(Class<T> typeClass, Class<? extends Serializer<T>> serializerClass)
    {
        requireNonNull(serializerClass, "typeClass is null");
        Serializer<T> serializer;
        try {
            Constructor<? extends Serializer<T>> constructor = serializerClass.getConstructor();
            serializer = constructor.newInstance();
        }
        catch (Exception e) {
            throw new JcodecException("jcodec failed", e);
        }
        this.register(typeClass, serializer);
    }

    @Override
    public <T> void register(Class<T> typeClass, Serializer<T> serializer)
    {
        requireNonNull(typeClass, "typeClass is null");
        if (serializer == null) {
            return;
        }
        SerializerWrapper old = registerMap.get(typeClass);
        SerializerWrapper wrapper;
        if (old != null && old.getId() != -1) {
            wrapper = new SerializerWrapper(old.getId(), typeClass, serializer);
            registerIndex.set(old.getId(), wrapper);
        }
        else {
            int classId = nextClassId++;
            wrapper = new SerializerWrapper(classId, typeClass, serializer);
            assert !DEBUG || classId == registerIndex.size();
            registerIndex.add(wrapper);
        }
        registerMap.put(typeClass, wrapper);
        if (typeClass.isPrimitive()) {
            Class<?> wrapperClass = JavaTypes.getWrapperClass(typeClass);
            registerMap.put(wrapperClass, wrapper);
        }
    }

    @Override
    public <T> void register(Class<T> typeClass)
    {
        requireNonNull(typeClass, "typeClass is null");
        SerializerWrapper wrapper = registerMap.get(typeClass);
        if (wrapper != null) {
            return;
        }
        if (typeClass.isInterface() || (!typeClass.isArray() && Modifier.isAbstract(typeClass.getModifiers()))) {
            throw new JcodecException("type class must be entity class: " + typeClass);
        }
        Serializer<T> serializer = createSerializer(typeClass);
        this.register(typeClass, serializer);
    }

    private SerializerWrapper getOrCacheSerializerWrapper(Class<?> typeClass)
    {
        SerializerWrapper wrapper = registerMap.get(typeClass);
        if (wrapper != null) {
            return wrapper;
        }
        Serializer<?> serializer = createSerializer(typeClass);
        wrapper = new SerializerWrapper(-1, typeClass, serializer);
        registerMap.put(typeClass, wrapper);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Serializer<T> getSerializer(Class<?> typeClass)
    {
        SerializerWrapper wrapper = registerMap.get(typeClass);
        if (wrapper != null) {
            return wrapper.getSerializer();
        }
        return (Serializer<T>) createSerializer(typeClass);
    }

    @Override
    public <T> void addSerializer(Class<T> typeClass, Class<? extends Serializer> serializerClass)
    {
        serializerManager.addSerializer(typeClass, serializerClass);
    }

    @SuppressWarnings({"unchecked"})
    private <T> Serializer<T> createSerializer(Class<T> typeClass)
    {
        JcodecSerializer jcodecSerializer = typeClass.getAnnotation(JcodecSerializer.class);
        if (jcodecSerializer != null) {
            serializerManager.checkSerializer(typeClass, jcodecSerializer.value());
            return (Serializer<T>) serializerManager.makeSerializer(this, typeClass, jcodecSerializer.value());
        }
        Serializer<T> serializer = (Serializer<T>) serializerManager.makeSerializer(this, typeClass);
        if (serializer != null) {
            return serializer;
        }
        if (typeClass.isInterface() || Modifier.isAbstract(typeClass.getModifiers())) {
            throw new JcodecException("typeClass is Interface or Abstract class");
            // return null;
        }
        return FieldSerializerFactory.makeSerializer(this, typeClass);
    }

    public SerializerWrapper writeClass(OutputView outputView, Class<?> typeClass)
    {
        if (typeClass == null) {
            outputView.writeVarInt(NULL, true);
            return null;
        }
        SerializerWrapper wrapper = getOrCacheSerializerWrapper(typeClass);
        if (wrapper.getId() == -1) {
            outputView.writeVarInt(1, true);
            Integer id = classNameWriteCache.get(typeClass);
            if (id != null) {
                outputView.writeVarInt(id, true);
            }
            else {
                id = classNameWriteCache.size();
                classNameWriteCache.put(typeClass, id);
                outputView.writeVarInt(id, true);
                outputView.writeString(typeClass.getName());
            }
        }
        else {
            outputView.writeVarInt(wrapper.getId() + 2, true);
        }
        return wrapper;
    }

    public SerializerWrapper readClass(InputView inputView)
    {
        int classId = inputView.readVarInt(true);
        if (classId == NULL) {
            return null;
        }
        else if (classId == 1) {
            int nameId = inputView.readVarInt(true);
            if (classNameReadCache.size() > nameId) {
                return classNameReadCache.get(nameId);
            }
            Class<?> typeClass;
            try {
                typeClass = Class.forName(inputView.readString());
            }
            catch (ClassNotFoundException e) {
                throw new JcodecException("class not found", e);
            }
            SerializerWrapper wrapper = this.getOrCacheSerializerWrapper(typeClass);
            if (DEBUG) {
                assert classNameReadCache.size() == nameId;
            }
            classNameReadCache.add(wrapper);
            return wrapper;
        }
        else {
            SerializerWrapper wrapper = registerIndex.get(classId - 2);
            if (DEBUG) {
                assert wrapper != null;
            }
            return wrapper;
        }
    }

    @Override
    public void writeObject(OutputView output, Object value)
    {
        requireNonNull(output, "output is null");
        requireNonNull(value, "value is null");
        Class<?> typeClass = value.getClass();
        getOrCacheSerializerWrapper(typeClass).getSerializer().write(this, output, value);
    }

    @Override
    public <T> void writeObject(OutputView output, T value, Serializer<T> serializer)
    {
        requireNonNull(output, "output is null");
        requireNonNull(serializer, "serializer is null");
        serializer.write(this, output, value);
    }

    @Override
    public <T> void writeObjectOrNull(OutputView output, T value, Class<T> typeClass)
    {
        requireNonNull(output, "output is null");
        requireNonNull(typeClass, "typeClass is null");
        if (value == null) {
            output.writeVarInt(NULL, true);
        }
        else {
            output.writeVarInt(NOT_NULL, true);
            getOrCacheSerializerWrapper(typeClass).getSerializer().write(this, output, value);
        }
    }

    @Override
    public <T> void writeObjectOrNull(OutputView output, T value, Serializer<T> serializer)
    {
        requireNonNull(output, "output is null");
        requireNonNull(serializer, "typeClass is null");
        if (value == null) {
            output.writeVarInt(NULL, true);
        }
        else {
            output.writeVarInt(NOT_NULL, true);
            serializer.write(this, output, value);
        }
    }

    @Override
    public void writeClassAndObject(OutputView output, Object value)
    {
        requireNonNull(output, "output is null");
        if (value == null) {
            writeClass(output, null);
            return;
        }
        SerializerWrapper wrapper = writeClass(output, value.getClass());
        wrapper.getSerializer().write(this, output, value);
    }

    @Override
    public <T> T readObject(InputView input, Class<T> typeClass)
    {
        SerializerWrapper wrapper = this.getOrCacheSerializerWrapper(typeClass);
        return wrapper.<T>getSerializer().read(this, input, typeClass);
    }

    @Override
    public <T> T readObject(InputView input, Class<? extends T> typeClass, Serializer<T> serializer)
    {
        return serializer.read(this, input, typeClass);
    }

    @Override
    public <T> T readClassAndObject(InputView inputView)
    {
        SerializerWrapper wrapper = this.readClass(inputView);
        if (wrapper == null) {
            return null;
        }
        return wrapper.<T>getSerializer().read(this, inputView, wrapper.getTypeClass());
    }

    @Override
    public <T> T readObjectOrNull(InputView inputView, Class<T> typeClass)
    {
        boolean isNull = inputView.readVarInt(true) == 0;
        if (isNull) {
            return null;
        }
        SerializerWrapper wrapper = this.getOrCacheSerializerWrapper(typeClass);
        return wrapper.<T>getSerializer().read(this, inputView, typeClass);
    }

    @Override
    public <T> T readObjectOrNull(InputView inputView, Class<? extends T> typeClass, Serializer<T> serializer)
    {
        boolean isNull = inputView.readVarInt(true) == 0;
        if (isNull) {
            return null;
        }
        return serializer.read(this, inputView, typeClass);
    }
}
