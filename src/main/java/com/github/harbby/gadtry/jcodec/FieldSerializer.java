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

import com.github.harbby.gadtry.base.Platform;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class FieldSerializer<T>
        implements Serializer<T>
{
    private final Class<? extends T> typeClass;
    private final List<FieldData> primitiveFields = new ArrayList<>();
    private final List<FieldData> finalFields = new ArrayList<>();
    private final List<FieldData> dynamicFields = new ArrayList<>();

    private boolean useConstructor = true;

    private static class FieldData
    {
        private final Field field;
        private final Serializer serializer;

        private FieldData(Field field, Serializer<Object> serializer)
        {
            this.field = field;
            this.serializer = serializer;
        }

        public static FieldData of(Field field, Serializer serializer)
        {
            return new FieldData(field, serializer);
        }

        public Class<?> getType()
        {
            return field.getType();
        }

        public Object get(Object obj)
                throws IllegalAccessException
        {
            return field.get(obj);
        }

        public void set(Object obj, Object value)
                throws IllegalAccessException
        {
            field.set(obj, value);
        }
    }

    public FieldSerializer(Jcodec jcodec, Class<? extends T> typeClass)
    {
        this.typeClass = typeClass;
        this.makeSerializer(jcodec, typeClass);
    }

    private void makeSerializer(Jcodec jcodec, Class<? extends T> typeClass)
    {
        Class<?> it = typeClass;
        while (it != Object.class) {
            Field[] fields = it.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }
                if (!Modifier.isPublic(modifiers)) {
                    field.setAccessible(true);
                }
                make(jcodec, field);
            }
            it = it.getSuperclass();
        }
    }

    private void make(Jcodec jcodec, Field field)
    {
        int modifiers = field.getType().getModifiers();
        Serializer serializer = jcodec.getSerializer(field.getType());
        FieldData fieldData = FieldData.of(field, serializer);
        if (field.getType().isPrimitive()) {
            primitiveFields.add(fieldData);
        }
        else if (Modifier.isFinal(modifiers) && serializer != null) {
            if (serializer.isNullable()) {
                primitiveFields.add(fieldData);
            }
            else {
                finalFields.add(fieldData);
            }
        }
        else {
            dynamicFields.add(fieldData);
        }
    }

    public T newInstance(Jcodec kryo, InputView input, Class<? extends T> typeClass)
    {
        if (useConstructor) {
            try {
                return kryo.newInstance(typeClass);
            }
            catch (JcodecException e) {
                useConstructor = false;
            }
        }
        return Platform.allocateInstance2(typeClass);
    }

    @Override
    public void write(Jcodec jcodec, OutputView output, T value)
    {
        try {
            for (FieldData field : primitiveFields) {
                Object fValue = field.get(value);
                jcodec.writeObject(output, fValue, field.serializer);
            }
            for (FieldData field : finalFields) {
                Object fValue = field.get(value);
                jcodec.writeObjectOrNull(output, fValue, field.serializer);
            }
            for (FieldData field : dynamicFields) {
                Object fValue = field.get(value);
                jcodec.writeClassAndObject(output, fValue);
            }
        }
        catch (Exception e) {
            throw new JcodecException("jcodec failed", e);
        }
    }

    @Override
    public T read(Jcodec jcodec, InputView input, Class<? extends T> typeClass)
    {
        T object = this.newInstance(jcodec, input, this.typeClass);
        try {
            Object value;
            for (FieldData field : primitiveFields) {
                value = jcodec.readObject(input, field.getType(), field.serializer);
                field.set(object, value);
            }
            for (FieldData field : finalFields) {
                value = jcodec.readObjectOrNull(input, field.getType(), field.serializer);
                field.set(object, value);
            }
            for (FieldData field : dynamicFields) {
                value = jcodec.readClassAndObject(input);
                field.set(object, value);
            }
        }
        catch (Exception e) {
            throw new JcodecException("jcodec failed", e);
        }
        return object;
    }
}
