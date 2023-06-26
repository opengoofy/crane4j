package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.exception.OperationExecuteException;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.MultiMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

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
 * @see AsyncBeanOperationExecutor
 * @see DisorderedBeanOperationExecutor
 * @see OrderedBeanOperationExecutor
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractBeanOperationExecutor implements BeanOperationExecutor {

    /**
     * Container manager.
     */
    private final ContainerManager containerManager;

    /**
     * <p>Wait time in milliseconds if the operation is not active.<br/>
     * In normal cases, this time is unnecessary set to a large value,
     * because the operation is usually active when the {@link #execute} method is called,
     * Unless there is a circular dependency between the operation and the resolution of another object during the parsing process.
     *
     * <p>NOTE: This is not a good solution, so in future versions,
     * we will try to solve this problem in the {@link cn.crane4j.core.parser.BeanOperationParser}.
     */
    @Setter
    public long waitTimeoutMillisecondIfOperationNotActive = 50L;

    /**
     * Whether to enable the execution of operations that are not active.
     */
    @Setter
    public boolean enableExecuteNotActiveOperation = false;

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations} and {@link Options}.
     *
     * @param targets targets
     * @param operations operations to be performed
     * @param options options for execution
     */
    @Override
    public void execute(Collection<?> targets, BeanOperations operations, Options options) {
        if (CollectionUtils.isEmpty(targets) || Objects.isNull(operations)) {
            return;
        }
        // When the following all conditions are met, the operation will be abandoned:
        // 1. the operation is not active;
        // 2. the operation is still not active after waiting for a period of time;
        // 3. the execution of non-active operations is not enabled.
        if (!operations.isActive() &&
            !waitForOperationActiveUntilTimeout(operations)
            && !enableExecuteNotActiveOperation) {
            log.warn("bean operation of [{}] is still not ready, abort execution of the operation", operations.getSource());
            return;
        }
        // complete the disassembly first if necessary
        MultiMap<BeanOperations, Object> collector = MultiMap.linkedListMultimap();
        collector.putAll(operations, targets);
        Predicate<? super KeyTriggerOperation> filter = options.getFilter();
        disassembleIfNecessary(targets, operations, filter, collector);

        // flattened objects are grouped according to assembly operations, then encapsulated as execution objects
        List<AssembleExecution> executions = new ArrayList<>();
        collector.asMap().forEach((op, ts) -> op.getAssembleOperations()
            .stream()
            .filter(filter)
            .map(p -> createAssembleExecution(op, p, ts, options))
            .forEach(executions::add)
        );

        // complete assembly operation
        executeOperations(executions, options);
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
        String namespace = operation.getContainer();
        Container<?> container = options.getContainer(containerManager, namespace);
        Asserts.isNotNull(container, "container of [{}] not found", namespace);
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

    private static <T> void disassembleIfNecessary(
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

    private static <T> void doDisassembleAndCollect(
        Collection<T> targets, DisassembleOperation disassembleOperation, Predicate<? super KeyTriggerOperation> filter, MultiMap<BeanOperations, Object> collector) {
        DisassembleOperationHandler handler = disassembleOperation.getDisassembleOperationHandler();
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
     * @param execute execute
     */
    protected static void tryExecute(Runnable execute) {
        try {
            execute.run();
        } catch(Exception e) {
            log.warn("execute operation fail: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
