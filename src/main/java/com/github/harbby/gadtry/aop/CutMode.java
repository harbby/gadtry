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

public interface CutMode<T>
{
    public T around(Handler1<ProxyContext> runnable);

    public T before(Handler0 runnable);

    public T before(Handler1<MethodInfo> runnable);

    public T afterReturning(Handler0 runnable);

    public T afterReturning(Handler1<MethodInfo> runnable);

    public T after(Handler0 runnable);

    public T after(Handler1<MethodInfo> runnable);

    public T afterThrowing(Handler0 runnable);

    public T afterThrowing(Handler1<MethodInfo> runnable);

    public interface Handler0<T>
    {
        public void apply()
                throws Exception;
    }

    public interface Handler1<T>
    {
        public void apply(T t)
                throws Exception;
    }
}
