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
package com.github.harbby.gadtry;

import com.github.harbby.gadtry.aop.AopBinder;
import com.github.harbby.gadtry.aop.AopGo;
import com.github.harbby.gadtry.aop.Aspect;
import com.github.harbby.gadtry.aop.aopgo.MockBinder;
import com.github.harbby.gadtry.function.exception.Consumer;
import com.github.harbby.gadtry.ioc.Bean;
import com.github.harbby.gadtry.ioc.BindMapping;
import com.github.harbby.gadtry.ioc.IocFactory;
import com.github.harbby.gadtry.ioc.IocFactoryImpl;
import com.github.harbby.gadtry.ioc.IocHandler;

import java.util.HashMap;
import java.util.Map;

public class GadTry
{
    private GadTry() {}

    public static Builder create(Bean... beans)
    {
        return new Builder(beans);
    }

    public static class Builder
    {
        private Bean[] beans;
        private Aspect[] aspects;

        public Builder(Bean... beans)
        {
            this.beans = beans;
        }

        public Builder aop(Aspect... aspects)
        {
            this.aspects = aspects;
            return this;
        }

        public Builder setConfigurationProperties(Map<String, Object> config)
        {
            return this;
        }

        public IocFactory initialize()
        {
            Map<Class<?>, Object> pointcutMap = new HashMap<>();
            AopBinder binder0 = new AopBinder()
            {
                @Override
                public <T> PointBuilder<T> bind(Class<T> inputClass)
                {
                    return binder -> pointcutMap.put(inputClass, binder);
                }
            };
            for (Aspect aspect : aspects) {
                aspect.register(binder0);
            }

            IocHandler handler = new IocHandler()
            {
                @Override
                public <T> T onCreate(Class<T> key, T instance)
                {
                    @SuppressWarnings("unchecked")
                    Consumer<MockBinder<T>, Throwable> pointcut = (Consumer<MockBinder<T>, Throwable>) pointcutMap.get(key);
                    if (pointcut == null) {
                        return instance;
                    }
                    return AopGo.proxy(key)
                            .byInstance(instance)
                            .aop(pointcut)
                            .build();
                }
            };
            BindMapping bindMapping = BindMapping.create(handler, beans);
            return new IocFactoryImpl(bindMapping, beans);
        }
    }
}
