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
package com.github.harbby.gadtry.aop;

import com.github.harbby.gadtry.aop.model.MethodInfo;
import com.github.harbby.gadtry.aop.model.Pointcut;
import com.github.harbby.gadtry.aop.v1.LocationBuilder;

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
    public LocationBuilder bind(String pointName);

    public static class PointBuilder
            implements CutMode<PointBuilder>
    {
        private final Pointcut pointcut;

        public PointBuilder(Pointcut pointcut)
        {
            this.pointcut = pointcut;
        }

        @Override
        public PointBuilder around(Handler1<ProxyContext> runnable)
        {
            pointcut.setAround(runnable);
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder before(Handler0 runnable)
        {
            pointcut.setBefore(info -> runnable.apply());
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder before(Handler1<MethodInfo> runnable)
        {
            pointcut.setBefore(runnable);
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder afterReturning(Handler0 runnable)
        {
            pointcut.setAfterReturning(info -> runnable.apply());
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder afterReturning(Handler1<MethodInfo> runnable)
        {
            pointcut.setAfterReturning(runnable);
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder after(Handler0 runnable)
        {
            pointcut.setAfter(info -> runnable.apply());
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder after(Handler1<MethodInfo> runnable)
        {
            pointcut.setAfter(runnable);
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder afterThrowing(Handler0 runnable)
        {
            pointcut.setAfterThrowing(info -> runnable.apply());
            return new PointBuilder(pointcut);
        }

        @Override
        public PointBuilder afterThrowing(Handler1<MethodInfo> runnable)
        {
            pointcut.setAfterThrowing(runnable);
            return new PointBuilder(pointcut);
        }
    }
}
