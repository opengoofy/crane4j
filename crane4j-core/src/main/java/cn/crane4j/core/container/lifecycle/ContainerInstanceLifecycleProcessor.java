package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.ContainerProvider;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

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
        Consumer<Container<Object>> initMethod = definition.getInitMethod();
        if (Objects.nonNull(initMethod)) {
            initMethod.accept(container);
        }
        return container;
    }

    /**
     * <p>Callback methods when the container instance is removed from the {@link ContainerManager}.<br/>
     * At this stage, you can perform some final operations
     * on the container definition or container instance, such as clearing data caches.
     *
     * @param definition definition
     * @param container container, if not created it is {@code null}
     * @see ContainerManager#clear()
     */
    @Override
    public void whenDestroyed(ContainerDefinition definition, Container<Object> container) {
        if (container instanceof Container.Lifecycle) {
            ((Container.Lifecycle)container).destroy();
        }
        Consumer<Container<Object>> destroyMethod = definition.getDestroyMethod();
        if (Objects.nonNull(destroyMethod)) {
            destroyMethod.accept(container);
        }
    }
}
