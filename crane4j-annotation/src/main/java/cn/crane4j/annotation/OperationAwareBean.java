package cn.crane4j.annotation;

/**
 * <p>An interface that make the target object aware of the operation.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.executor.OperationAwareBeanOperationExecutor
 * @see cn.crane4j.core.container.lifecycle.SmartOperationAwareBean
 * @since 2.5.0
 */
public interface OperationAwareBean {

    /**
     * Whether the target object supports the specified operation.
     *
     * @param key key property name of the operation
     * @return true if supported, false otherwise
     */
    default boolean supportOperation(String key) {
        return true;
    }

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
