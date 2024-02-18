package cn.crane4j.core.parser.operation;

import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.SimpleBeanOperations;
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
    private final TypeFixedDisassembleOperation operation = TypeFixedDisassembleOperation.builder()
        .key("key")
        .sourceType(Object.class)
        .internalBeanOperations(BEAN_OPERATIONS)
        .build();

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
