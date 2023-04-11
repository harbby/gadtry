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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class SerializerManager
{
    private final List<Tuple2<Class<?>, Class<? extends Serializer>>> list = new ArrayList<>();

    public <T> void addSerializer(Class<T> typeClass, Class<? extends Serializer> serializerClass)
    {
        ParameterizedType parameterizedType = (ParameterizedType) serializerClass.getGenericInterfaces()[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }
        //Class<?> tClass = (Class<?>) type;
        //checkState(tClass.isAssignableFrom(tClass), "serializerClass not typeClass");
        list.add(Tuple2.of(typeClass, serializerClass));
    }

    public Serializer<?> findSerializer(Jcodec jcodec, Class<?> typeClass)
    {
        for (Tuple2<Class<?>, Class<? extends Serializer>> it : list) {
            if (it.key().isAssignableFrom(typeClass)) {
                Class<? extends Serializer> serializerClass = it.value();
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
        return null;
    }
}
