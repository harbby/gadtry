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

import com.github.harbby.gadtry.aop.CutMode;
import com.github.harbby.gadtry.aop.ProxyContext;
import com.github.harbby.gadtry.aop.v1.MethodFilter;

import java.util.Set;

public class Pointcut
{
    private CutMode.Handler1<ProxyContext> around;

    private CutMode.Handler1<MethodInfo> before;
    private CutMode.Handler1<MethodInfo> after;
    private CutMode.Handler1<MethodInfo> afterThrowing;
    private CutMode.Handler1<MethodInfo> afterReturning;

    private final String pointName;
    private MethodFilter methodFilter;
    private Set<Class<?>> searchClass;

    public Pointcut(String pointName)
    {
        this.pointName = pointName;
    }

    public String getPointName()
    {
        return pointName;
    }

    public MethodFilter getMethodFilter()
    {
        return methodFilter;
    }

    public void setLocation(MethodFilter methodFilter)
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

    public CutMode.Handler1<MethodInfo> getAfterThrowing()
    {
        return afterThrowing;
    }

    public void setAfterThrowing(CutMode.Handler1<MethodInfo> afterThrowing)
    {
        this.afterThrowing = afterThrowing;
    }

    public CutMode.Handler1<MethodInfo> getAfterReturning()
    {
        return afterReturning;
    }

    public void setAfterReturning(CutMode.Handler1<MethodInfo> afterReturning)
    {
        this.afterReturning = afterReturning;
    }

    public CutMode.Handler1<MethodInfo> getBefore()
    {
        return before;
    }

    public void setBefore(CutMode.Handler1<MethodInfo> before)
    {
        this.before = before;
    }

    public CutMode.Handler1<MethodInfo> getAfter()
    {
        return after;
    }

    public void setAfter(CutMode.Handler1<MethodInfo> after)
    {
        this.after = after;
    }

    public void setAround(CutMode.Handler1<ProxyContext> runnable)
    {
        this.around = runnable;
    }

    public CutMode.Handler1<ProxyContext> getAround()
    {
        return around;
    }

    public CutMode.Handler1<ProxyContext> buildRunHandler()
    {
        return (proxyContext) -> {
            if (this.getBefore() != null) {
                this.getBefore().apply(proxyContext.getInfo());
            }

            try {
                if (this.getAround() != null) {
                    this.getAround().apply(proxyContext);
                }
                else {
                    proxyContext.proceed();
                }
                if (this.getAfterReturning() != null) {
                    this.getAfterReturning().apply(proxyContext.getInfo());
                }
            }
            catch (Exception e) {
                if (this.getAfterThrowing() != null) {
                    this.getAfterThrowing().apply(proxyContext.getInfo());
                }
                throw e;
            }
            finally {
                if (this.getAfter() != null) {
                    this.getAfter().apply(proxyContext.getInfo());
                }
            }
        };
    }
}
