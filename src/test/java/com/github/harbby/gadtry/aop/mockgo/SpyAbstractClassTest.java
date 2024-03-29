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

import static com.github.harbby.gadtry.aop.MockGo.doAnswer;
import static com.github.harbby.gadtry.aop.MockGo.doReturn;
import static com.github.harbby.gadtry.aop.MockGo.when;

public class SpyAbstractClassTest
{
    private abstract static class AbstractUser
    {
        public abstract String getName();

        public abstract int getAge();

        public int getValue()
        {
            return 666;
        }

        protected String getNameAge()
        {
            return this.getName() + ":" + this.getAge();
        }
    }

    private static class User1
            extends AbstractUser
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
    public void spyAbstractUserClassTest()
    {
        AbstractUser user = MockGo.spy(AbstractUser.class);
        Assertions.assertEquals(user.getAge(), 0);
        Assertions.assertNull(user.getName());
        Assertions.assertEquals(user.getNameAge(), "null:0");

        when(user.getName()).thenReturn("hello");
        doReturn(666).when(user).getAge();
        Assertions.assertEquals(user.getNameAge(), "hello:666");
    }

    @Test
    public void spyUser1ClassTest()
    {
        AbstractUser user = MockGo.spy(User1.class);
        Assertions.assertEquals(user.getAge(), 18);
        Assertions.assertEquals(user.getName(), "user0");
        Assertions.assertEquals(user.getNameAge(), "user0:18");

        when(user.getName()).thenReturn("hello");
        doReturn(666).when(user).getAge();
        Assertions.assertEquals(user.getNameAge(), "hello:666");

        doReturn("check_getNameAge").when(user).getNameAge();
        Assertions.assertEquals(user.getNameAge(), "check_getNameAge");

        Assertions.assertEquals(user.getValue(), 666);
        doAnswer(p -> (int) p.proceed() + 1).when(user).getValue();
        Assertions.assertEquals(user.getValue(), 667);
    }
}
