package cn.crane4j.core.parser;

import cn.crane4j.core.container.Container;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link SimpleBeanOperations}
 *
 * @author huangchengxing
 */
public class SimpleBeanOperationsTest {

    private SimpleBeanOperations operations;

    @Before
    public void init() {
        operations = new SimpleBeanOperations(Object.class);
    }

    @Test
    public void getTargetType() {
        Assert.assertEquals(Object.class, operations.getTargetType());
    }

    @Test
    public void getAssembleOperations() {
        Assert.assertTrue(operations.getAssembleOperations().isEmpty());
    }

    @Test
    public void putAssembleOperations() {
        Assert.assertTrue(operations.getAssembleOperations().isEmpty());
        AssembleOperation operation = new SimpleAssembleOperation(
            "key", Integer.MIN_VALUE, Collections.emptySet(), Container.empty(), null
        );
        operations.putAssembleOperations(operation);
        operations.putAssembleOperations(operation);
        Assert.assertEquals(1, operations.getAssembleOperations().size());
    }

    @Test
    public void getDisassembleOperations() {
        Assert.assertTrue(operations.getDisassembleOperations().isEmpty());
    }

    @Test
    public void putDisassembleOperations() {
        Assert.assertTrue(operations.getDisassembleOperations().isEmpty());
        DisassembleOperation operation = new TypeFixedDisassembleOperation(
            "key", Object.class, null, null
        );
        operations.putDisassembleOperations(operation);
        operations.putDisassembleOperations(operation);
        Assert.assertEquals(1, operations.getDisassembleOperations().size());
    }

    @Test
    public void isActive() {
        Assert.assertFalse(operations.isActive());
    }

    @Test
    public void setActive() {
        Assert.assertFalse(operations.isActive());
        operations.setActive(true);
        Assert.assertTrue(operations.isActive());
    }

}
