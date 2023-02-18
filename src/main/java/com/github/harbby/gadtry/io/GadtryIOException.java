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
package com.github.harbby.gadtry.io;

public class GadtryIOException
        extends RuntimeException
{
    private static final long serialVersionUID = -5819644260818480079L;

    public GadtryIOException()
    {
        super();
    }

    public GadtryIOException(String message)
    {
        super(message);
    }

    public GadtryIOException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public GadtryIOException(Throwable cause)
    {
        super(cause);
    }
}
