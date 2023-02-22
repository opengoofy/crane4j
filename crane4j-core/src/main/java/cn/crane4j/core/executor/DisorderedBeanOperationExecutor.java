package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Synchronization implementation of {@link BeanOperationExecutor}.<br />
 * During execution, the number of calls to {@link Container} will be reduced as much as possible,
 * but the order of operation execution cannot be guaranteed.
 *
 * @author huangchengxing
 */
@Slf4j
public class DisorderedBeanOperationExecutor extends AbstractBeanOperationExecutor {

    /**
     * Complete assembly operation.
     *
     * @param executions executions
     */
    @Override
    protected void executeOperations(List<AssembleExecution> executions) {
        executions.stream()
            .collect(Collectors.groupingBy(AssembleExecution::getContainer))
            .forEach(this::doExecuteOperations);
    }

    private void doExecuteOperations(Container<?> container, List<AssembleExecution> executions) {
        executions.stream()
            .collect(Collectors.groupingBy(AssembleExecution::getHandler))
            .forEach((handler, es) -> tryExecute(() -> handler.process(container, es)));
    }
}
