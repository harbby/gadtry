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

import java.util.HashMap;
import java.util.Map;

interface Binds
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

        public Binds build()
        {
            return new Binds()
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
                    return bindMapping;
                }

                @Override
                public String toString()
                {
                    return bindMapping.toString();
                }
            };
        }
    }
}
