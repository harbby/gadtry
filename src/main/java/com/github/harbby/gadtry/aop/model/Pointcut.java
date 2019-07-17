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
package com.github.harbby.gadtry.aop.model;

import com.github.harbby.gadtry.aop.ProxyContext;
import com.github.harbby.gadtry.function.Function1;
import com.github.harbby.gadtry.function.exception.Consumer;
import com.github.harbby.gadtry.function.exception.Function;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static com.github.harbby.gadtry.base.Throwables.throwsThrowable;

public class Pointcut
{
    private Function<ProxyContext, Object, Throwable> around = ProxyContext::proceed;

    private Consumer<Before, Exception> before;
    private Consumer<After, Exception> after;
    private Consumer<AfterThrowing, Exception> afterThrowing;
    private Consumer<AfterReturning, Exception> afterReturning;

    private final String pointName;
    private Function1<MethodInfo, Boolean> methodFilter;
    private Set<Class<?>> searchClass;

    public Pointcut(String pointName)
    {
        this.pointName = pointName;
    }

    public String getPointName()
    {
        return pointName;
    }

    public Function1<MethodInfo, Boolean> getMethodFilter()
    {
        return methodFilter;
    }

    public void setLocation(Function1<MethodInfo, Boolean> methodFilter)
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

    public Consumer<AfterThrowing, Exception> getAfterThrowing()
    {
        return afterThrowing;
    }

    public void setAfterThrowing(Consumer<AfterThrowing, Exception> afterThrowing)
    {
        this.afterThrowing = afterThrowing;
    }

    public Consumer<AfterReturning, Exception> getAfterReturning()
    {
        return afterReturning;
    }

    public void setAfterReturning(Consumer<AfterReturning, Exception> afterReturning)
    {
        this.afterReturning = afterReturning;
    }

    public Consumer<Before, Exception> getBefore()
    {
        return before;
    }

    public void setBefore(Consumer<Before, Exception> before)
    {
        this.before = before;
    }

    public Consumer<After, Exception> getAfter()
    {
        return after;
    }

    public void setAfter(Consumer<After, Exception> after)
    {
        this.after = after;
    }

    public void setAround(Function<ProxyContext, Object, Throwable> aroundHandler)
    {
        this.around = aroundHandler;
    }

    public Function<ProxyContext, Object, Throwable> getAround()
    {
        return around;
    }

    public Function<ProxyContext, Object, Throwable> buildRunHandler()
    {
        return (proxyContext) -> {
            if (this.getBefore() != null) {
                this.getBefore().apply(Before.of(proxyContext.getMethod(), proxyContext.getArgs()));
            }

            Object value = null;
            Throwable throwable = null;
            try {
                value = this.getAround().apply(proxyContext);
                if (this.getAfterReturning() != null) {
                    this.getAfterReturning().apply(AfterReturning.of(proxyContext.getMethod(),
                            proxyContext.getArgs(), value));
                }
                return value;
            }
            catch (Exception e) {
                throwable = e;
                if (this.getAfterThrowing() != null) {
                    this.getAfterThrowing().apply(AfterThrowing.of(proxyContext.getMethod(),
                            proxyContext.getArgs(), e));
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
                    this.getAfter().apply(After.of(proxyContext.getMethod(),
                            proxyContext.getArgs(), value, throwable));
                }
            }
        };
    }
}
