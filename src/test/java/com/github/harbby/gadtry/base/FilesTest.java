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

import java.io.File;
import java.util.List;

public class FilesTest
{
    @Test
    public void listFilesRecursiveGiveFalse()
    {
        File file = new File(this.getClass().getClassLoader().getResource("version1").getFile()).getParentFile();
        List<File> fileList = Files.listFiles(file, false, pathname -> {
            return true;
        });
        Assert.assertEquals(fileList.size(), 0);
    }

    @Test
    public void listFilesGiveFile()
    {
        File file = new File(this.getClass().getClassLoader().getResource("version1/h2-1.4.191.jar").getFile());
        List<File> fileList = Files.listFiles(file, true, pathname -> {
            return true;
        });
        Assert.assertEquals(fileList.size(), 1);
    }

    @Test
    public void listFilesGiveNotFoundFile()
    {
        File file = new File(System.currentTimeMillis() + "");
        List<File> fileList = Files.listFiles(file, true, pathname -> {
            return true;
        });
        Assert.assertEquals(fileList.size(), 0);
    }
}
