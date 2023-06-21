package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.ContainerProvider;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

/**
 * Processor for init and destroy container.
 *
 * @author huangchengxing
 */
@Slf4j
public class ContainerInstanceLifecycleProcessor implements ContainerLifecycleProcessor {

    /**
     * <p>Callback methods when the container is instantiated based on the {@link ContainerDefinition}.<br/>
     * At this stage, you can modify the container's definition information
     * or perform initialization or other modifications on the created container instance.
     *
     * @param definition definition of container, if create by {@link ContainerProvider}, definition is {@code null}
     * @param container  container
     * @return final effective container instance
     * @see ContainerManager#getContainer
     */
    @Nullable
    @Override
    public Container<Object> whenCreated(ContainerDefinition definition, Container<Object> container) {
        if (container instanceof Container.Lifecycle) {
            ((Container.Lifecycle)container).init();
        }
        return container;
    }

    /**
     * <p>Callback methods when the container instance is removed from the {@link ContainerManager}.<br/>
     * At this stage, you can perform some final operations
     * on the container definition or container instance, such as clearing data caches.
     *
     * @param target container instance or container definition
     * @see ContainerManager#clear()
     */
    @Override
    public void whenDestroyed(Object target) {
        if (target instanceof Container.Lifecycle) {
            ((Container.Lifecycle)target).destroy();
        }
    }
}
