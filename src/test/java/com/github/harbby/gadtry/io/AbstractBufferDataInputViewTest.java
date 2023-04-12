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

import com.github.harbby.gadtry.jcodec.AbstractInputView;
import com.github.harbby.gadtry.jcodec.AbstractOutputView;
import com.github.harbby.gadtry.jcodec.EncoderChecker;
import com.github.harbby.gadtry.jcodec.InputView;
import com.github.harbby.gadtry.jcodec.Jcodec;
import com.github.harbby.gadtry.jcodec.OutputView;
import com.github.harbby.gadtry.jcodec.Serializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AbstractBufferDataInputViewTest
{
    @Disabled
    @Test
    public void utf16CharCountTest()
    {
        Serializer<Integer> utf16CharCountEncoder = new Serializer<Integer>()
        {
            @Override
            public void write(Jcodec jcodec, OutputView output, Integer value)
            {
                AbstractOutputView outputView = (AbstractOutputView) output;
                outputView.writeUtf16CharCount(value);
            }

            @Override
            public Integer read(Jcodec jcodec, InputView input, Class<? extends Integer> typeClass)
            {
                AbstractInputView inputView = (AbstractInputView) input;
                return inputView.readUtf16CharCount();
            }
        };
        EncoderChecker<Integer> checker = new EncoderChecker<>(utf16CharCountEncoder, int.class);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            byte[] bytes = checker.encoder(i);
            int rs = checker.decoder(bytes);
            Assertions.assertEquals(i, rs);
        }
    }

    @Disabled
    @Test
    public void utf16CharCountTest2()
    {
        Serializer<Integer> utf16CharCountEncoder = new Serializer<Integer>()
        {
            @Override
            public void write(Jcodec jcodec, OutputView output, Integer value)
            {
                AbstractOutputView outputView = (AbstractOutputView) output;
                outputView.writeUtf16CharCount(value);
            }

            @Override
            public Integer read(Jcodec jcodec, InputView input, Class<? extends Integer> typeClass)
            {
                AbstractInputView inputView = (AbstractInputView) input;
                return inputView.readUtf16CharCount();
            }
        };
        EncoderChecker<Integer> checker = new EncoderChecker<>(utf16CharCountEncoder, int.class);
        for (int i = Integer.MIN_VALUE; i < 0; i++) {
            byte[] bytes = checker.encoder(i);
            int rs = checker.decoder(bytes);
            Assertions.assertEquals(i, rs);
        }
    }
}
