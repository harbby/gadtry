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

import com.github.harbby.gadtry.collection.tuple.Tuple2;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;

final class SerializerManager
{
    private final List<Tuple2<Class<?>, Class<? extends Serializer>>> list = new ArrayList<>();

    public <T> void addSerializer(Class<T> typeClass, Class<? extends Serializer> serializerClass)
    {
        // check serializerClass
        this.checkSerializer(typeClass, serializerClass);
        int i = 0;
        for (; i < list.size(); i++) {
            Tuple2<Class<?>, Class<? extends Serializer>> it = list.get(i);
            if (it.key().isAssignableFrom(typeClass)) {
                break;
            }
        }
        list.add(i, Tuple2.of(typeClass, serializerClass));
    }

    public Serializer<?> makeSerializer(Jcodec jcodec, Class<?> typeClass)
    {
        for (Tuple2<Class<?>, Class<? extends Serializer>> it : list) {
            if (it.key().isAssignableFrom(typeClass)) {
                return this.makeSerializer(jcodec, typeClass, it.value());
            }
        }
        return null;
    }

    public void checkSerializer(Class<?> typeClass, Class<? extends Serializer> serializerClass)
    {
        ParameterizedType parameterizedType = (ParameterizedType) serializerClass.getGenericInterfaces()[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }
        if (typeClass == Object[].class) {
            checkState(type instanceof GenericArrayType, "serializerClass not is array serializer");
        }
        else {
            checkState(((Class<?>) type).isAssignableFrom(typeClass), "serializerClass not typeClass");
        }
    }

    public Serializer<?> makeSerializer(Jcodec jcodec, Class<?> typeClass, Class<? extends Serializer> serializerClass)
    {
        try {
            try {
                return serializerClass.getDeclaredConstructor().newInstance();
            }
            catch (NoSuchMethodException ignored) {
            }
            try {
                return serializerClass.getDeclaredConstructor(Class.class).newInstance(typeClass);
            }
            catch (NoSuchMethodException ignored) {
            }
            return serializerClass.getDeclaredConstructor(Jcodec.class, Class.class).newInstance(jcodec, typeClass);
        }
        catch (Exception e) {
            throw new JcodecException("Instance failed", e);
        }
    }
}
