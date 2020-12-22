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
package com.github.harbby.gadtry.easyspi;

import com.github.harbby.gadtry.aop.AopFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PluginLoaderTest
{
    private final AtomicInteger closeNumber = new AtomicInteger(0);
    private final PluginLoader<Driver> pluginLoader = PluginLoader.<Driver>newScanner()
            .setScanDir(new File(this.getClass().getClassLoader().getResource("version1").getFile()).getParentFile())
            .setPlugin(Driver.class)
            .setLoadHandler(module -> {
                System.out.println("load module " + Arrays.asList(module.getModulePath().list()) +
                        "  " + "loadTime " + module.getLoadTime());
            })
            .setCloseHandler(module -> {
                System.out.println("close module " + module.getName());
                closeNumber.getAndIncrement();
            })
            .setParentLoader(this.getClass().getClassLoader())
            .onlyAccessSpiPackages(Arrays.asList("com.github.harbby.gadtry.aop", "version2"))
            .load();

    public PluginLoaderTest()
            throws IOException
    {}

    @Test
    public void newPluginLoadTest()
    {
        pluginLoader.reload();
        Assert.assertEquals(2, pluginLoader.getPlugins().size());
    }

    @Test
    public void onlyAccessSpiPackagesTest()
            throws ClassNotFoundException, IOException
    {
        Module module = pluginLoader.getModules().get(0);
        ClassLoader moduleClassLoader = module.getModuleClassLoader();

        try {
            moduleClassLoader.loadClass(PluginLoader.class.getName());
            Assert.fail();
        }
        catch (ClassNotFoundException ignored) {
        }

        moduleClassLoader.loadClass(AopFactory.class.getName());
        moduleClassLoader.loadClass(AtomicInteger.class.getName());
        Assert.assertTrue(Driver.class.isAssignableFrom(moduleClassLoader.loadClass("org.h2.Driver")));

        Assert.assertNull(moduleClassLoader.getResource("version1"));

        Assert.assertNotNull(moduleClassLoader.getResources("version2").nextElement());
        Assert.assertNotNull(moduleClassLoader.getResource("version2"));

        Assert.assertNotNull(this.getClass().getClassLoader().getResource("version1"));
    }

    @Test
    public void reloadTest()
            throws IOException
    {
        Assert.assertEquals(3, pluginLoader.getModules().size());
        Module module = pluginLoader.getModules().get(0);
        File reloadFile = new File(module.getModulePath(), "reload");
        File reloadFile2 = new File(module.getModulePath().getParentFile(), "reload");
        try {
            byte[] value = String.valueOf(System.currentTimeMillis()).getBytes(UTF_8);
            Files.write(Paths.get(reloadFile.toURI()), value, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            reloadFile2.mkdirs();
            pluginLoader.reload();
            Assert.assertEquals(1, closeNumber.get());
            Assert.assertEquals(4, pluginLoader.getModules().size());
            Assert.assertEquals(2, pluginLoader.getPlugins().size());
        }
        finally {
            reloadFile.delete();
            reloadFile2.delete();
            pluginLoader.reload();
            Assert.assertEquals(3, pluginLoader.getModules().size());
        }
    }

    @Test
    public void loadFilterTest()
            throws IOException
    {
        final PluginLoader<Driver> pluginLoader = PluginLoader.<Driver>newScanner()
                .setScanDir(() -> {
                    return Arrays.asList(new File(this.getClass().getClassLoader().getResource("version1").getFile()),
                            new File(this.getClass().getClassLoader().getResource("version2").getFile()));
                })
                .setPlugin(Driver.class)
                .setLoadHandler(module -> {})
                .setCloseHandler(module -> {})
                .setModuleDepFilter(file -> {
                    if ("version2".equalsIgnoreCase(file.getName())) {
                        return new ArrayList<>();
                    }
                    else {
                        return Arrays.stream(file.listFiles()).collect(Collectors.toList());
                    }
                })
                .load();

        Assert.assertEquals(1, pluginLoader.getPlugins().size());
    }
}
