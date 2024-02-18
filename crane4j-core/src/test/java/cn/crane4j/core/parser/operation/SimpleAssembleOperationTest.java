package cn.crane4j.core.parser.operation;

import cn.crane4j.core.container.Container;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link SimpleAssembleOperation}
 *
 * @author huangchengxing
 */
public class SimpleAssembleOperationTest {

    private SimpleAssembleOperation operation;

    @Before
    public void initOperation() {
        operation = SimpleAssembleOperation.builder()
            .key("key")
            .propertyMappings(Collections.emptySet())
            .container(Container.EMPTY_CONTAINER_NAMESPACE)
            .sort(Integer.MAX_VALUE)
            .build();
    }

    @Test
    public void testKeyType() {
        Assert.assertNull(operation.getKeyType());
        operation.setKeyType(Object.class);
        Assert.assertEquals(Object.class, operation.getKeyType());
    }

    @Test
    public void getPropertyMappings() {
        Assert.assertEquals(Collections.emptySet(), operation.getPropertyMappings());
    }

    @Test
    public void getContainer() {
        Assert.assertEquals(Container.EMPTY_CONTAINER_NAMESPACE, operation.getContainer());
    }

    @Test
    public void getAssembleOperationHandler() {
        Assert.assertNull(operation.getAssembleOperationHandler());
    }
}
