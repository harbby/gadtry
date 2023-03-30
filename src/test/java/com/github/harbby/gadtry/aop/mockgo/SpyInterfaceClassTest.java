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
import org.junit.jupiter.api.Test;

import static com.github.harbby.gadtry.aop.MockGo.doReturn;
import static com.github.harbby.gadtry.aop.MockGo.when;

public class SpyInterfaceClassTest
{
    private interface UserInterface0
    {
        public abstract String getName();

        default String getUpperName()
        {
            String name = this.getName();
            return name == null ? null : name.toUpperCase();
        }
    }

    private interface UserInterface
            extends UserInterface0
    {
        public abstract int getAge();

        default String getNameAge()
        {
            return this.getName() + ":" + this.getAge();
        }
    }

    private static class User0
            implements UserInterface
    {
        @Override
        public String getName()
        {
            return "user0";
        }

        @Override
        public int getAge()
        {
            return 18;
        }
    }

    @Test
    public void mockInterfaceTest()
    {
        UserInterface user = MockGo.mock(UserInterface.class);
        Assertions.assertEquals(user.getAge(), 0);
        Assertions.assertNull(user.getName());
        Assertions.assertNull(user.getUpperName());
        Assertions.assertNull(user.getNameAge());

        when(user.getName()).thenReturn("hello");
        Assertions.assertNull(user.getUpperName());
        doReturn(666).when(user).getAge();
        Assertions.assertNull(user.getNameAge());
        when(user.getUpperName()).thenReturn("SUCCEED");
        Assertions.assertEquals(user.getUpperName(), "SUCCEED");
    }

    @Test
    public void spyInterfaceWhenThanTest()
    {
        UserInterface user = MockGo.spy(UserInterface.class);
        Assertions.assertEquals(user.getNameAge(), "null:0");

        when(user.getNameAge()).thenReturn("success2");
        Assertions.assertEquals(user.getNameAge(), "success2");
    }

    @Test
    public void spyInterfaceTest()
    {
        UserInterface user = MockGo.spy(UserInterface.class);
        Assertions.assertEquals(user.getAge(), 0);
        Assertions.assertNull(user.getName());
        Assertions.assertNull(user.getUpperName());
        Assertions.assertEquals(user.getNameAge(), "null:0");

        when(user.getName()).thenReturn("hello");
        Assertions.assertEquals(user.getUpperName(), "HELLO");
        doReturn(666).when(user).getAge();
        Assertions.assertEquals(user.getNameAge(), "hello:666");
        when(user.getUpperName()).thenReturn("SUCCEED");
        Assertions.assertEquals(user.getUpperName(), "SUCCEED");
    }

    @Test
    public void mockUser0ClassTest()
    {
        UserInterface user = MockGo.mock(User0.class);
        Assertions.assertEquals(user.getAge(), 0);
        Assertions.assertNull(user.getName());
        Assertions.assertNull(user.getUpperName());
        Assertions.assertNull(user.getNameAge());

        when(user.getName()).thenReturn("hello");
        doReturn(666).when(user).getAge();
        Assertions.assertNull(user.getNameAge());
        Assertions.assertNull(user.getUpperName());
        when(user.getUpperName()).thenReturn("SUCCEED");
        Assertions.assertEquals(user.getUpperName(), "SUCCEED");
    }

    @Test
    public void spyUser0ClassTest()
    {
        UserInterface user = MockGo.spy(User0.class);
        Assertions.assertEquals(user.getAge(), 18);
        Assertions.assertEquals(user.getName(), "user0");
        Assertions.assertEquals(user.getUpperName(), "USER0");
        Assertions.assertEquals(user.getNameAge(), "user0:18");

        when(user.getName()).thenReturn("hello");
        doReturn(666).when(user).getAge();
        Assertions.assertEquals(user.getNameAge(), "hello:666");
        Assertions.assertEquals(user.getUpperName(), "HELLO");
        when(user.getUpperName()).thenReturn("SUCCEED");
        Assertions.assertEquals(user.getUpperName(), "SUCCEED");
    }
}
