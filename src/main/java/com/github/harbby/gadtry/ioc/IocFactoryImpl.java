/*
 * Copyright (C) 2018 The Harbby Authors
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
import com.github.harbby.gadtry.function.Function;
import com.github.harbby.gadtry.graph.Graph;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class IocFactoryImpl
        implements IocFactory
{
    private final BindMapping binds;
    private final Bean[] beans;

    public IocFactoryImpl(BindMapping binds, Bean[] beans)
    {
        this.binds = binds;
        this.beans = beans;
    }

    /**
     * @throws InjectorException Injector error
     */
    public <T> T getInstance(Class<T> driver, Function<Class<?>, ?> userCreator)
    {
        return InternalContext.of(binds, userCreator).get(driver);
    }

    @Override
    public <T> Creator<T> getCreator(Class<T> driver)
    {
        return () -> InternalContext.of(binds, (driverClass) -> null).get(driver);
    }

    @Override
    public <T> BindMapping getAllBeans()
    {
        return binds;
    }

    @Override
    public Graph<Void, Void> analysis()
    {
        Graph.GraphBuilder<Void, Void> builder = Graph.builder();

        Binder binder = new Binder()
        {
            @Override
            public <T> void bind(Class<T> key, T instance) {}

            @Override
            public <T> BinderBuilder<T> bind(Class<T> key)
            {
                return new BinderBuilder<T>()
                {
                    @Override
                    public void withSingle()
                    {
                        parserDep(key, key);
                    }

                    @Override
                    public void noScope()
                    {
                        parserDep(key, key);
                    }

                    @Override
                    public Scope by(Class<? extends T> createClass)
                    {
                        parserDep(key, createClass);
                        return () -> {};
                    }

                    @Override
                    public void byInstance(T instance) {}

                    @Override
                    public Scope byCreator(Creator<? extends T> creator)
                    {
                        return () -> {};
                    }

                    @Override
                    public Scope byCreator(Class<? extends Creator<T>> creatorClass)
                    {
                        parserDep(key, creatorClass);
                        return () -> {};
                    }
                };
            }

            private void parserDep(Class key, Class aClass)
            {
                for (Constructor<?> constructor : aClass.getConstructors()) {
                    if (constructor.getAnnotation(Autowired.class) != null) {
                        for (Class type : constructor.getParameterTypes()) {
                            if (type != key) {
                                builder.addNode(key.toString());
                                builder.addNode(type.toString());
                                builder.addEdge(key.toString(), type.toString());
                            }
                        }
                    }
                }

                for (Field field : aClass.getDeclaredFields()) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    if (autowired != null && key != field.getType()) {
                        builder.addNode(key.toString());
                        builder.addNode(field.getType().toString());
                        builder.addEdge(key.toString(), field.getType().toString());
                    }
                }
            }
        };

        for (Bean bean : beans) {
            bean.configure(binder);
        }
        Graph<Void, Void> graph = builder.create();
        graph.searchRuleRoute(route -> {
            if (!route.containsDeadRecursion()) {
                throw new IllegalArgumentException("Find Circular dependency" + route.getIds());
            }
            return true;
        });
        return graph;
    }
}
