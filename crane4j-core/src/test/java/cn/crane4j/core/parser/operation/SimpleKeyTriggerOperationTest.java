package cn.crane4j.core.parser.operation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link SimpleKeyTriggerOperation}
 *
 * @author huangchengxing
 */
public class SimpleKeyTriggerOperationTest {

    private SimpleKeyTriggerOperation operation;

    @Before
    public void initOperation() {
        operation = new SimpleKeyTriggerOperation("key", Integer.MIN_VALUE);
        operation.putGroup("one");
    }

    @Test
    public void getKey() {
        Assert.assertEquals("key", operation.getKey());
    }

    @Test
    public void getSort() {
        Assert.assertEquals(Integer.MIN_VALUE, operation.getSort());
    }

    @Test
    public void isBelong() {
        Assert.assertTrue(operation.isBelong("one"));
        Assert.assertFalse(operation.isBelong("two"));
    }

    @Test
    public void getGroups() {
        Assert.assertEquals(Collections.singleton("one"), operation.getGroups());
    }

    @Test
    public void putGroup() {
        Assert.assertEquals(Collections.singleton("one"), operation.getGroups());
        operation.putGroup("two");
        Assert.assertTrue(operation.getGroups().contains("two"));
    }

    @Test
    public void removeGroup() {
        Assert.assertEquals(Collections.singleton("one"), operation.getGroups());
        operation.removeGroup("one");
        Assert.assertFalse(operation.getGroups().contains("one"));
    }
}
