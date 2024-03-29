package cn.crane4j.core.container;

import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>A {@link ContainerProvider} implementation for conveniently registering container.<br/>
 * When get container by given namespace, it will return the container registered by {@link #registerContainer(Container)},
 * if not exist, it will return the container created by {@link #defaultContainerFactory}.
 *
 * @author huangchengxing
 * @since 2.0.0
 */
public class PartitionContainerProvider implements ConfigurableContainerProvider {

    /**
     * Container map.
     */
    private final Map<String, Container<Object>> containerMap = new HashMap<>();

    /**
     * Default container factory for non-existent container.
     */
    @NonNull
    @Setter
    private Function<String, Container<Object>> defaultContainerFactory = namespace -> null;

    /**
     * Get container comparator by given namespace
     *
     * @param namespace namespace of container
     * @return container comparator
     */
    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <K> Container<K> getContainer(String namespace) {
        return (Container<K>)containerMap.getOrDefault(namespace, defaultContainerFactory.apply(namespace));
    }

    /**
     * Register container.
     *
     * @param container container
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object registerContainer(@NonNull Container<?> container) {
        Objects.requireNonNull(container, "Container must not null");
        return containerMap.put(container.getNamespace(), (Container<Object>)container);
    }

    /**
     * Whether this provider has container of given {@code namespace}.
     *
     * @param namespace namespace
     * @return boolean
     */
    @Override
    public boolean containsContainer(String namespace) {
        return Objects.nonNull(getContainer(namespace));
    }
}
