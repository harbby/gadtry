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
package com.github.harbby.gadtry.ioc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BindMappingTest
{
    @Test
    public void builder()
    {
        Assertions.assertThrows(InjectorException.class, ()-> {
            IocFactory.create(binder ->
            {
                binder.bind(Map.class, new HashMap());
                binder.bind(Map.class).byInstance(new Properties());
            });
        });
    }

    @Test
    public void bindMappingToString()
    {
        IocFactory iocFactory = IocFactory.create(binder -> binder.bind(Map.class, new HashMap()));
        BindMapping bindMapping = iocFactory.getAllBeans();
        Assertions.assertEquals(bindMapping.getAllBeans().size(), 1);
        Assertions.assertTrue(bindMapping.toString().contains("interface java.util.Map="));
        Assertions.assertEquals(bindMapping.get(Map.class).get(), new HashMap());
    }

    @Test
    public void createGiveKeyWithSingle()
    {
        BindMapping bindMapping = BindMapping.create(binder -> binder.bind(HashMap.class).withSingle());
        Assertions.assertEquals(bindMapping.getAllBeans().size(), 1);
        Assertions.assertTrue(bindMapping.get(HashMap.class).get() == bindMapping.get(HashMap.class).get());
    }

    @Test
    public void createGiveKeyAndInstance()
    {
        BindMapping bindMapping = BindMapping.create(binder -> binder.bind(Map.class, new HashMap()));
        Assertions.assertEquals(bindMapping.getAllBeans().size(), 1);
        Assertions.assertTrue(bindMapping.get(Map.class).get() == bindMapping.get(Map.class).get());
    }

    @Test
    public void createGiveKeyByInstance()
    {
        BindMapping bindMapping = BindMapping.create(binder -> binder.bind(Map.class).byInstance(new HashMap()));
        Assertions.assertEquals(bindMapping.get(Map.class).get(), new HashMap());
        Assertions.assertTrue(bindMapping.get(Map.class).get() == bindMapping.get(Map.class).get());
    }

    @Test
    public void createGiveInterfaceWithSingleReturnIllegalStateException()
    {
        Assertions.assertThrows(IllegalStateException.class, ()-> {
            IocFactory.create(binder -> binder.bind(Map.class).withSingle());
        });
    }

    @Test
    public void createGiveInterfaceWithNoScopeReturnIllegalStateException()
    {
        Assertions.assertThrows(IllegalStateException.class, ()-> {
            IocFactory.create(binder -> binder.bind(Map.class).noScope());
        });
    }
}
