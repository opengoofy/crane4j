package cn.createsequence.crane4j.core.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link Grouped}
 *
 * @author huangchengxing
 */
public class SortedTest {

    @Test
    public void getSort() {
        Assert.assertEquals(Integer.MAX_VALUE, new Foo().getSort());
    }

    private static class Foo implements Sorted {
    }
}
