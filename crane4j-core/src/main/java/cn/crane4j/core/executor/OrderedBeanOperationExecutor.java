package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.OperationExecuteException;
import cn.crane4j.core.parser.AssembleOperation;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <p>Synchronization implementation of {@link BeanOperationExecutor}.<br />
 * During execution, the execution order of {@link AssembleOperation} will be guaranteed,
 * but it cannot be guaranteed that {@link Container} will only be called at least once.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class OrderedBeanOperationExecutor extends AbstractBeanOperationExecutor {

    /**
     * comparator
     */
    private final Comparator<AssembleOperation> comparator;

    /**
     * Complete assembly operation.
     *
     * @param executions executions
     */
    @Override
    protected void executeOperations(List<AssembleExecution> executions) throws OperationExecuteException {
        try {
            executions.stream()
                .sorted(Comparator.comparing(AssembleExecution::getOperation, comparator))
                .forEach(e -> tryExecute(() -> e.getHandler().process(e.getContainer(), Collections.singletonList(e))));
        } catch (Exception e) {
            throw new OperationExecuteException(e);
        }
    }
}
