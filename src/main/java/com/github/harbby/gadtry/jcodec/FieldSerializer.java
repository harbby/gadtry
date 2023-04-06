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

public abstract class FieldSerializer<T>
        implements Serializer<T>
{
    protected final Class<? extends T> typeClass;

    protected boolean useConstructor = true;

    static class FieldData
    {
        protected final FieldType fieldType;
        protected final Field field;
        protected final Serializer serializer;

        private FieldData(Field field, FieldType fieldType, Serializer<Object> serializer)
        {
            this.field = field;
            this.fieldType = fieldType;
            this.serializer = serializer;
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

        public Field getField()
        {
            return field;
        }

        public void set(Object obj, Object value)
                throws IllegalAccessException
        {
            field.set(obj, value);
        }
    }

    static enum FieldType
    {
        OBJECT,
        OBJECT_OR_NULL,
        CLASS_AND_OBJET;
    }

    public FieldSerializer(Jcodec jcodec, Class<? extends T> typeClass)
    {
        this.typeClass = typeClass;
    }

    static <T> List<FieldData> analyzeClass(Jcodec jcodec, Class<? extends T> typeClass)
    {
        List<FieldData> fieldList = new ArrayList<>();
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
                FieldData fieldData = make(jcodec, field);
                fieldList.add(fieldData);
            }
            it = it.getSuperclass();
        }
        return fieldList;
    }

    private static FieldData make(Jcodec jcodec, Field field)
    {
        int modifiers = field.getType().getModifiers();
        FieldType fieldType;
        Serializer serializer;
        if (field.getType().isPrimitive()) {
            serializer = jcodec.getSerializer(field.getType());
            fieldType = FieldType.OBJECT;
        }
        else if (Modifier.isFinal(modifiers)) {
            serializer = jcodec.getSerializer(field.getType());
            fieldType = serializer.isNullable() ? FieldType.OBJECT : FieldType.OBJECT_OR_NULL;
        }
        else {
            serializer = null;
            fieldType = FieldType.CLASS_AND_OBJET;
        }
        return new FieldData(field, fieldType, serializer);
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
}
