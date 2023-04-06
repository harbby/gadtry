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
import com.github.harbby.gadtry.jcodec.codecs.DoubleSerializer;
import com.github.harbby.gadtry.jcodec.codecs.FloatSerializer;
import com.github.harbby.gadtry.jcodec.codecs.IntSerializer;
import com.github.harbby.gadtry.jcodec.codecs.LongSerializer;
import com.github.harbby.gadtry.jcodec.codecs.ObjectArraySerializer;
import com.github.harbby.gadtry.jcodec.codecs.ShortSerializer;
import com.github.harbby.gadtry.jcodec.codecs.StringSerializer;
import com.github.harbby.gadtry.jcodec.codecs.TypeArraySerializer;
import com.github.harbby.gadtry.jcodec.codecs.VoidSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.harbby.gadtry.StaticAssert.DEBUG;
import static java.util.Objects.requireNonNull;

final class JcodecImpl
        implements Jcodec
{
    private static final int NULL = 0;
    private static final int NOT_NULL = 1;

    private final Map<Class<?>, SerializerWrapper> classWriteCache = new CuckooStashHashMap<>();
    private int nextClassId;
    private final List<SerializerWrapper> classReadCache = new ArrayList<>();

    JcodecImpl()
    {
        // Primitives
        register(byte.class, ByteSerializer.class);
        register(boolean.class, BooleanSerializer.class);
        register(short.class, ShortSerializer.class);
        register(char.class, CharSerializer.class);
        register(int.class, IntSerializer.class);
        register(float.class, FloatSerializer.class);
        register(long.class, LongSerializer.class);
        register(double.class, DoubleSerializer.class);
        register(String.class, StringSerializer.class);
        // array
        register(byte[].class, ArraySerializers.ByteArraySerializer.class);
        register(boolean[].class, ArraySerializers.BooleanArraySerializer.class);
        register(short[].class, ArraySerializers.ShortArraySerializer.class);
        register(char[].class, ArraySerializers.CharArraySerializer.class);
        register(int[].class, ArraySerializers.IntArraySerializer.class);
        register(float[].class, ArraySerializers.FloatArraySerializer.class);
        register(long[].class, ArraySerializers.LongArraySerializer.class);
        register(double[].class, ArraySerializers.DoubleArraySerializer.class);
        register(Void.class, VoidSerializer.class);
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
        SerializerWrapper old = classWriteCache.get(typeClass);
        SerializerWrapper wrapper;
        if (old != null && old.getId() != -1) {
            wrapper = new SerializerWrapper(old.getId(), typeClass, serializer);
            classReadCache.set(old.getId(), wrapper);
        }
        else {
            int classId = nextClassId++;
            wrapper = new SerializerWrapper(classId, typeClass, serializer);
            assert !DEBUG || classId == classReadCache.size();
            classReadCache.add(wrapper);
        }
        classWriteCache.put(typeClass, wrapper);
        if (typeClass.isPrimitive()) {
            Class<?> wrapperClass = JavaTypes.getWrapperClass(typeClass);
            classWriteCache.put(wrapperClass, wrapper);
        }
    }

    @Override
    public <T> void register(Class<T> typeClass)
    {
        requireNonNull(typeClass, "typeClass is null");
        SerializerWrapper wrapper = classWriteCache.get(typeClass);
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
        SerializerWrapper wrapper = classWriteCache.get(typeClass);
        if (wrapper != null) {
            return wrapper;
        }
        Serializer<?> serializer = createSerializer(typeClass);
        wrapper = new SerializerWrapper(-1, typeClass, serializer);
        classWriteCache.put(typeClass, wrapper);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Serializer<T> getSerializer(Class<?> typeClass)
    {
        SerializerWrapper wrapper = classWriteCache.get(typeClass);
        if (wrapper != null) {
            return wrapper.getSerializer();
        }
        return (Serializer<T>) createSerializer(typeClass);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> Serializer<T> createSerializer(Class<T> typeClass)
    {
        if (typeClass.isArray()) {
            Class<?> classTag = typeClass.getComponentType();
            Serializer<?> serializer = this.getSerializer(classTag);
            return serializer == null ? new ObjectArraySerializer(classTag) : new TypeArraySerializer(serializer, classTag);
        }
        else if (typeClass.isInterface() || Modifier.isAbstract(typeClass.getModifiers())) {
            return null;
        }
        else {
            return FieldSerializerFactory.makeSerializer(this, typeClass);
        }
    }

    private final Map<Class<?>, Integer> classNameWriteCache = new CuckooStashHashMap<>();
    private final List<SerializerWrapper> classNameReadCache = new ArrayList<>();

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
            SerializerWrapper wrapper = classReadCache.get(classId - 2);
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
    public void writeObjectOrNull(OutputView output, Object value, Class<?> typeClass)
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
    public <T> T readObject(InputView input, Class<T> typeClass, Serializer<T> serializer)
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
    public <T> T readObjectOrNull(InputView inputView, Class<T> typeClass, Serializer<T> serializer)
    {
        boolean isNull = inputView.readVarInt(true) == 0;
        if (isNull) {
            return null;
        }
        return serializer.read(this, inputView, typeClass);
    }
}
