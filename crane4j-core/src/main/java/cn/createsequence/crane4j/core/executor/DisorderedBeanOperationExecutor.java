package cn.createsequence.crane4j.core.executor;

import cn.createsequence.crane4j.core.container.Container;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>{@link BeanOperationExecutor}的同步实现。<br />
 * 执行时，会尽可能的保证减少{@link Container}的调用次数，
 * 但是无法保证操作执行顺序。
 *
 * @author huangchengxing
 */
@Slf4j
public class DisorderedBeanOperationExecutor extends AbstractBeanOperationExecutor {

    /**
     * 完成装配操作
     *
     * @param executions 待完成的装配操作
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
