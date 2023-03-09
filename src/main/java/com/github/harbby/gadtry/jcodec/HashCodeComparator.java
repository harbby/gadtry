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

import java.util.Comparator;

public class HashCodeComparator<K>
        implements Comparator<K>
{
    @Override
    public int compare(K o1, K o2)
    {
        int h1 = o1 == null ? 0 : o1.hashCode();
        int h2 = o2 == null ? 0 : o2.hashCode();
        return h1 - h2;
    }
}
