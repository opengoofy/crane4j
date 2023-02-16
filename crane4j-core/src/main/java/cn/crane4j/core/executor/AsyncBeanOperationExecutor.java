package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * <p>{@link BeanOperationExecutor}的异步实现，
 * 当提交任务时，将以数据源容器分组异步的支持操作。<br />
 * 无法保证操作按顺序执行，仅可以保证按操作顺序向线程池提交任务。
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncBeanOperationExecutor extends AbstractBeanOperationExecutor {

    /**
     * 用于执行操作的线程池
     */
    private final ExecutorService executorService;

    /**
     * 完成装配操作
     *
     * @param executions 待完成的装配操作
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
