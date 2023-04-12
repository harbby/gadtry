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

public interface Jcodec
{
    public <T> void addSerializer(Class<T> typeClass, Class<? extends Serializer> serializerClass);

    public <T> Serializer<T> getSerializer(Class<?> typeClass);

    public <T> T newInstance(Class<? extends T> type);

    public <T> void register(Class<T> typeClass, Class<? extends Serializer<T>> serializerClass);

    public <T> void register(Class<T> typeClass, Serializer<T> serializer);

    public <T> void register(Class<T> typeClass);

    /**
     * write nonnull object to output
     *
     * @param output outputView
     * @param value  not null value
     */
    void writeObject(OutputView output, Object value);

    <T> void writeObject(OutputView output, T value, Serializer<T> serializer);

    <T> void writeObjectOrNull(OutputView output, T value, Class<T> typeClass);

    <T> void writeObjectOrNull(OutputView output, T value, Serializer<T> serializer);

    void writeClassAndObject(OutputView output, Object value);

    <T> T readObject(InputView input, Class<T> typeClass);

    <T> T readObject(InputView input, Class<? extends T> typeClass, Serializer<T> serializer);

    <T> T readClassAndObject(InputView inputView);

    <T> T readObjectOrNull(InputView inputView, Class<T> typeClass);

    <T> T readObjectOrNull(InputView inputView, Class<? extends T> typeClass, Serializer<T> serializer);

    static Jcodec of()
    {
        return new JcodecImpl();
    }
}
