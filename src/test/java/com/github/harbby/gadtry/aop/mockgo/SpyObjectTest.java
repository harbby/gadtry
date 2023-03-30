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

import static com.github.harbby.gadtry.aop.MockGo.when;

public class SpyObjectTest
{
    abstract static class AbstractUser
    {
        private final int value = 666;
        protected String name;

        AbstractUser(String name)
        {
            this.name = name;
        }

        public abstract String getName();

        public abstract int getAge();

        public int getValue()
        {
            return value;
        }

        String getNameAge()
        {
            return this.getName() + ":" + this.getAge();
        }
    }

    static class User
            extends AbstractUser
    {
        private final int age;

        User(String name, int age)
        {
            super(name);
            this.age = age;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public int getAge()
        {
            return age;
        }
    }

    @Test
    public void spyUserObjectWhenThenTest()
    {
        User old = new User("user", 1);
        User user = MockGo.spy(old);

        when(user.getNameAge()).thenReturn("success");
        Assertions.assertEquals(user.getNameAge(), "success");
    }

    @Test
    public void spyUserObjectTest()
    {
        User old = new User("user", 1);
        User user = MockGo.spy(old);

        Assertions.assertEquals(user.getNameAge(), "user:1");
        Assertions.assertEquals(user.getName(), "user");
        Assertions.assertEquals(user.getValue(), 666);

        when(user.getName()).thenReturn("aaa");
        when(user.getAge()).thenReturn(18);
        Assertions.assertEquals(user.getName(), "aaa");
        Assertions.assertEquals(user.getAge(), 18);
        Assertions.assertEquals(user.getNameAge(), "aaa:18");
    }
}
