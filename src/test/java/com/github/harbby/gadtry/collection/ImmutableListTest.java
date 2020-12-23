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

import com.github.harbby.gadtry.base.Serializables;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ImmutableListTest
{
    @Test
    public void serializableTest()
            throws IOException, ClassNotFoundException
    {
        List<Integer> list = ImmutableList.of(1, 2, 3);
        byte[] bytes = Serializables.serialize((Serializable) list);
        List<Integer> out = Serializables.byteToObject(bytes);
        Assert.assertEquals(list, out);
        Assert.assertEquals(out, Arrays.asList(1, 2, 3));
    }
}
