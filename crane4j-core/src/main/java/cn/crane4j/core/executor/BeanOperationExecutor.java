package cn.crane4j.core.executor;

import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.KeyTriggerOperation;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * <p>Bean operation executor, used to perform disassembly operations,
 * encapsulate the assembly operations to be performed and target objects into {@link AssembleExecution},
 * and then distribute them to {@link AssembleOperationHandler} for execution.
 *
 * @author huangchengxing
 * @see AbstractBeanOperationExecutor
 * @see AsyncBeanOperationExecutor
 * @see DisorderedBeanOperationExecutor
 * @see OrderedBeanOperationExecutor
 * @see AssembleExecution
 */
public interface BeanOperationExecutor {

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations}
     *
     * @param targets targets
     * @param operations operations to be performed
     * @param filter operation filter, which can filter some operations based on operation key, group and other attributes
     */
    void execute(Collection<?> targets, BeanOperations operations, Predicate<? super KeyTriggerOperation> filter);

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations}
     *
     * @param targets targets
     * @param operations operations to be performed
     */
    default void execute(Collection<?> targets, BeanOperations operations) {
        execute(targets, operations, t -> true);
    }
}
