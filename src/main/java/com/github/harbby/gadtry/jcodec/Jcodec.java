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
package com.github.harbby.gadtry.jcodec;

import java.io.Serializable;
import java.util.Comparator;

public interface Jcodec<E>
        extends Serializable
{
    public void encoder(E value, OutputView output);

    public E decoder(InputView input);

    default boolean isNullable()
    {
        return false;
    }

    /**
     * sortMerge shuffle need
     */
    public default Comparator<E> comparator()
    {
        return new HashCodeComparator<>();
    }
}
