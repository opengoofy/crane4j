package cn.crane4j.core.container.lifecycle;

import cn.crane4j.annotation.OperationAwareBean;
import cn.crane4j.core.executor.OperationAwareBeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;

/**
 * <p>An interface that make the target object aware of the operation.<br />
 * It's enhanced on the basis of {@link OperationAwareBean} and supports awareness more arguments.
 *
 * @author huangchengxing
 * @see OperationAwareBeanOperationExecutor
 * @since 2.5.0
 */
public interface SmartOperationAwareBean extends OperationAwareBean {

    /**
     * Whether the target object supports the specified operation.
     *
     * @param operation operation
     * @return true if supported, false otherwise
     */
    default boolean supportOperation(KeyTriggerOperation operation) {
        return true;
    }

    /**
     * Do something before the assembly operations begin.
     *
     * @param operations operations
     */
    default void beforeAssembleOperation(BeanOperations operations) {
        // do nothing
    }

    /**
     * Do something after all operations completed.
     *
     * @param operations operations
     */
    default void afterOperationsCompletion(BeanOperations operations) {
        // do nothing
    }
}
