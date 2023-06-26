package cn.crane4j.core.executor;

import cn.crane4j.core.executor.handler.AssembleOperationHandler;

import java.util.Collection;

/**
 * @author huangchengxing
 * @see AssembleOperationHandler
 * @see AssembleExecution
 */
public interface AssembleExecutionLifecycleProcessor {

    default Collection<AssembleExecution> beforeHandler(Collection<AssembleExecution> executions) {
        return executions;
    }

    default void beforePropertyMapping(Object target, Object source) {
        // do nothing
    }

    default void afterHandler(Collection<AssembleExecution> executions) {
        // do nothing
    }
}
