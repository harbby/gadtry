package com.github.harbby.gadtry.base;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static com.github.harbby.gadtry.base.Throwables.noCatch;
import static com.github.harbby.gadtry.base.Throwables.throwsException;

public class ThrowablesTest
{
    @Test
    public void testNoCatch()
    {
        noCatch(() -> { new URL("file:");});
        URL url1 = noCatch(() -> new URL("file:"));

        try {
            URL url = noCatch(() -> new URL("/harbby"));
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testThrowsException()
    {
        try {
            URL url = new URL("file:");
        }
        catch (IOException e) {
            throwsException(e);
        }

        try {
            try {
                URL url = new URL("/harbby");
            }
            catch (IOException e) {
                throwsException(e);
            }
            Assert.fail();
        }
        catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testThrowsExceptionClass()
            throws IOException
    {
        //强制 抛出IOException个异常
        throwsException(IOException.class);
    }
}