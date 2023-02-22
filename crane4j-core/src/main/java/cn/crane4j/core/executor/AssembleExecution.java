package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperations;

import java.util.Collection;

/**
 * <p>Indicates that an assembly operation is executed at one time,
 * including all objects that need to perform this operation,
 * and the processor used for execution.
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
    default Class<?> getTargetType() {
        return getBeanOperations().getTargetType();
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
    default Container<?> getContainer() {
        return getOperation().getContainer();
    }

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
}
