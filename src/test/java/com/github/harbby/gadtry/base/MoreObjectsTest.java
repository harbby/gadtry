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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class MoreObjectsTest
{
    @Test
    public void firstNonNull()
    {
        int num = MoreObjects.firstNonNull(null, null, 2, null, 3);
        Assert.assertEquals(num, 2);
    }

    @Test(expected = NullPointerException.class)
    public void firstNonNullGiveNull()
    {
        try {
            MoreObjects.firstNonNull(null);
            Assert.fail();
        }
        catch (NullPointerException ignored) {
        }
        MoreObjects.firstNonNull(null, null, null);
    }

    @Test
    public void checkArgument()
    {
        MoreObjects.checkArgument(true);
        try {
            MoreObjects.checkArgument(false);
            Assert.fail();
        }
        catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void checkState()
    {
        MoreObjects.checkState(true);
        try {
            MoreObjects.checkState(false);
            Assert.fail();
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
            Assert.fail();
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "done");
        }
    }

    @Test
    public void checkArgumentGiveTrueAndMsgFormat()
    {
        MoreObjects.checkArgument(true, "done %s,%s", 1, "2");
        try {
            MoreObjects.checkArgument(false, "done %s,%s", 1, "2");
            Assert.fail();
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "done 1,2");
        }
    }

    @Test
    public void checkNonFalseReturnFalse()
    {
        AtomicInteger cnt = new AtomicInteger(0);
        boolean ok = MoreObjects.checkNonFalse(new Integer[] {1, 2, 1, 5, 2}, (num) -> {
            cnt.getAndIncrement();
            return num <= 3;
        });
        Assert.assertFalse(ok);
        Assert.assertEquals(4, cnt.get());
    }

    @Test
    public void checkNonFalseReturnTrue()
    {
        AtomicInteger cnt = new AtomicInteger(0);
        boolean ok = MoreObjects.checkNonFalse(new Integer[] {1, 2, 1, 5, 2}, (num) -> {
            cnt.getAndIncrement();
            return num <= 10;
        });
        Assert.assertTrue(ok);
        Assert.assertEquals(5, cnt.get());
    }

    @Test
    public void checkContainsTrue()
    {
        AtomicInteger cnt = new AtomicInteger(0);
        boolean ok = MoreObjects.checkContainsTrue((num) -> {
            cnt.getAndIncrement();
            return num > 3;
        }, 1, 2, 1, 5, 2);
        Assert.assertTrue(ok);
        Assert.assertEquals(4, cnt.get());

        boolean noFound = MoreObjects.checkContainsTrue((num) -> {
            cnt.getAndIncrement();
            return num > 3;
        }, 1);
        Assert.assertFalse(noFound);
    }

    @Test
    public void checkIterableContainsTrue()
    {
        AtomicInteger cnt = new AtomicInteger(0);
        boolean ok = MoreObjects.checkContainsTrue((num) -> {
            cnt.getAndIncrement();
            return num > 3;
        }, Arrays.asList(1, 2, 1, 5, 2));
        Assert.assertTrue(ok);
        Assert.assertEquals(4, cnt.get());

        boolean noFound = MoreObjects.checkContainsTrue((num) -> {
            cnt.getAndIncrement();
            return num > 3;
        }, new ArrayList<Integer>());
        Assert.assertFalse(noFound);
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
        Assert.assertTrue(toString.contains("key") && toString.contains("123"));
    }
}
