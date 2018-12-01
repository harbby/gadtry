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
package com.github.harbby.gadtry.collection;

public class Tuple2<F0, F1>
{
    private final F0 f0;
    private final F1 f1;

    public Tuple2(F0 f0, F1 f1)
    {
        this.f0 = f0;
        this.f1 = f1;
    }

    public static <F0, F1> Tuple2<F0, F1> of(F0 f0, F1 f1)
    {
        return new Tuple2<>(f0, f1);
    }

    public F0 f0()
    {
        return f0;
    }

    public F1 f1()
    {
        return f1;
    }
}
