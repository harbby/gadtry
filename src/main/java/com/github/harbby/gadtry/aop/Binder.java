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
package com.github.harbby.gadtry.aop;

import com.github.harbby.gadtry.aop.model.After;
import com.github.harbby.gadtry.aop.model.AfterReturning;
import com.github.harbby.gadtry.aop.model.AfterThrowing;
import com.github.harbby.gadtry.aop.model.Before;
import com.github.harbby.gadtry.aop.model.Pointcut;
import com.github.harbby.gadtry.aop.v1.FilterBuilder;
import com.github.harbby.gadtry.function.exception.Consumer;
import com.github.harbby.gadtry.function.exception.Function;

import static java.util.Objects.requireNonNull;

public interface Binder
{
    /**
     * Define a pointcut
     *
     * @param pointName cut name
     * @param location cut location
     * @return next set CutMode
     */
    @Deprecated
    public PointBuilder bind(String pointName, String location);

    /**
     * Define a pointcut
     *
     * @param pointName cut name
     * @return LocationOrClass
     */
    public FilterBuilder bind(String pointName);

    public static class PointBuilder
            implements CutMode<PointBuilder>
    {
        private final Pointcut pointcut;

        public PointBuilder(Pointcut pointcut)
        {
            this.pointcut = pointcut;
        }

        @Override
        public PointBuilder around(Function<ProxyContext, Object, Throwable> aroundHandler)
        {
            requireNonNull(aroundHandler, "aroundHandler is null");
            pointcut.setAround(aroundHandler);
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder before(Consumer<Before, Exception> runnable)
        {
            pointcut.setBefore(runnable);
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder afterReturning(Consumer<AfterReturning, Exception> runnable)
        {
            pointcut.setAfterReturning(runnable);
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder after(Consumer<After, Exception> runnable)
        {
            pointcut.setAfter(runnable);
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder afterThrowing(Consumer<AfterThrowing, Exception> runnable)
        {
            pointcut.setAfterThrowing(runnable);
            return new PointBuilder(pointcut);
        }
    }
}
