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
package com.github.harbby.gadtry.collection.tuple;

import java.util.Objects;

public class Tuple1<F1>
        implements Tuple
{
    private F1 f1;

    public Tuple1(F1 f1)
    {
        this.f1 = f1;
    }

    public static <F1> Tuple1<F1> of(F1 f1)
    {
        return new Tuple1<>(f1);
    }

    public F1 f1()
    {
        return f1;
    }

    public F1 get()
    {
        return f1;
    }

    public void set(F1 f1)
    {
        this.f1 = f1;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(f1);
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

        Tuple1 other = (Tuple1) obj;
        return Objects.equals(this.f1, other.f1);
    }

    @Override
    public String toString()
    {
        return String.format("(%s)", f1);
    }

    @Override
    public int getArity()
    {
        return 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getField(int pos)
    {
        switch (pos) {
            case 1:
                return (T) f1;
            default:
                throw new IndexOutOfBoundsException(String.valueOf(pos));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple1<F1> copy()
    {
        return new Tuple1<>(f1);
    }
}
