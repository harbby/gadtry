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
package com.github.harbby.gadtry.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;

public class ClassLoaderObjectInputStreamTest
{
    @Test
    public void resolveClass()
            throws IOException, ClassNotFoundException
    {
        ObjectStreamClass intClass = ObjectStreamClass.lookupAny(int.class);

        byte[] bytes = Serializables.serialize(1);
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
        ClassLoaderObjectInputStream objectInputStream = new ClassLoaderObjectInputStream(arrayInputStream, this.getClass().getClassLoader());
        Assertions.assertEquals(int.class, objectInputStream.resolveClass(intClass));

        //ObjectStreamClass
        Assertions.assertEquals(1, (int) objectInputStream.readObject());
    }
}
