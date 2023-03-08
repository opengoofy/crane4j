package cn.crane4j.core.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link OperationParseException}
 *
 * @author huangchengxing
 */
public class OperationParseExceptionTest {

    @Test
    public void test() {
        Assert.assertThrows("ex!", OperationParseException.class, () -> { throw new OperationParseException("{}!", "ex"); });
        RuntimeException e = new RuntimeException("ex!");
        Assert.assertThrows("ex!", OperationParseException.class, () -> { throw new OperationParseException(e); });
    }
}
