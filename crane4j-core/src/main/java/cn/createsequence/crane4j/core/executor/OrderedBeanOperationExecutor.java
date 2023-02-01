package cn.createsequence.crane4j.core.executor;

import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.parser.AssembleOperation;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <p>{@link BeanOperationExecutor}的同步实现。<br />
 * 执行时，会保证{@link AssembleOperation}的执行顺序，
 * 但是无法保证一次执行中{@link Container}只被调用最少次。
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class OrderedBeanOperationExecutor extends AbstractBeanOperationExecutor {

    /**
     * 比较器
     */
    private final Comparator<AssembleOperation> comparator;

    /**
     * 完成装配操作
     *
     * @param executions 待完成的装配操作
     */
    @Override
    protected void executeOperations(List<AssembleExecution> executions) {
        executions.stream()
            .sorted(Comparator.comparing(AssembleExecution::getOperation, comparator))
            .forEach(e -> tryExecute(() -> e.getHandler().process(e.getContainer(), Collections.singletonList(e))));
    }
}
