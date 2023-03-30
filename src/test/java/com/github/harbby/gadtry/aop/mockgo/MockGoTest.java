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
import com.github.harbby.gadtry.base.JavaTypes;
import com.github.harbby.gadtry.base.Throwables;
import com.github.harbby.gadtry.collection.ImmutableList;
import com.github.harbby.gadtry.collection.MutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static com.github.harbby.gadtry.aop.MockGo.doAnswer;
import static com.github.harbby.gadtry.aop.MockGo.doAround;
import static com.github.harbby.gadtry.aop.MockGo.doReturn;
import static com.github.harbby.gadtry.aop.MockGo.doThrow;
import static com.github.harbby.gadtry.aop.MockGo.when;
import static com.github.harbby.gadtry.aop.mockgo.MockGoArgument.anyInt;

public class MockGoTest
{
    private static void spyListChecks(List<String> proxy)
    {
        Assertions.assertEquals(proxy.size(), 3);
        doReturn(7).when(proxy).size();
        doAround(proxyContext -> "123").when(proxy).toString();
        doThrow(new RuntimeException("mockDoThrow")).when(proxy).get(anyInt());

        Assertions.assertEquals(proxy.size(), 7);
        Assertions.assertEquals("123", proxy.toString());
        Assertions.assertTrue(proxy.stream() instanceof Stream);
        try {
            proxy.get(0);
            Assertions.fail();
        }
        catch (RuntimeException e) {
            Assertions.assertEquals(e.getMessage(), "mockDoThrow");
        }
    }

    @Test
    public void mockSpyByDoWhen()
    {
        spyListChecks(MockGo.spy(ImmutableList.of("1", "2", "3")));
        spyListChecks(MockGo.spy(MutableList.of("1", "2", "3")));

        List<String> proxy3 = MockGo.spy(JavaTypes.classTag(ArrayList.class));
        Assertions.assertTrue(proxy3 instanceof ArrayList);
    }

    @Test
    public void mockSpyWhenMockGo()
    {
        List<String> a1 = MockGo.spy(ImmutableList.of("1", "2", "3"));
        Assertions.assertEquals(3, a1.size());
        MockGo.doReturn(99).when(a1).size();
        Assertions.assertEquals(99, a1.size());
    }

    @Test
    public void disableSuperMethodMockSpyByWhen()
    {
        List<String> proxy = MockGo.spy(ImmutableList.of("1", "2", "3"));
        when(proxy.toString()).thenAround(proxyContext -> "123");
        Assertions.assertEquals("123", proxy.toString()); //check disableSuperMethod()
    }

    @Test
    public void mockSpyByWhen()
    {
        List<String> proxy = MockGo.spy(MutableList.of("1", "2", "3"));
        when(proxy.size()).thenReturn(7);
        when(proxy.toString()).thenAround(proxyContext -> "123");
        when(proxy.get(anyInt())).thenThrow(new IOException("mockDoThrow"));

        Assertions.assertEquals(proxy.size(), 7);
        Assertions.assertEquals("123", proxy.toString()); //check disableSuperMethod()
        try {
            proxy.get(0);
            Assertions.fail();
            Throwables.throwThrowable(IOException.class);
        }
        catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "mockDoThrow");
        }
    }

    @Test
    public void whenDoesNotSelectAnyMethod()
    {
        Assertions.assertThrows(MockGoException.class, ()-> {
            when(123).thenReturn(1);
            when(123).thenReturn(1);
        });
    }

    @Test
    public void mockUseDoWhen()
    {
        List<String> proxy = MockGo.mock(List.class);
        doReturn(7).when(proxy).size();
        doAnswer(proxyContext -> {
            Assertions.assertEquals(proxyContext.getMethod().getName(), "toString");
            proxyContext.proceed();
            return "123";
        }).when(proxy).toString();
        doThrow(new RuntimeException("mockDoThrow")).when(proxy).get(anyInt());

        Assertions.assertEquals(proxy.size(), 7);
        Assertions.assertEquals("123", proxy.toString());
        Assertions.assertEquals(null, proxy.stream());
        try {
            proxy.get(0);
            Assertions.fail();
        }
        catch (RuntimeException e) {
            Assertions.assertEquals(e.getMessage(), "mockDoThrow");
        }
    }

    @Test
    public void duplicateMockWhenThen()
    {
        List<String> proxy = MockGo.mock(List.class);
        when(proxy.size()).thenReturn(7);
        Assertions.assertEquals(proxy.size(), 7);
        when(proxy.size()).thenReturn(8);
        Assertions.assertEquals(proxy.size(), 8);
    }

    @Test
    public void disableSuperMethodMockWhenThen()
    {
        HashMap<?, ?> proxy = MockGo.mock(HashMap.class);
        when(proxy.toString()).thenReturn("disableSuperMethodMockWhenThen");
        Assertions.assertEquals(proxy.toString(), "disableSuperMethodMockWhenThen");
        when(proxy.size()).thenReturn(7);
        Assertions.assertEquals(proxy.size(), 7);
        when(proxy.size()).thenReturn(8);
        Assertions.assertEquals(proxy.size(), 8);
    }

    @Test
    public void duplicateMockDoWhen()
    {
        List<String> proxy = MockGo.mock(List.class);
        doReturn(7).when(proxy).size();
        Assertions.assertEquals(proxy.size(), 7);
        doReturn(8).when(proxy).size();
        Assertions.assertEquals(proxy.size(), 8);
    }

    @Test
    public void verify()
    {
//        Mockito.verify(proxy, Mockito.times(5)).get(anyInt());
//        Mockito.verify(proxy, Mockito.times(5)).get(2);
//        Mockito.verify(proxy, VerificationModeFactory.only()).size();
//        Mockito.verify(proxy, Mockito.timeout(1)).size();
//        Assertions.assertEquals(Mockito.analaysis(proxy.get(2)).getTirggTimes(), 5);
    }

    @Test
    public void mockByWhen()
    {
        List<String> proxy = MockGo.mock(List.class);
        when(proxy.size()).thenReturn(7);
        when(proxy.toString()).thenAround(proxyContext -> "123");
        when(proxy.get(anyInt())).thenThrow(new RuntimeException("mockDoThrow"));

        Assertions.assertEquals(proxy.size(), 7);
        Assertions.assertEquals("123", proxy.toString());
        Assertions.assertEquals(null, proxy.stream());
        try {
            proxy.get(0);
            Assertions.fail();
        }
        catch (RuntimeException e) {
            Assertions.assertEquals(e.getMessage(), "mockDoThrow");
        }
    }

    @Test
    public void doNothingTest()
    {
        List<String> proxy = MockGo.spy(MutableList.of("a", "b"));
        MockGo.doNothing().when(proxy).clear();
        proxy.clear();
        Assertions.assertEquals(2, proxy.size());

        MockGo.doNothing().when(proxy).size();
        Assertions.assertThrows(MockGoException.class, ()-> {
            proxy.size();
        });
    }
}
