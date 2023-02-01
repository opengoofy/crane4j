package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.support.TypeResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * test for {@link TypeDynamitedDisassembleOperation}
 *
 * @author huangchengxing
 */
public class TypeDynamitedDisassembleOperationTest {

    private static final BeanOperationParser PARSER = SimpleBeanOperations::new;
    private static final TypeResolver TYPE_RESOLVER = Object::getClass;
    private TypeDynamitedDisassembleOperation operation;

    @Before
    public void init() {
        operation = new TypeDynamitedDisassembleOperation(
            "key",
            Object.class, null,
            PARSER, TYPE_RESOLVER
        );
    }

    @Test
    public void getSourceType() {
        Assert.assertEquals(Object.class, operation.getSourceType());
    }

    @Test
    public void getInternalBeanOperations() {
        Assert.assertEquals(BigDecimal.class, operation.getInternalBeanOperations(BigDecimal.ONE).getTargetType());
        Assert.assertEquals(String.class, operation.getInternalBeanOperations("str").getTargetType());
    }

    @Test
    public void getDisassembleOperationHandler() {
        Assert.assertNull(operation.getDisassembleOperationHandler());
    }

}
