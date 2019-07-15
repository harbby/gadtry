package com.github.harbby.gadtry.aop.model;

import java.lang.reflect.Method;

public interface AfterThrowing
        extends Before
{
    public Throwable getThrowable();

    public static AfterThrowing of(Method method, Object[] args, Throwable e)
    {
        return new AfterThrowing()
        {
            @Override
            public Method getMethod()
            {
                return method;
            }

            @Override
            public Object[] getArgs()
            {
                return args;
            }

            @Override
            public Throwable getThrowable()
            {
                return e;
            }
        };
    }
}
