package com.github.harbby.gadtry.aop.model;

import java.lang.reflect.Method;

public interface Before
{
    /**
     * @return getMethodName
     */
    public default String getName()
    {
        return getMethod().getName();
    }

    Method getMethod();

    default Object getArgument(int index)
    {
        return this.getArgs()[index];
    }

    Object[] getArgs();

    public static Before of(Method method, Object[] args)
    {
        return new Before()
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
        };
    }
}
