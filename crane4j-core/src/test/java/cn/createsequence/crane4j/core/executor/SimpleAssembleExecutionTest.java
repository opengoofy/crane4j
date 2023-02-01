package cn.createsequence.crane4j.core.executor;

import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.parser.AssembleOperation;
import cn.createsequence.crane4j.core.parser.BeanOperations;
import cn.createsequence.crane4j.core.parser.SimpleAssembleOperation;
import cn.createsequence.crane4j.core.parser.SimpleBeanOperations;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link SimpleAssembleExecution}
 *
 * @author huangchengxing
 */
public class SimpleAssembleExecutionTest {

    private static final AssembleOperationHandler ASSEMBLE_OPERATION_HANDLER = (c, ts) -> {};
    private static final BeanOperations BEAN_OPERATIONS = new SimpleBeanOperations(Void.TYPE);
    private static final AssembleOperation ASSEMBLE_OPERATION = new SimpleAssembleOperation(
        "key", Collections.emptySet(), Container.empty(), ASSEMBLE_OPERATION_HANDLER
    );
    private static final AssembleExecution EXECUTION = new SimpleAssembleExecution(
        BEAN_OPERATIONS, ASSEMBLE_OPERATION, Collections.emptyList()
    );

    @Test
    public void getBeanOperations() {
        Assert.assertSame(BEAN_OPERATIONS, EXECUTION.getBeanOperations());
    }

    @Test
    public void getTargetType() {
        Assert.assertEquals(BEAN_OPERATIONS.getTargetType(), EXECUTION.getTargetType());
    }

    @Test
    public void getOperation() {
        Assert.assertSame(ASSEMBLE_OPERATION, EXECUTION.getOperation());
    }

    @Test
    public void getContainer() {
        Assert.assertEquals(Container.empty(), EXECUTION.getContainer());
    }

    @Test
    public void getHandler() {
        Assert.assertSame(ASSEMBLE_OPERATION_HANDLER, EXECUTION.getHandler());
    }

    @Test
    public void getTargets() {
        Assert.assertEquals(Collections.emptyList(), EXECUTION.getTargets());
    }
}
