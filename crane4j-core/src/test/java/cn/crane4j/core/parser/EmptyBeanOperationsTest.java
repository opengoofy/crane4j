package cn.crane4j.core.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link BeanOperations.EmptyBeanOperations}
 *
 * @author huangchengxing
 */
public class EmptyBeanOperationsTest {

    private BeanOperations operations;

    @Before
    public void init() {
        this.operations = BeanOperations.empty();
    }

    @Test
    public void getSource() {
        Assert.assertNull(this.operations.getSource());
    }

    @Test
    public void getAssembleOperations() {
        Assert.assertTrue(this.operations.getAssembleOperations().isEmpty());
    }

    @Test
    public void addAssembleOperations() {
        Assert.assertThrows(UnsupportedOperationException.class, () -> this.operations.addAssembleOperations(null));
    }

    @Test
    public void getDisassembleOperations() {
        Assert.assertTrue(this.operations.getDisassembleOperations().isEmpty());
    }

    @Test
    public void addDisassembleOperations() {
        Assert.assertThrows(UnsupportedOperationException.class, () -> this.operations.addDisassembleOperations(null));
    }

    @Test
    public void isActive() {
        Assert.assertTrue(this.operations.isActive());
    }

    @Test
    public void setActive() {
        this.operations.setActive(false);
        Assert.assertTrue(this.operations.isActive());
    }

    @Test
    public void isEmpty() {
        Assert.assertTrue(this.operations.isEmpty());
    }
}
