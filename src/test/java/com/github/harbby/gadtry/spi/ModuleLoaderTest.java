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
package com.github.harbby.gadtry.spi;

import com.github.harbby.gadtry.aop.AopGo;
import com.github.harbby.gadtry.base.Files;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ModuleLoaderTest
{
    private final Map<String, Module<Driver>> modulesMap = new HashMap<>();
    private final ModuleLoader<Driver> moduleLoader = ModuleLoader.<Driver>newScanner()
            .setScanDir(new File(this.getClass().getClassLoader().getResource("version1").getFile()).getParentFile())
            .setPlugin(Driver.class)
            .setLoadHandler(module -> {
                modulesMap.put(module.getName(), module);
                System.out.println("load module " + Arrays.asList(module.moduleFile().list()) +
                        "  " + "loadTime " + module.getLoadTime());
            })
            .setParentLoader(this.getClass().getClassLoader())
            .accessSpiPackages(Arrays.asList("com.github.harbby.gadtry.aop", "version2"))
            .load();

    public ModuleLoaderTest()
            throws IOException
    {}

    @Test
    public void newPluginLoadTest()
            throws IOException
    {
        moduleLoader.reload(modulesMap);
        Assert.assertEquals(2, modulesMap.values().stream().mapToInt(driverModule -> driverModule.getPlugins().size()).sum());
    }

    @Test
    public void onlyAccessSpiPackagesTest()
            throws ClassNotFoundException, IOException
    {
        Module module = modulesMap.values().stream().findFirst().get();
        ClassLoader moduleClassLoader = module.getModuleClassLoader();

        try {
            moduleClassLoader.loadClass(ModuleLoader.class.getName());
            Assert.fail();
        }
        catch (ClassNotFoundException ignored) {
        }

        moduleClassLoader.loadClass(AopGo.class.getName());
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
        Assert.assertEquals(3, modulesMap.size());
        Module module = modulesMap.values().stream().findFirst().get();
        File reloadFile = new File(module.moduleFile(), "reload");
        File reloadFile2 = new File(module.moduleFile().getParentFile(), "reload");
        try {
            byte[] value = String.valueOf(System.currentTimeMillis()).getBytes(UTF_8);
            java.nio.file.Files.write(Paths.get(reloadFile.toURI()), value, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            reloadFile2.mkdirs();
            moduleLoader.reload(modulesMap);
            Assert.assertEquals(4, modulesMap.size());
        }
        finally {
            reloadFile.delete();
            reloadFile2.delete();
            moduleLoader.reload(modulesMap);
            Assert.assertEquals(3, modulesMap.size());
        }
    }

    @Test
    public void loadFilterTest()
            throws Exception
    {
        Map<String, Module<Driver>> modules = new HashMap<>();
        final ModuleLoader<Driver> moduleLoader = ModuleLoader.<Driver>newScanner()
                .setScanDir(() -> {
                    return Arrays.asList(new File(this.getClass().getClassLoader().getResource("version1").getFile()),
                            new File(this.getClass().getClassLoader().getResource("version2").getFile()));
                })
                .setPlugin(Driver.class)
                .setLoadHandler(module -> {
                    modules.put(module.getName(), module);
                })
                .setModuleDepFilter(file -> {
                    if ("version2".equalsIgnoreCase(file.getName())) {
                        return new ArrayList<>();
                    }
                    else {
                        return Files.listFiles(file, false);
                    }
                })
                .accessSpiPackages(Collections.emptyList())
                .load();

        Assert.assertEquals(1, modules.values().stream().mapToInt(driverModule -> driverModule.getPlugins().size()).sum());
    }
}
