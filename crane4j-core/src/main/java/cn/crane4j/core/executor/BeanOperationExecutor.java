package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * <p>Bean operation executor, used to perform disassembly operations,
 * encapsulate the assembly operations to be performed and target objects into {@link AssembleExecution},
 * and then distribute them to {@link AssembleOperationHandler} for execution.
 *
 * @author huangchengxing
 * @see AbstractBeanOperationExecutor
 * @see AsyncBeanOperationExecutor
 * @see DisorderedBeanOperationExecutor
 * @see OrderedBeanOperationExecutor
 * @see AssembleExecution
 */
public interface BeanOperationExecutor {

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations} and {@link Options}.
     *
     * @param targets targets
     * @param operations operations to be performed
     * @param options options for execution
     */
    void execute(Collection<?> targets, BeanOperations operations, Options options);

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations}
     *
     * @param targets targets
     * @param operations operations to be performed
     * @param filter operation filter, which can filter some operations based on operation key, group and other attributes
     */
    default void execute(Collection<?> targets, BeanOperations operations, Predicate<? super KeyTriggerOperation> filter) {
        execute(targets, operations, () -> filter);
    }

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations}
     *
     * @param targets targets
     * @param operations operations to be performed
     */
    default void execute(Collection<?> targets, BeanOperations operations) {
        execute(targets, operations, t -> true);
    }

    /**
     * Options for execution.
     *
     * @author huangchengxing
     */
    interface Options {

        /**
         * Get the container manager.
         *
         * @return container manager
         */
        Predicate<? super KeyTriggerOperation> getFilter();

        /**
         * Get container.
         *
         * @param containerManager container manager
         * @param namespace namespace of container
         * @return container instance
         */
        default Container<?> getContainer(ContainerManager containerManager, String namespace) {
            return containerManager.getContainer(namespace);
        }

        @RequiredArgsConstructor
        @Getter
        public class DynamicContainerOption implements Options {
            private final Predicate<? super KeyTriggerOperation> filter;
            private final Map<String, Container<Object>> dynamicContainers;
            @Override
            public Container<?> getContainer(ContainerManager containerManager, String namespace) {
                return dynamicContainers.getOrDefault(namespace, containerManager.getContainer(namespace));
            }
        }
    }
}
