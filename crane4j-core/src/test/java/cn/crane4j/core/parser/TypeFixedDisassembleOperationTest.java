package cn.crane4j.core.parser;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * test for {@link TypeFixedDisassembleOperation}
 *
 * @author huangchengxing
 */
public class TypeFixedDisassembleOperationTest {

    private static final BeanOperations BEAN_OPERATIONS = new SimpleBeanOperations(Void.TYPE);
    private final TypeFixedDisassembleOperation operation = new TypeFixedDisassembleOperation(
        "key", Object.class, BEAN_OPERATIONS, null
    );


    @Test
    public void getSourceType() {
        Assert.assertEquals(Object.class, operation.getSourceType());
    }

    @Test
    public void getInternalBeanOperations() {
        Assert.assertEquals(BEAN_OPERATIONS, operation.getInternalBeanOperations(BigDecimal.ONE));
        Assert.assertEquals(BEAN_OPERATIONS, operation.getInternalBeanOperations("str"));
    }

    @Test
    public void getDisassembleOperationHandler() {
        Assert.assertNull(operation.getDisassembleOperationHandler());
    }
}
