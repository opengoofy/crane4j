package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerProvider;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

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
     * @param old old container instance or container definition
     * @param newDefinition new definition of container
     */
    @Override
    public ContainerDefinition whenRegistered(@Nullable Object old, ContainerDefinition newDefinition) {
        if (Objects.isNull(old)) {
            logConsumer.accept("register container definition [{}]", new Object[]{ newDefinition.getNamespace() });
        } else {
            logConsumer.accept("replace container definition [{}]", new Object[]{ newDefinition.getNamespace() });
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
        logConsumer.accept("create container [{}] from definition [{}]", new Object[]{ container.hashCode(), definition.getNamespace() });
        return container;
    }

    /**
     * Callback when container is destroyed.
     *
     * @param target container instance or container definition
     */
    @Override
    public void whenDestroyed(Object target) {
        if (target instanceof ContainerDefinition) {
            String namespace = ((ContainerDefinition) target).getNamespace();
            logConsumer.accept("destroy container definition [{}] of [{}]", new Object[]{ namespace });
            return;
        }
        Container<?> container = (Container<?>) target;
        logConsumer.accept(
            "destroy container comparator [{}]",
            new Object[]{ container.hashCode(), container.getNamespace() }
        );
    }
}
