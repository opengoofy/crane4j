package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.support.Sorted;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Processor for container lifecycle.
 *
 * @author huangchengxing
 * @see ContainerDefinition
 * @see ContainerManager
 */
public interface ContainerLifecycleProcessor extends Sorted {

    /**
     * <p>Callback methods when the container is registered with the {@link ContainerManager}.<br/>
     * At this stage, you can modify the container's definition information,
     * including but not limited to modifying its loading strategy or replacing its factory method.
     *
     * @param old old container comparator or container definition
     * @param newDefinition new definition of container
     * @return final effective container definition
     * @see ContainerManager#registerContainer
     */
    default ContainerDefinition whenRegistered(
            @Nullable Object old, ContainerDefinition newDefinition) {
        return newDefinition;
    }

    /**
     * <p>Callback methods when the container is instantiated based on the {@link ContainerDefinition}.<br/>
     * At this stage, you can modify the container's definition information
     * or perform initialization or other modifications on the created container comparator.
     *
     * @param definition definition of container, if create by {@link ContainerProvider}, definition is {@code null}
     * @param container container
     * @return final effective container comparator
     * @see ContainerManager#getContainer
     */
    @Nullable
    default Container<Object> whenCreated(ContainerDefinition definition, Container<Object> container) {
        return container;
    }

    /**
     * <p>Callback methods when the container comparator is removed from the {@link ContainerManager}.<br/>
     * At this stage, you can perform some final operations
     * on the container definition or container comparator, such as clearing data caches.
     *
     * @param target container comparator or container definition
     * @see ContainerManager#clear()
     */
    default void whenDestroyed(Object target) {
        // do nothing
    }
}
