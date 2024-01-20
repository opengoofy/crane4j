package cn.crane4j.annotation;

/**
 * <p>An interface that make the target object aware of the operation.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.executor.OperationAwareBeanOperationExecutor
 * @see cn.crane4j.core.container.lifecycle.SmartOperationAware
 * @since 2.5.0
 */
public interface OperationAware {

    /**
     * Do something before the assembly operations begin.
     */
    default void beforeAssembleOperation() {
        // do nothing
    }

    /**
     * Do something after all operations completed.
     */
    default void afterOperationsCompletion() {
        // do nothing
    }
}
