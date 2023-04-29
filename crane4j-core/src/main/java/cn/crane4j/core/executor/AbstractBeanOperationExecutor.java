package cn.crane4j.core.executor;

import cn.crane4j.core.exception.OperationExecuteException;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.DisassembleOperation;
import cn.crane4j.core.parser.KeyTriggerOperation;
import cn.crane4j.core.util.CollectionUtils;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Basic implementation of {@link BeanOperationExecutor}.
 *
 * @author huangchengxing
 * @see AsyncBeanOperationExecutor
 * @see DisorderedBeanOperationExecutor
 * @see OrderedBeanOperationExecutor
 */
@Slf4j
public abstract class AbstractBeanOperationExecutor implements BeanOperationExecutor {

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations}
     *
     * @param targets targets
     * @param operations operations to be performed
     * @param filter operation filter, which can filter some operations based on operation key, group and other attributes
     */
    @Override
    public void execute(Collection<?> targets, BeanOperations operations, Predicate<? super KeyTriggerOperation> filter) {
        if (CollectionUtils.isEmpty(targets) || Objects.isNull(operations)) {
            return;
        }
        if (!operations.isActive()) {
            log.warn("bean operation of [{}] is still not ready, please try again", operations.getSource());
            return;
        }
        // complete the disassembly first if necessary
        Multimap<BeanOperations, Object> collector = LinkedListMultimap.create();
        collector.putAll(operations, targets);
        disassembleIfNecessary(targets, operations, filter, collector);

        // flattened objects are grouped according to assembly operations, then encapsulated as execution objects
        List<AssembleExecution> executions = new ArrayList<>();
        collector.asMap().forEach((op, ts) -> op.getAssembleOperations()
            .stream()
            .filter(filter)
            .map(p -> createAssembleExecution(op, p, ts))
            .forEach(executions::add)
        );

        // complete assembly operation
        executeOperations(executions);
    }

    /**
     * Create a {@link AssembleExecution}.
     *
     * @param beanOperations bean operations
     * @param operation operation
     * @param targets targets
     * @return {@link AssembleExecution}
     */
    protected AssembleExecution createAssembleExecution(
        BeanOperations beanOperations, AssembleOperation operation, Collection<Object> targets) {
        return new SimpleAssembleExecution(beanOperations, operation, targets);
    }
    
    /**
     * <p>Complete the assembly operation.<br />
     * All operations of input parameters ensure their orderliness in the same class.
     * For example, if there are ordered operations <i>a<i> and <i>b<i> in {@code A.class},
     * the order of <i>a<i> and <i>b<i> is still guaranteed when
     * the corresponding {@link AssembleExecution} is obtained.
     *
     * @param executions assembly operations to be completed
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
    protected abstract void executeOperations(List<AssembleExecution> executions) throws OperationExecuteException;

    private static <T> void disassembleIfNecessary(
        Collection<T> targets, BeanOperations operations,
        Predicate<? super KeyTriggerOperation> filter, Multimap<BeanOperations, Object> collector) {
        Collection<DisassembleOperation> internalOperations = operations.getDisassembleOperations();
        if (CollectionUtils.isEmpty(internalOperations)) {
            return;
        }
        internalOperations.stream()
            .filter(filter)
            .forEach(internal -> doDisassembleAndCollect(targets, internal, filter, collector));
    }

    private static <T> void doDisassembleAndCollect(
        Collection<T> targets, DisassembleOperation disassembleOperation, Predicate<? super KeyTriggerOperation> filter, Multimap<BeanOperations, Object> collector) {
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
