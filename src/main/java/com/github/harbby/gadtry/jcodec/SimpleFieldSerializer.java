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

import java.util.ArrayList;
import java.util.List;

public final class SimpleFieldSerializer<T>
        extends FieldSerializer<T>
{
    private final List<FieldData> primitiveFields = new ArrayList<>();
    private final List<FieldData> finalFields = new ArrayList<>();
    private final List<FieldData> dynamicFields = new ArrayList<>();

    public SimpleFieldSerializer(Jcodec jcodec, Class<? extends T> typeClass)
    {
        super(jcodec, typeClass);
        List<FieldData> fieldDataList = analyzeClass(jcodec, typeClass);
        for (FieldData fieldData : fieldDataList) {
            switch (fieldData.fieldType) {
                case OBJECT:
                    primitiveFields.add(fieldData);
                    break;
                case OBJECT_OR_NULL:
                    finalFields.add(fieldData);
                    break;
                case CLASS_AND_OBJET:
                    dynamicFields.add(fieldData);
                    break;
            }
        }
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
    public T read(Jcodec jcodec, InputView input, Class<? extends T> aClass)
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
