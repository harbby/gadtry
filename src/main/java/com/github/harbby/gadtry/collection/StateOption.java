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
package com.github.harbby.gadtry.collection;

import java.io.Serializable;

public class StateOption<V>
        implements Serializable
{
    private boolean defined;
    private V value;

    public static <V> StateOption<V> of(V value)
    {
        StateOption<V> stateOption = new StateOption<>();
        stateOption.update(value);
        return stateOption;
    }

    public static <V> StateOption<V> empty()
    {
        return new StateOption<>();
    }

    public void update(V value)
    {
        this.value = value;
        defined = true;
    }

    public boolean isDefined()
    {
        return defined;
    }

    public boolean isEmpty()
    {
        return !defined;
    }

    public V getValue()
    {
        return value;
    }

    public V remove()
    {
        V old = value;
        this.value = null;
        this.defined = false;
        return old;
    }
}
