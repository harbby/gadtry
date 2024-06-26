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
package com.github.harbby.gadtry.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MoreObjectsTest
{
    @Test
    public void firstNonNull()
    {
        int num = MoreObjects.getFirstNonNull(null, null, 2, null, 3);
        Assertions.assertEquals(num, 2);
    }

    @Test
    public void getFirstNonNullGiveNull()
    {
        Assertions.assertThrows(NullPointerException.class, ()-> {
            MoreObjects.getFirstNonNull(null, null, null);
        });
    }

    @Test
    public void checkArgument()
    {
        MoreObjects.checkArgument(true);
        try {
            MoreObjects.checkArgument(false);
            Assertions.fail();
        }
        catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void checkState()
    {
        MoreObjects.checkState(true);
        try {
            MoreObjects.checkState(false);
            Assertions.fail();
        }
        catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void checkArgumentGiveTrueAndMsg()
    {
        MoreObjects.checkArgument(true, "done");
        try {
            MoreObjects.checkArgument(false, "done");
            Assertions.fail();
        }
        catch (IllegalArgumentException e) {
            Assertions.assertEquals(e.getMessage(), "done");
        }
    }

    @Test
    public void checkArgumentGiveTrueAndMsgFormat()
    {
        MoreObjects.checkArgument(true, "done %s,%s", 1, "2");
        try {
            MoreObjects.checkArgument(false, "done %s,%s", 1, "2");
            Assertions.fail();
        }
        catch (IllegalArgumentException e) {
            Assertions.assertEquals(e.getMessage(), "done 1,2");
        }
    }

    @Test
    public void toStringHelper()
    {
        String toString = MoreObjects.toStringHelper(this)
                .add("key1", 123)
                .add("key2", "123")
                .add("key3", 123L)
                .add("key4", 3.14f)
                .add("key5", 3.14d)
                .add("key6", true)
                .toString();
        Assertions.assertTrue(toString.contains("key") && toString.contains("123"));
    }
}
