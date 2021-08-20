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
package com.github.harbby.gadtry.aop.aopgo;

import com.github.harbby.gadtry.aop.event.JoinPoint;
import com.github.harbby.gadtry.aop.mockgo.Mock;
import com.github.harbby.gadtry.aop.mockgo.MockGoJUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static com.github.harbby.gadtry.aop.MockGo.when;

@RunWith(MockGoJUnitRunner.class)
public class AroundHandlerTest
{
    @Mock private JoinPoint joinPoint;

    @Test(expected = SQLException.class)
    public void doAfterThrowing()
            throws Throwable
    {
        when(joinPoint.proceed()).thenThrow(new SQLException("thenThrow"));
        AroundHandler.doAfterThrowing(afterThrowing -> {
            Assert.assertEquals(afterThrowing.getThrowable().getMessage(), "thenThrow");
        }).apply(joinPoint);
    }
}
