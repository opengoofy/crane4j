package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerProvider;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Logger for outputting logs during the lifecycle of a container.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ContainerRegisterLogger implements ContainerLifecycleProcessor {

    /**
     * log consumer.
     */
    private final BiConsumer<String, Object[]> logConsumer;

    /**
     * <p>Callback before registering container.<br/>
     * If the instance is created through a container, this method will not be called
     *
     * @param oldDefinition old definition of container, if not registered it is {@code null}
     * @param newDefinition new definition of container
     */
    @Override
    public ContainerDefinition whenRegistered(@Nullable ContainerDefinition oldDefinition, ContainerDefinition newDefinition) {
        if (Objects.isNull(oldDefinition)) {
            logConsumer.accept("register container definition [{}]", new Object[]{ newDefinition.getNamespace() });
        } else {
            logConsumer.accept("replace container definition [{}]", new Object[]{ oldDefinition.getNamespace() });
        }
        return newDefinition;
    }

    /**
     * Callback when container is created but still not cached.
     *
     * @param definition definition of container, if create by {@link ContainerProvider}, definition is {@code null}
     * @param container container
     * @return final effective container instance
     */
    @Nullable
    @Override
    public Container<Object> whenCreated(ContainerDefinition definition, Container<Object> container) {
        logConsumer.accept("create container instance [{}] from definition [{}]", new Object[]{ container.hashCode(), definition.getNamespace() });
        return container;
    }

    /**
     * Callback when container is destroyed.
     *
     * @param definition definition
     * @param container container, if not created it is {@code null}
     */
    @Override
    public void whenDestroyed(ContainerDefinition definition, Container<Object> container) {
        if (Objects.nonNull(container)) {
            logConsumer.accept(
                "destroy container instance [{}] of [{}]",
                new Object[]{ container.hashCode(), container.getNamespace() }
            );
        }
    }
}
