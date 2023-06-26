package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.exception.OperationExecuteException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * <p>The asynchronous implementation of {@link BeanOperationExecutor}.<br />
 * It will group the operations to be executed according to the data source container,
 * then submit them to the executor in turn, and finally complete them asynchronously.
 *
 * <p>It is not possible to ensure that the operations are executed in order,
 * but only to submit tasks to the thread pool in the order of operations.
 *
 * @author huangchengxing
 */
@Slf4j
public class AsyncBeanOperationExecutor extends AbstractBeanOperationExecutor {

    /**
     * thread pool used to perform operations.
     */
    private final ExecutorService executorService;

    /**
     * Create an comparator of {@link AsyncBeanOperationExecutor}.
     *
     * @param containerManager container manager
     * @param executorService thread pool used to perform operations
     */
    public AsyncBeanOperationExecutor(
            ContainerManager containerManager, ExecutorService executorService) {
        super(containerManager);
        this.executorService = executorService;
    }

    /**
     * <p>Complete the assembly operation.<br />
     * All operations of input parameters ensure their orderliness in the same class.
     * For example, if there are ordered operations <i>a<i> and <i>b<i> in {@code A.class},
     * the order of <i>a<i> and <i>b<i> is still guaranteed when
     * the corresponding {@link AssembleExecution} is obtained.
     *
     * @param executions assembly operations to be completed
     * @param options options for execution
     * @throws OperationExecuteException thrown when operation execution exception
     * @implNote
     * <ul>
     *     <li>If necessary, you need to ensure the execution order of {@link AssembleExecution};</li>
     *     <li>
     *         If the network request and other time-consuming operations are required to obtain the data source,
     *         the number of requests for the data source should be reduced as much as possible;
     *     </li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void executeOperations(List<AssembleExecution> executions, Options options) throws OperationExecuteException {
        CompletableFuture<Void>[] tasks = executions.stream()
            .map(execution -> (Runnable)() -> doExecuteOperations(execution))
            .map(task -> CompletableFuture.runAsync(task, executorService))
            .toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(tasks).join();
        } catch (Exception e) {
            throw new OperationExecuteException(e);
        }
    }

    private void doExecuteOperations(AssembleExecution execution) {
        Container<?> container = execution.getContainer();
        tryExecute(() -> execution.getHandler().process(container, Collections.singleton(execution)));
    }
}
