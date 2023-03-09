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

import java.io.IOException;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

public final class StreamOutputView
        extends AbstractOutputView
{
    private final OutputStream outputStream;

    public StreamOutputView(OutputStream outputStream)
    {
        super();
        this.outputStream = requireNonNull(outputStream, "outputStream is null");
    }

    public StreamOutputView(OutputStream outputStream, int buffSize)
    {
        super(buffSize);
        this.outputStream = outputStream;
    }

    @Override
    public void flush()
    {
        try {
            outputStream.write(buffer, 0, offset);
            this.offset = 0;
        }
        catch (IOException e) {
            throw new JcodecException(e);
        }
    }

    @Override
    public void close()
    {
        try (OutputStream ignored1 = this.outputStream) {
            this.flush();
        }
        catch (IOException e) {
            throw new JcodecException(e);
        }
    }
}
