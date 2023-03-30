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
package com.github.harbby.gadtry.ioc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

public class InternalContextTest
{
    @Test
    public void getInstance3()
    {
        Assertions.assertThrows(IllegalStateException.class, ()-> {
            BindMapping bindMapping = BindMapping.create();
            InternalContext context = InternalContext.of(bindMapping);
            context.get(DeadDependency1.class);
        });
    }

    @Test
    public void getInstance4()
    {
        Assertions.assertThrows(IllegalStateException.class, ()-> {
            BindMapping bindMapping = BindMapping.create(binder -> binder.bind(HashSet.class).byCreator(() -> null));
            InternalContext context = InternalContext.of(bindMapping);
            context.get(DeadDependency1.class);
        });
    }

    @Test
    public void getInstance5()
    {
        Assertions.assertThrows(IllegalStateException.class, ()-> {
            BindMapping bindMapping = BindMapping.create(binder -> {});
            InternalContext context = InternalContext.of(bindMapping);
            context.get(DeadDependency3.class);
        });
    }

    public static class DeadDependency1
    {
        @Autowired
        public DeadDependency1(HashSet hashSet, DeadDependency2 deadDependency2) {}
    }

    public static class DeadDependency2
    {
        @Autowired
        public DeadDependency2(DeadDependency1 deadDependency1) {}
    }

    public static class DeadDependency3
    {
        @Autowired
        public DeadDependency3(DeadDependency3 deadDependency3) {}
    }
}
