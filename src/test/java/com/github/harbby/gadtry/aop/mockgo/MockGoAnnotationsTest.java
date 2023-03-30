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
package com.github.harbby.gadtry.aop.mockgo;

import com.github.harbby.gadtry.aop.MockGo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class MockGoAnnotationsTest
{
    @Mock
    private List<String> list;
    @Mock
    private Map<String, Integer> map;

    @InjectMock
    private InjectMockClass injectMockClass;

    @BeforeEach
    public void init()
    {
        MockGo.initMocks(this);
    }

    @Test
    public void initMocks()
    {
        Assertions.assertTrue(list == injectMockClass.getList());
        Assertions.assertTrue(map == injectMockClass.getMap());
    }

    public static class InjectMockClass
    {
        private final List<String> list;
        private final Map<String, Integer> map;

        public InjectMockClass(List<String> list, Map<String, Integer> map)
        {
            this.list = list;
            this.map = map;
        }

        public List<String> getList()
        {
            return list;
        }

        public Map<String, Integer> getMap()
        {
            return map;
        }
    }
}
