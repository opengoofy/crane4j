package cn.crane4j.core.executor;

import cn.crane4j.annotation.OperationAware;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.lifecycle.SmartOperationAware;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.MultiMap;
import lombok.NonNull;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * An abstract implementation of {@link AbstractBeanOperationExecutor}
 * that supports the operation aware.
 *
 * @author huangchengxing
 * @see OperationAware
 * @see SmartOperationAware
 * @since 2.5.0
 */
public abstract class OperationAwareBeanOperationExecutor extends AbstractBeanOperationExecutor {

    protected OperationAwareBeanOperationExecutor(ContainerManager containerManager) {
        super(containerManager);
    }

    /**
     * Do something before the assembly operation begin.
     *
     * @param targetWithOperations target with operations
     */
    @Override
    protected void beforeAssembleOperation(MultiMap<BeanOperations, Object> targetWithOperations) {
        targetWithOperations.forEach((operations, target) -> {
            if (target instanceof OperationAware) {
                ((OperationAware)target).beforeAssembleOperation();
                if (target instanceof SmartOperationAware) {
                    ((SmartOperationAware)target).beforeAssembleOperation(operations);
                }
            }
        });
    }

    /**
     * Trigger the {@link OperationAware#afterOperationsCompletion} method of the target object.
     *
     * @param targetWithOperations target with operations
     * @since 2.5.0
     */
    @Override
    protected void afterOperationsCompletion(MultiMap<BeanOperations, Object> targetWithOperations) {
        targetWithOperations.forEach((operations, target) -> {
            if (target instanceof OperationAware) {
                ((OperationAware)target).afterOperationsCompletion();
                if (target instanceof SmartOperationAware) {
                    ((SmartOperationAware)target).afterOperationsCompletion(operations);
                }
            }
        });
    }

    /**
     * Filter the targets that support the specified operation
     * by the {@link SmartOperationAware#supportOperation} method.
     *
     * @param targets targets
     * @param operation operation
     * @return filtered targets
     * @since 2.5.0
     */
    @Override
    @NonNull
    protected <T> Collection<T> filterTargetsForSupportedOperation(
        Collection<T> targets, KeyTriggerOperation operation) {
        return targets.stream()
            .filter(t -> !(t instanceof SmartOperationAware)
                || ((SmartOperationAware)t).supportOperation(operation))
            .collect(Collectors.toList());
    }
}
