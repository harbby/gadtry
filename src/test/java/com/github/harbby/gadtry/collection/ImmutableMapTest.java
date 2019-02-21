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

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class ImmutableMapTest
{
    @Test
    public void builder()
    {
        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("a1", "v1")
                .build();
        try {
            map.put("a1", "a2");
            Assert.fail();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(e instanceof UnsupportedOperationException);
        }
    }
}
