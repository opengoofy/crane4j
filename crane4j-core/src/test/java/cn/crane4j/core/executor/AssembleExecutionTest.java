package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * test for {@link AssembleExecution.SimpleAssembleExecution}
 *
 * @author huangchengxing
 */
public class AssembleExecutionTest {

    @Test
    public void test() {
        List<Object> targets = Arrays.asList(new Object(), new Object());
        AssembleOperationHandler handler = new OneToOneAssembleOperationHandler(new ReflectivePropertyOperator());
        AssembleOperation assembleOperation = new SimpleAssembleOperation(
            "key", Collections.singleton(new SimplePropertyMapping("src", "ref")),
            "container", handler
        );
        AssembleExecution execution = AssembleExecution.create(
            BeanOperations.empty(), assembleOperation, Container.empty(), targets
        );

        Assert.assertEquals(BeanOperations.empty(), execution.getBeanOperations());
        Assert.assertEquals(BeanOperations.empty().getSource(), execution.getSource());
        Assert.assertEquals(assembleOperation, execution.getOperation());
        Assert.assertEquals(Container.empty(), execution.getContainer());
        Assert.assertEquals(handler, execution.getHandler());
        Assert.assertEquals(targets, execution.getTargets());
    }
}
