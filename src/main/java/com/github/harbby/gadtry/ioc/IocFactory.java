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

import com.github.harbby.gadtry.function.Creator;
import com.github.harbby.gadtry.function.exception.Function;
import com.github.harbby.gadtry.graph.Graph;

/**
 * harbby ioc
 */
public interface IocFactory
{
    /**
     * @param driver Class waiting to be acquired
     * @param <T>    is driver type
     * @return Driver instance object
     * @throws InjectorException Injector error
     */
    default <T> T getInstance(Class<T> driver)
    {
        return getInstance(driver, (driverClass) -> null);
    }

    /**
     * @param driver      Class waiting to be acquired
     * @param userCreator User-provided implementation
     * @param <T>         is driver type
     * @return T Driver instance object
     * @throws InjectorException Injector error
     */
    public <T> T getInstance(Class<T> driver, Function<Class<?>, ?, Exception> userCreator);

    public <T> Creator<T> getCreator(Class<T> driver);

    public <T> BindMapping getAllBeans();

    @Deprecated
    default Graph<Void, Void> analysis()
    {
        return analyze();
    }

    Graph<Void, Void> analyze();

    public static IocFactory create(Bean... beans)
    {
        BindMapping bindMapping = BindMapping.create(beans);
        return new IocFactoryImpl(bindMapping, beans);
    }
}
