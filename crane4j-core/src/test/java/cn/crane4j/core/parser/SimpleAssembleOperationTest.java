package cn.crane4j.core.parser;

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
        operation = new SimpleAssembleOperation(
            "key", Integer.MAX_VALUE,
            Collections.emptySet(), Container.empty(), null
        );
    }

    @Test
    public void getPropertyMappings() {
        Assert.assertEquals(Collections.emptySet(), operation.getPropertyMappings());
    }

    @Test
    public void getContainer() {
        Assert.assertEquals(Container.empty(), operation.getContainer());
    }

    @Test
    public void getAssembleOperationHandler() {
        Assert.assertNull(operation.getAssembleOperationHandler());
    }
}
