package cn.crane4j.core.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link Crane4jException}
 *
 * @author huangchengxing
 */
public class Crane4jExceptionTest {

    @Test
    public void test() {
        Assert.assertThrows("ex!", Crane4jException.class, () -> { throw new Crane4jException("{}!", "ex"); });
    }
}
