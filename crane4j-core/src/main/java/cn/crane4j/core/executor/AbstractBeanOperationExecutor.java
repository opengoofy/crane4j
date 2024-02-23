package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.exception.OperationExecuteException;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ExecutionTimeLogable;
import cn.crane4j.core.util.MultiMap;
import cn.crane4j.core.util.Timer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * <p>This class serves as a template class and provides a basic skeleton implementation
 * for most of the {@link BeanOperationExecutor},
 * particularly shielding the complexity of parsing {@link DisassembleOperation} operations.
 * Once the {@link #executeOperations} method is implemented in a subclass,
 * it can be used as a {@link BeanOperationExecutor}.
 *
 * <p>According to the instructions specified in {@link BeanOperations},
 * the following steps are performed when executing operations through the {@link BeanOperationExecutor#execute} method:
 * <ul>
 *     <li>
 *         If there are any {@link DisassembleOperation} operations to be executed,
 *         recursively extract and flatten the objects that need to be processed from
 *         the {@code target} object (if it is a collection or an array, iterate over each element);
 *     </li>
 *     <li>
 *         Group all the objects to be processed based on their corresponding {@link BeanOperations},
 *         and wrap each group into an {@link AssembleExecution} object.
 *     </li>
 *     <li>
 *         Invoke the {@link #executeOperations} method implemented in the subclass to
 *         actually perform the operations within each {@link AssembleExecution}.
 *     </li>
 * </ul>
 * 
 * <p>This class only guarantees the sequential execution of {@link DisassembleOperation} operations,
 * while the sequential execution of {@link AssembleOperation} operations depends on
 * the implementation logic of {@link #executeOperations}.<br />
 * For performance reasons, when implementing the {@link #executeOperations} method,
 * it is recommended to minimize the number of accesses to the {@link Container}.
 *
 * @author huangchengxing
 * @see OperationAwareBeanOperationExecutor
 * @see AsyncBeanOperationExecutor
 * @see DisorderedBeanOperationExecutor
 * @see OrderedBeanOperationExecutor
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractBeanOperationExecutor implements BeanOperationExecutor, ExecutionTimeLogable {

    /**
     * Container manager.
     */
    private final ContainerManager containerManager;

    /**
     * <p>Wait time in milliseconds if the operation is not active.<br/>
     * In normal cases, this time is unnecessary set to a large value.
     * The operation is usually active when the {@link #execute} method is called,
     * Unless there is a circular dependency between the operation and the resolution of another object during the parsing process.
     *
     * <p>NOTE: This is not a good solution, so in future versions,
     * we will try to solve this problem in the {@link cn.crane4j.core.parser.BeanOperationParser}.
     */
    @Setter
    private long waitTimeoutMillisecondIfOperationNotActive = 50L;

    /**
     * Whether to enable the execution of operations that are not active.
     */
    @Setter
    private boolean enableExecuteNotActiveOperation = false;

    /**
     * Whether to log the execution time of the operation.
     */
    @Setter
    private boolean logExecutionTime = false;

    /**
     * <p>process target num of each batch when executing an operation.<br />
     * for example, if we have 1000 targets and batch size is 100,
     * and each target has 3 operations, so we will get 3000 executions.<br />
     * it's maybe useful when using asynchronous executor to process large number of targets.
     *
     * @since 2.5.0
     */
    @Setter
    private int batchSize = -1;

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations} and {@link Options}.
     *
     * @param targets targets
     * @param operations operations to be performed
     * @param options options for execution
     * @see #beforeDisassembleOperation
     * @see #beforeAssembleOperation
     * @see #afterOperationsCompletion
     */
    @Override
    public void execute(Collection<?> targets, BeanOperations operations, Options options) {
        if (CollectionUtils.isEmpty(targets) || Objects.isNull(operations)) {
            return;
        }

        // When the following all conditions are met, the operation will be abandoned:
        // 1. The operation is not active;
        // 2. The operation is still not active after waiting for a period of time;
        // 3. The execution of non-active operations is not enabled.
        if (!operations.isActive() &&
            !waitForOperationActiveUntilTimeout(operations)
            && !enableExecuteNotActiveOperation) {
            log.warn("bean operation of [{}] is still not ready, abort execution of the operation", operations.getSource());
            return;
        }

        // complete the disassembly first if necessary
        beforeDisassembleOperation(targets, operations, options);
        MultiMap<BeanOperations, Object> targetWithOperations = MultiMap.linkedListMultimap();
        targetWithOperations.putAll(operations, targets);
        Predicate<? super KeyTriggerOperation> filter = options.getFilter();

        Timer timer = Timer.startTimer(logExecutionTime);
        disassembleIfNecessary(targets, operations, filter, targetWithOperations);
        timer.stop(TimeUnit.MILLISECONDS, time -> log.debug("disassemble operations completed in {} ms", time));

        // flattened objects are grouped according to assembly operations, then encapsulated as execution objects
        beforeAssembleOperation(targetWithOperations);
        List<AssembleExecution> executions = new ArrayList<>();
        targetWithOperations.asMap().forEach((op, ts) -> {
            List<AssembleExecution> executionsOfOp = combineExecutions(options, filter, op, ts);
            if (CollectionUtils.isNotEmpty(executionsOfOp)) {
                executions.addAll(executionsOfOp);
            }
        });

        // complete assembly operation
        timer.start();
        executeOperations(executions, options);
        timer.stop(TimeUnit.MILLISECONDS, time -> log.debug("execute operations completed in {} ms", time));
        afterOperationsCompletion(targetWithOperations);
    }

    @NonNull
    private List<AssembleExecution> combineExecutions(
        Options options, Predicate<? super KeyTriggerOperation> filter, BeanOperations beanOperations, Collection<Object> targets) {
        List<Collection<Object>> batches = batchSize > 1 ?
            CollectionUtils.split(targets, batchSize) : Collections.singletonList(targets);
        return batches.stream()
            .map(batch -> doCombineExecutions(options, filter, beanOperations, batch))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Combine the {@link AssembleExecution} objects according to the specified {@link BeanOperations} and {@link Options}.
     *
     * @param options options for execution
     * @param filter filter
     * @param beanOperations bean operations
     * @param targets targets
     * @return {@link AssembleExecution} objects
     */
    @NonNull
    protected List<AssembleExecution> doCombineExecutions(
        Options options, Predicate<? super KeyTriggerOperation> filter, BeanOperations beanOperations, Collection<Object> targets) {
        return beanOperations.getAssembleOperations()
            .stream()
            .filter(filter)
            .map(p -> createAssembleExecution(beanOperations, p, targets, options))
            .collect(Collectors.toList());
    }

    /**
     * Create a {@link AssembleExecution}.
     *
     * @param beanOperations bean operations
     * @param operation operation
     * @param targets targets
     * @param options options for execution
     * @return {@link AssembleExecution}
     */
    protected AssembleExecution createAssembleExecution(
        BeanOperations beanOperations, AssembleOperation operation, Collection<Object> targets, Options options) {
        targets = filterTargetsForSupportedOperation(targets, operation);
        String namespace = operation.getContainer();
        Container<?> container = options.getContainer(containerManager, namespace);
        Asserts.isNotNull(container, "container [{}] not found", namespace);
        return AssembleExecution.create(beanOperations, operation, container, targets);
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
    protected abstract void executeOperations(List<AssembleExecution> executions, Options options) throws OperationExecuteException;

    /**
     * Do something before the assembly operation begin.
     *
     * @param targetWithOperations target with operations
     */
    protected void beforeAssembleOperation(MultiMap<BeanOperations, Object> targetWithOperations) {
        // do nothing
    }

    /**
     * Do something before the disassemble operations begin.
     *
     * @param targets targets
     * @param operations operations
     * @param options options for execution
     * @since 2.5.0
     */
    protected void beforeDisassembleOperation(
        Collection<?> targets, BeanOperations operations, Options options) {
        // do nothing
    }

    /**
     * Do something after all operations completed.
     *
     * @param targetWithOperations target with operations
     * @since 2.5.0
     */
    protected void afterOperationsCompletion(MultiMap<BeanOperations, Object> targetWithOperations) {
        // do nothing
    }

    /**
     * Filter the targets that do not support the operation.
     *
     * @param targets targets
     * @param operation operation
     * @return filtered targets
     * @since 2.5.0
     */
    @NonNull
    protected <T> Collection<T> filterTargetsForSupportedOperation(
        Collection<T> targets, KeyTriggerOperation operation) {
        return targets;
    }

    private <T> void disassembleIfNecessary(
        Collection<T> targets, BeanOperations operations,
        Predicate<? super KeyTriggerOperation> filter, MultiMap<BeanOperations, Object> collector) {
        Collection<DisassembleOperation> internalOperations = operations.getDisassembleOperations();
        if (CollectionUtils.isEmpty(internalOperations)) {
            return;
        }
        internalOperations.stream()
            .filter(filter)
            .forEach(internal -> doDisassembleAndCollect(targets, internal, filter, collector));
    }

    private <T> void doDisassembleAndCollect(
        Collection<T> targets, DisassembleOperation disassembleOperation, Predicate<? super KeyTriggerOperation> filter, MultiMap<BeanOperations, Object> collector) {
        DisassembleOperationHandler handler = disassembleOperation.getDisassembleOperationHandler();
        targets = filterTargetsForSupportedOperation(targets, disassembleOperation);
        Collection<?> internalTargets = handler.process(disassembleOperation, targets);
        if (CollectionUtils.isEmpty(internalTargets)) {
            return;
        }
        BeanOperations internalOperations = disassembleOperation.getInternalBeanOperations(internalTargets);
        collector.putAll(internalOperations, internalTargets);
        // recurse process if still have nested objects
        disassembleIfNecessary(internalTargets, internalOperations, filter, collector);
    }

    private boolean waitForOperationActiveUntilTimeout(BeanOperations operations) {
        // rotate training and wait within the specified timeout period
        long start = System.currentTimeMillis();
        while (!operations.isActive()) {
            if (System.currentTimeMillis() - start > waitTimeoutMillisecondIfOperationNotActive) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Try to execute the operation.<br />
     * If necessary, output the log when throwing an exception.
     *
     * @param handler handler
     * @param executions executions
     * @param container container
     */
    protected static void doExecute(
        AssembleOperationHandler handler, Container<?> container, Collection<AssembleExecution> executions) {
        try {
            handler.process(container, executions);
        } catch(Exception ex) {
            log.warn("execute operation fail: {}", ex.getMessage(), ex);
        }
    }
}
