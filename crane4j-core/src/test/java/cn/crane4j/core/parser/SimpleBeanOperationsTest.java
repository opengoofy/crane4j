package cn.crane4j.core.parser;

import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.parser.operation.TypeFixedDisassembleOperation;
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
        Assert.assertEquals(Object.class, operations.getSource());
    }

    @Test
    public void getAssembleOperations() {
        Assert.assertTrue(operations.getAssembleOperations().isEmpty());
    }

    @Test
    public void putAssembleOperations() {
        Assert.assertTrue(operations.getAssembleOperations().isEmpty());
        AssembleOperation operation = new SimpleAssembleOperation(
            "key", Integer.MIN_VALUE, Collections.emptySet(), "empty", null
        );
        operations.addAssembleOperations(operation);
        operations.addAssembleOperations(operation);
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
        operations.addDisassembleOperations(operation);
        operations.addDisassembleOperations(operation);
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
