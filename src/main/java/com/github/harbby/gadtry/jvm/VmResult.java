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
package com.github.harbby.gadtry.jvm;

import java.io.Serializable;

public class VmResult<V extends Serializable>
        implements Serializable
{
    private V result;
    private String errorMessage;
    private final boolean isFailed;

    public V get()
            throws JVMException
    {
        if (isFailed()) {
            throw new JVMException("ForKJVMError: " + errorMessage);
        }
        return result;
    }

    public boolean isFailed()
    {
        return isFailed;
    }

    public String getOnFailure()
    {
        return errorMessage;
    }

    public VmResult(Serializable result)
    {
        this.result = (V) result;
        this.isFailed = false;
    }

    public VmResult(String errorMessage)
    {
        this.errorMessage = errorMessage;
        this.isFailed = true;
    }
}
