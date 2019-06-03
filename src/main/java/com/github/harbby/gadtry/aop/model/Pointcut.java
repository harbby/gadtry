/*
 * Copyright (C) 2018 The Harbby Authors
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
package com.github.harbby.gadtry.aop.model;

import com.github.harbby.gadtry.aop.ProxyContext;
import com.github.harbby.gadtry.function.exception.Consumer;
import com.github.harbby.gadtry.function.exception.Function;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static com.github.harbby.gadtry.base.Throwables.throwsThrowable;

public class Pointcut
{
    private Function<ProxyContext, Object> around = ProxyContext::proceed;

    private Consumer<MethodInfo> before;
    private Consumer<MethodInfo> after;
    private Consumer<MethodInfo> afterThrowing;
    private Consumer<MethodInfo> afterReturning;

    private final String pointName;
    private Function<MethodInfo, Boolean> methodFilter;
    private Set<Class<?>> searchClass;

    public Pointcut(String pointName)
    {
        this.pointName = pointName;
    }

    public String getPointName()
    {
        return pointName;
    }

    public Function<MethodInfo, Boolean> getMethodFilter()
    {
        return methodFilter;
    }

    public void setLocation(Function<MethodInfo, Boolean> methodFilter)
    {
        this.methodFilter = methodFilter;
    }

    public void setSearchClass(Set<Class<?>> searchClass)
    {
        this.searchClass = searchClass;
    }

    public Set<Class<?>> getSearchClass()
    {
        return searchClass;
    }

    public Consumer<MethodInfo> getAfterThrowing()
    {
        return afterThrowing;
    }

    public void setAfterThrowing(Consumer<MethodInfo> afterThrowing)
    {
        this.afterThrowing = afterThrowing;
    }

    public Consumer<MethodInfo> getAfterReturning()
    {
        return afterReturning;
    }

    public void setAfterReturning(Consumer<MethodInfo> afterReturning)
    {
        this.afterReturning = afterReturning;
    }

    public Consumer<MethodInfo> getBefore()
    {
        return before;
    }

    public void setBefore(Consumer<MethodInfo> before)
    {
        this.before = before;
    }

    public Consumer<MethodInfo> getAfter()
    {
        return after;
    }

    public void setAfter(Consumer<MethodInfo> after)
    {
        this.after = after;
    }

    public void setAround(Function<ProxyContext, Object> aroundHandler)
    {
        this.around = aroundHandler;
    }

    public Function<ProxyContext, Object> getAround()
    {
        return around;
    }

    public Function<ProxyContext, Object> buildRunHandler()
    {
        return (proxyContext) -> {
            Object value = null;

            if (this.getBefore() != null) {
                this.getBefore().apply(proxyContext.getInfo());
            }

            try {
                value = this.getAround().apply(proxyContext);

                if (this.getAfterReturning() != null) {
                    this.getAfterReturning().apply(proxyContext.getInfo());
                }
            }
            catch (Exception e) {
                if (this.getAfterThrowing() != null) {
                    this.getAfterThrowing().apply(proxyContext.getInfo());
                }
                if (e instanceof InvocationTargetException) {
                    throw throwsThrowable(((InvocationTargetException) e).getTargetException());
                }
                else {
                    throw e;
                }
            }
            finally {
                if (this.getAfter() != null) {
                    this.getAfter().apply(proxyContext.getInfo());
                }
            }

            return value;
        };
    }
}
