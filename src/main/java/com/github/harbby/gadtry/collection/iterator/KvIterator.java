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
package com.github.harbby.gadtry.collection.iterator;

import com.github.harbby.gadtry.collection.IteratorPlus;
import com.github.harbby.gadtry.collection.tuple.Tuple2;

public interface KvIterator<K, V>
        extends IteratorPlus<Tuple2<K, V>>
{
    /**
     * 唯一键
     *
     * @return key是否具有唯一性
     */
    public boolean primaryKey();
}
