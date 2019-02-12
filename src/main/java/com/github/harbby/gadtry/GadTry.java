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
package com.github.harbby.gadtry;

import com.github.harbby.gadtry.aop.AopFactory;
import com.github.harbby.gadtry.aop.Aspect;
import com.github.harbby.gadtry.ioc.Bean;
import com.github.harbby.gadtry.ioc.BindMapping;
import com.github.harbby.gadtry.ioc.IocFactory;
import com.github.harbby.gadtry.ioc.IocFactoryImpl;

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
        private AopFactory aopFactory;

        public Builder(Bean... beans)
        {
            this.beans = beans;
        }

        public Builder aop(AopFactory aopFactory)
        {
            this.aopFactory = aopFactory;
            return this;
        }

        public Builder aop(Aspect... aspects)
        {
            this.aopFactory = AopFactory.create(aspects);
            return this;
        }

        public Builder setConfigurationProperties(Map<String, Object> config)
        {
            return this;
        }

        public IocFactory initialize()
        {
            IocFactory.ReplaceHandler handler = new IocFactory.ReplaceHandler()
            {
                @Override
                public <T> T replace(Class<T> key, T instance)
                {
                    return aopFactory.proxy(key, instance);
                }
            };
            BindMapping bindMapping = BindMapping.create(handler, beans);
            return new IocFactoryImpl(bindMapping);
        }
    }
}
