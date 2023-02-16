package cn.crane4j.core.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link CraneException}
 *
 * @author huangchengxing
 */
public class CraneExceptionTest {

    @Test
    public void test() {
        Assert.assertThrows("ex!", CraneException.class, () -> { throw new CraneException("{}!", "ex"); });
    }
}
