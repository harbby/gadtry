package com.github.harbby.gadtry.aop.model;

import java.lang.reflect.Method;

public interface AfterReturning
        extends Before
{
    /**
     * get Method return value
     */
    public Object getValue();

    public static AfterReturning of(Method method, Object[] args, Object value)
    {
        return new AfterReturning()
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
            public Object getValue()
            {
                return value;
            }
        };
    }
}
