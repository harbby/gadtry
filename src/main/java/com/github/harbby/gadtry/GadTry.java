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
import com.github.harbby.gadtry.function.Creator;
import com.github.harbby.gadtry.function.Function;
import com.github.harbby.gadtry.ioc.Bean;
import com.github.harbby.gadtry.ioc.BindMapping;
import com.github.harbby.gadtry.ioc.IocFactory;

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
        private IocFactory iocFactory;
        private AopFactory aopFactory;

        public Builder(Bean... beans)
        {
            this.iocFactory = IocFactory.create(beans);
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
            return new IocFactory()
            {
                @Override
                public <T> T getInstance(Class<T> driver, Function<Class<?>, ?> userCreator)
                {
                    T value = iocFactory.getInstance(driver, userCreator);

                    return aopFactory == null ? value : aopFactory.proxy(driver, value);
                }

                @Override
                public <T> Creator<T> getCreator(Class<T> driver)
                {
                    return iocFactory.getCreator(driver);
                }

                @Override
                public <T> BindMapping getAllBeans()
                {
                    return iocFactory.getAllBeans();
                }
            };
        }
    }
}
