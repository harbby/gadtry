package com.github.harbby.gadtry.collection.immutable;

import com.github.harbby.gadtry.base.Serializables;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ImmutableListTest
{

    @Test
    public void serializableTest()
            throws IOException, ClassNotFoundException
    {

        List<Integer> list = ImmutableList.of(1, 2, 3);
        byte[] bytes = Serializables.serialize((Serializable) list);
        List<Integer> out = Serializables.byteToObject(bytes);
        Assert.assertEquals(list, out);
        Assert.assertEquals(out, Arrays.asList(1, 2, 3));
    }
}