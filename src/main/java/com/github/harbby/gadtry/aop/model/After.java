package com.github.harbby.gadtry.aop.model;

import java.lang.reflect.Method;

public interface After
        extends Before, AfterReturning, AfterThrowing
{
    public boolean isSuccess();

    public static After of(Method method, Object[] args, Object value, Throwable e)
    {
        return new After()
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

            @Override
            public Object getValue()
            {
                return value;
            }

            @Override
            public boolean isSuccess()
            {
                return e != null;
            }
        };
    }
}
