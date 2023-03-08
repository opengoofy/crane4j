package cn.crane4j.core.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link OperationExecuteException}
 *
 * @author huangchengxing
 */
public class OperationExecuteExceptionTest {

    @Test
    public void test() {
        Assert.assertThrows("ex!", OperationExecuteException.class, () -> { throw new OperationExecuteException("{}!", "ex"); });
        RuntimeException e = new RuntimeException("ex!");
        Assert.assertThrows("ex!", OperationExecuteException.class, () -> { throw new OperationExecuteException(e); });
    }
}
