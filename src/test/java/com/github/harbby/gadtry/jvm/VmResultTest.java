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
package com.github.harbby.gadtry.jvm;

import org.junit.Assert;
import org.junit.Test;

public class VmResultTest
{
    private final VmResult<String> vmResult = new VmResult<>("error");

    @Test
    public void getOnFailure()
    {
        Assert.assertEquals(vmResult.getOnFailure(), "error");
    }

    @Test
    public void isFailed()
    {
        Assert.assertTrue(vmResult.isFailed());
    }
}
