package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * <p>The asynchronous implementation of {@link BeanOperationExecutor}
 * It will group the operations to be executed according to the data source container,
 * then submit them to the executor in turn, and finally complete them asynchronously.
 *
 * <p>It is not possible to ensure that the operations are executed in order,
 * but only to submit tasks to the thread pool in the order of operations.
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncBeanOperationExecutor extends AbstractBeanOperationExecutor {

    /**
     * thread pool used to perform operations.
     */
    private final ExecutorService executorService;

    /**
     * Complete assembly operation.
     *
     * @param executions executions
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void executeOperations(List<AssembleExecution> executions) {
        CompletableFuture<Void>[] tasks = executions.stream()
            .map(execution -> (Runnable)() -> doExecuteOperations(execution))
            .map(task -> CompletableFuture.runAsync(task, executorService))
            .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(tasks).join();
    }

    private void doExecuteOperations(AssembleExecution execution) {
        Container<?> container = execution.getContainer();
        tryExecute(() -> execution.getHandler().process(container, Collections.singleton(execution)));
    }
}
