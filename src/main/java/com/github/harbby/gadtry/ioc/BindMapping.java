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

import com.github.harbby.gadtry.base.Lazys;
import com.github.harbby.gadtry.collection.mutable.MutableMap;
import com.github.harbby.gadtry.function.Creator;

import java.util.HashMap;
import java.util.Map;

import static com.github.harbby.gadtry.base.MoreObjects.checkState;
import static java.util.Objects.requireNonNull;

public interface BindMapping
{
    default <T> Creator<T> get(Class<T> type)
    {
        return getOrDefault(type, null);
    }

    <T> Creator<T> getOrDefault(Class<T> type, Creator<T> defaultValue);

    public <T> Map<Class<?>, Creator<?>> getAllBeans();

    static Builder builder()
    {
        return new Builder();
    }

    static class Builder
    {
        private final Map<Class<?>, Creator<?>> bindMapping = new HashMap<>();

        public <T> Builder bind(Class<T> type, Creator<? extends T> creator)
        {
            Creator oldCreator = bindMapping.get(type);
            if (oldCreator != null) {
                throw new InjectorException(" Unable to create IocFactory, see the following errors:\n" +
                        "A binding to " + type.toString() + " was already configured at " + oldCreator);
            }
            bindMapping.put(type, creator);
            return this;
        }

        <T> void bindUpdate(Class<T> type, Creator<? extends T> creator)
        {
            bindMapping.put(type, creator);
        }

        public BindMapping build()
        {
            return new BindMapping()
            {
                @SuppressWarnings("unchecked")
                @Override
                public <T> Creator<T> getOrDefault(Class<T> type, Creator<T> defaultValue)
                {
                    return (Creator<T>) bindMapping.getOrDefault(type, defaultValue);
                }

                @Override
                public Map<Class<?>, Creator<?>> getAllBeans()
                {
                    return MutableMap.copy(bindMapping);
                }

                @Override
                public String toString()
                {
                    return bindMapping.toString();
                }
            };
        }
    }

    public static BindMapping create(Bean... beans)
    {
        return create(IocFactory.ReplaceHandler.INSTANCE, beans);
    }

    public static BindMapping create(IocFactory.ReplaceHandler proxyHandler, Bean... beans)
    {
        requireNonNull(proxyHandler, "proxyHandler is null");
        final BindMapping.Builder builder = BindMapping.builder();
        final InternalContext context = InternalContext.of(builder.build(), (x) -> null);
        final Binder binder = new Binder()
        {
            @Override
            public <T> void bind(Class<T> key, T instance)
            {
                builder.bind(key, Lazys.goLazy(() -> proxyHandler.replace(key, instance)));
            }

            @Override
            public <T> BinderBuilder<T> bind(Class<T> key)
            {
                return new BinderBuilder<T>()
                {
                    @Override
                    public void withSingle()
                    {
                        checkState(!key.isInterface(), key + "key is Interface");
                        Creator<T> creator = () -> proxyHandler.replace(key, context.getByNew(key));
                        builder.bind(key, Lazys.goLazy(creator));
                    }

                    @Override
                    public void noScope()
                    {
                        checkState(!key.isInterface(), key + "key is Interface");
                        Creator<T> creator = () -> proxyHandler.replace(key, context.getByNew(key));
                        builder.bind(key, creator);
                    }

                    @Override
                    public Scope by(Class<? extends T> createClass)
                    {
                        Creator<T> creator = () -> proxyHandler.replace(key, context.getByNew(createClass));
                        builder.bind(key, creator);
                        return () -> builder.bindUpdate(key, Lazys.goLazy(creator));
                    }

                    @Override
                    public void byInstance(T instance)
                    {
                        builder.bind(key, Lazys.goLazy(() -> proxyHandler.replace(key, instance)));
                    }

                    @Override
                    public Scope byCreator(Creator<? extends T> creator)
                    {
                        Creator<? extends T> proxyCreator = () -> proxyHandler.replace(key, creator.get());
                        builder.bind(key, proxyCreator);
                        return () -> builder.bindUpdate(key, Lazys.goLazy(proxyCreator));
                    }

                    @Override
                    public Scope byCreator(Class<? extends Creator<T>> creatorClass)
                    {
                        Creator<? extends T> proxyCreator = () -> proxyHandler.replace(key, context.getByNew(creatorClass).get());
                        builder.bind(key, proxyCreator);
                        return () -> builder.bindUpdate(key, Lazys.goLazy(proxyCreator));
                    }
                };
            }
        };

        for (Bean bean : beans) {
            bean.configure(binder);
        }

        return builder.build();
    }
}
