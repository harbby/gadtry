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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LimitedNioChannelTest
{
    @Test
    public void readTest()
            throws URISyntaxException, IOException
    {
        URL url = LimitedNioChannelTest.class.getClassLoader().getResource("blogCatalog-dataset/readme.txt");
        File file = new File(url.toURI());
        int length = (int) file.length();
        Assert.assertEquals(length, 2032);
        ByteBuffer allBytes = ByteBuffer.allocate(length);
        ByteBuffer tmp = ByteBuffer.allocate(128);
        try (FileChannel fileChannel = new FileInputStream(file).getChannel()) {
            LimitedNioChannel nioChannel = new LimitedNioChannel(fileChannel, 0, length / 2);
            while (nioChannel.read(tmp) != -1) {
                tmp.flip();
                allBytes.put(tmp);
                tmp.clear();
            }
        }
        Assert.assertEquals(allBytes.position(), length / 2);
    }
}
