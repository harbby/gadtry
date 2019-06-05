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

import com.github.harbby.gadtry.aop.AopFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BindMappingTest
{
    @Test(expected = InjectorException.class)
    public void builder()
    {
        IocFactory.create(binder ->
        {
            binder.bind(Map.class, new HashMap());
            binder.bind(Map.class).byInstance(new Properties());
        });
    }

    @Test
    public void bindMappingToString()
    {
        IocFactory iocFactory = IocFactory.create(binder -> binder.bind(Map.class, new HashMap()));
        BindMapping bindMapping = iocFactory.getAllBeans();
        Assert.assertEquals(bindMapping.getAllBeans().size(), 1);
        Assert.assertTrue(bindMapping.toString().contains("interface java.util.Map="));
        Assert.assertEquals(bindMapping.get(Map.class).get(), new HashMap());
    }

    @Test
    public void createGiveKeyWithSingle()
    {
        BindMapping bindMapping = BindMapping.create(binder -> binder.bind(HashMap.class).withSingle());
        Assert.assertEquals(bindMapping.getAllBeans().size(), 1);
        Assert.assertTrue(bindMapping.get(HashMap.class).get() == bindMapping.get(HashMap.class).get());
    }

    @Test
    public void createGiveKeyAndInstance()
    {
        BindMapping bindMapping = BindMapping.create(new IocFactory.ReplaceHandler()
        {
            @Override
            public <T> T replace(Class<T> key, T instance)
            {
                return AopFactory.proxy(key).byInstance(instance).before(a -> {});
            }
        }, binder -> binder.bind(Map.class, new HashMap()));
        Assert.assertEquals(bindMapping.getAllBeans().size(), 1);
        Assert.assertTrue(bindMapping.get(Map.class).get() == bindMapping.get(Map.class).get());
    }

    @Test
    public void createGiveKeyByInstance()
    {
        BindMapping bindMapping = BindMapping.create(new IocFactory.ReplaceHandler()
        {
            @Override
            public <T> T replace(Class<T> key, T instance)
            {
                return AopFactory.proxy(key).byInstance(instance).before(a -> {});
            }
        }, binder -> binder.bind(Map.class).byInstance(new HashMap()));
        Assert.assertEquals(bindMapping.get(Map.class).get(), new HashMap());
        Assert.assertTrue(bindMapping.get(Map.class).get() == bindMapping.get(Map.class).get());
    }

    @Test(expected = IllegalStateException.class)
    public void createGiveInterfaceWithSingleReturnIllegalStateException()
    {
        IocFactory.create(binder -> binder.bind(Map.class).withSingle());
    }

    @Test(expected = IllegalStateException.class)
    public void createGiveInterfaceWithNoScopeReturnIllegalStateException()
    {
        IocFactory.create(binder -> binder.bind(Map.class).noScope());
    }
}
