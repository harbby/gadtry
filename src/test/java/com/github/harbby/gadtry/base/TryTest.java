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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TryTest
{
    @Test
    public void tryOfTest()
    {
        List<String> func = new ArrayList<>();
        Try.of(() -> func.add("of"))
                .onSuccess(() -> {
                    func.add("onSuccess");
                    throw new UnsupportedOperationException();
                }).matchException(IllegalStateException.class, e -> {})
                .onFailure(e -> {
                    func.add("onFailure");
                    Assert.assertTrue(e instanceof UnsupportedOperationException);
                }).onFinally(() -> func.add("onFinally"))
                .doTry();
        Assert.assertEquals(func, Arrays.asList("of", "onSuccess", "onFailure", "onFinally"));

        AtomicInteger atomicInteger = new AtomicInteger(1);
        Try.of(() -> atomicInteger.set(10)).onSuccess(atomicInteger::getAndIncrement).doTry();
        Assert.assertEquals(11, atomicInteger.get());

        Try.of(() -> atomicInteger.set(5)).doTry();
        Assert.assertEquals(atomicInteger.get(), 5);
    }

    @Test
    public void tryValueOfTest()
    {
        List<String> func = new ArrayList<>();
        Try.valueOf(() -> func.add("of"))
                .onSuccess((v) -> {
                    func.add("onSuccess");
                    throw new UnsupportedOperationException();
                }).matchException(IllegalStateException.class, e -> false)
                .onFailure(e -> {
                    func.add("onFailure");
                    Assert.assertTrue(e instanceof UnsupportedOperationException);
                    return true;
                }).onFinally(() -> func.add("onFinally"))
                .doTry();
        Assert.assertEquals(func, Arrays.asList("of", "onSuccess", "onFailure", "onFinally"));

        Assert.assertEquals(2, Try.valueOf(() -> new AtomicInteger(1)).onSuccess(AtomicInteger::getAndIncrement).doTry().get());
        Assert.assertEquals(1, Try.valueOf(() -> 1).doTry().intValue());
        Assert.assertEquals(0, Try.valueOf(() -> 1 / 0).matchException(ArithmeticException.class, e -> 0).doTry().intValue());
    }
}
