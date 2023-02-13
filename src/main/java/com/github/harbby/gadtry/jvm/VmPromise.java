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
package com.github.harbby.gadtry.jvm;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface VmPromise<R>
        extends Promise<R>
{
    R call()
            throws JVMException, InterruptedException;

    R call(long timeout, TimeUnit timeUnit)
            throws JVMException, InterruptedException;

    long pid();

    boolean isAlive();

    void cancel();

    default <E> VmPromise<E> map(Function<R, E> map)
    {
        requireNonNull(map, "func is null");
        return new VmPromise<E>()
        {
            @Override
            public long pid()
            {
                return VmPromise.this.pid();
            }

            @Override
            public boolean isAlive()
            {
                return VmPromise.this.isAlive();
            }

            @Override
            public E call()
                    throws InterruptedException
            {
                return map.apply(VmPromise.this.call());
            }

            @Override
            public E call(long timeout, TimeUnit timeUnit)
                    throws JVMException, InterruptedException
            {
                return map.apply(VmPromise.this.call(timeout, timeUnit));
            }

            @Override
            public void cancel()
            {
                VmPromise.this.cancel();
            }
        };
    }
}
