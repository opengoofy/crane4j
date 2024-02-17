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

    private static final Object SOURCE = new Object();
    private SimpleKeyTriggerOperation operation;

    @Before
    public void initOperation() {
        operation = SimpleKeyTriggerOperation.builder()
            .key("key")
            .sort(Integer.MIN_VALUE)
            .group("one")
            .source(SOURCE)
            .build();
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
    public void getSource() {
        Assert.assertSame(SOURCE, operation.getSource());
    }
}
