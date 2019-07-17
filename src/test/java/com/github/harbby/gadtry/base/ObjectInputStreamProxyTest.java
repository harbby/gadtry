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

import com.github.harbby.gadtry.aop.mock.Mock;
import com.github.harbby.gadtry.aop.mock.MockGo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;

import static com.github.harbby.gadtry.aop.mock.MockGo.when;

public class ObjectInputStreamProxyTest
{
    @Test
    public void getLatestUserDefinedLoader()
    {
        Assert.assertEquals(this.getClass().getClassLoader(), ObjectInputStreamProxy.getLatestUserDefinedLoader());
    }

    @Mock
    private ObjectStreamClass objectStreamClass;

    @Before
    public void setUp()
    {
        MockGo.initMocks(this);
    }

    @Test
    public void resolveClass()
            throws IOException, ClassNotFoundException
    {
        ObjectStreamClass intClass = ObjectStreamClass.lookupAny(int.class);

        byte[] bytes = Serializables.serialize(1);
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStreamProxy objectInputStream = new ObjectInputStreamProxy(arrayInputStream, ObjectInputStreamProxy.getLatestUserDefinedLoader());

        when(objectStreamClass.getName()).thenReturn("gadtry.gadtry.gadtry.gadtry");
        Assert.assertEquals(int.class, objectInputStream.resolveClass(intClass));

        try {
            objectInputStream.resolveClass(objectStreamClass);
            Assert.fail();
        }
        catch (ClassNotFoundException ignored) {
        }
        //ObjectStreamClass

        Assert.assertEquals(1, (int) objectInputStream.readObject());
    }
}
