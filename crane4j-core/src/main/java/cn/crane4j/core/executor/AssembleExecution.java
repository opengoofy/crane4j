package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

/**
 * <p>Indicates that an assembly operation is executed at one time,
 * including all objects that need to perform this operation,
 * and the lifecycle used for execution.
 *
 * <p>This object is usually one-time.
 * It is created at one execution of {@link BeanOperationExecutor}
 * and destroyed after that execution.
 *
 * @author huangchengxing
 * @see BeanOperationExecutor
 * @see AssembleOperationHandler
 */
public interface AssembleExecution {

    /**
     * Create an comparator of {@link AssembleExecution}.
     *
     * @param beanOperations bean operations
     * @param operation      operation
     * @param container      container
     * @param targets        targets
     * @return execution comparator
     */
    static AssembleExecution create(
            BeanOperations beanOperations, AssembleOperation operation, Container<?> container, Collection<Object> targets) {
        return new SimpleAssembleExecution(beanOperations, operation, container, targets);
    }

    /**
     * Get the operation configuration corresponding to the operation object.
     *
     * @return bean operations
     */
    BeanOperations getBeanOperations();

    /**
     * Get the type of the object to be operated on.
     *
     * @return type
     */
    default AnnotatedElement getSource() {
        return getBeanOperations().getSource();
    }

    /**
     * Get the assembly operation to be performed.
     *
     * @return operations to be performed.
     */
    AssembleOperation getOperation();

    /**
     * Get the data source container of the assembly operation.
     *
     * @return container
     */
    Container<?> getContainer();

    /**
     * Gets the handler used to perform the assembly operation.
     *
     * @return handler
     */
    default AssembleOperationHandler getHandler() {
        return getOperation().getAssembleOperationHandler();
    }

    /**
     * get the target object to be processed.
     *
     * @return target
     */
    Collection<Object> getTargets();

    /**
     * Simple implementation of {@link AssembleExecution}.
     *
     * @author huangchengxing
     */
    @Getter
    @RequiredArgsConstructor
    class SimpleAssembleExecution implements AssembleExecution {
        private final BeanOperations beanOperations;
        private final AssembleOperation operation;
        private final Container<?> container;
        private final Collection<Object> targets;
    }
}
