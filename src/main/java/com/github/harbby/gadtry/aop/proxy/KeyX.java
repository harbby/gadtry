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
package com.github.harbby.gadtry.aop.proxy;

import java.lang.ref.WeakReference;
import java.util.Collection;

/*
 * a key used for proxy class with any number of implemented interfaces
 */
public final class KeyX
{
    private final int hash;
    private final WeakReference<Class<?>>[] refs;

    @SuppressWarnings("unchecked")
    public KeyX(Collection<Class<?>> interfaces)
    {
        hash = interfaces.hashCode();
        refs = (WeakReference<Class<?>>[]) new WeakReference<?>[interfaces.size()];
        int i = 0;
        for (Class<?> aClass : interfaces) {
            refs[i++] = new WeakReference<>(aClass);
        }
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        KeyX other = (KeyX) obj;
        if (this.refs.length != other.refs.length) {
            return false;
        }
        for (int i = 0; i < this.refs.length; i++) {
            Class<?> intf = this.refs[i].get();
            if (intf == null || intf != other.refs[i].get()) {
                return false;
            }
        }
        return true;
    }
}
