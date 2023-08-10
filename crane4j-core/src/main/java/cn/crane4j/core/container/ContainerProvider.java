package cn.crane4j.core.container;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Provider for conveniently registering container definitions
 * with the container manager and providing instances of containers generated based on specific rules,
 * similar to FactoryBean in spring.
 *
 * <p>Since the manager caches all container instances created within its management scope,
 * the container provider does not need to cache the created container instances
 * unless there are specific cases where multiple namespaces require the same instance.
 *
 * @author huangchengxing
 * @see ContainerManager
 * @see Container
 */
public interface ContainerProvider {

    /**
     * Get container instance by given namespace
     *
     * @param namespace namespace of container
     * @param <K> key type
     * @return container instance
     */
    @Nullable
    <K> Container<K> getContainer(String namespace);
    
    /**
     * Whether this provider has container of given {@code namespace}.
     *
     * @param namespace namespace
     * @return boolean
     */
    default boolean containsContainer(String namespace) {
        return true;
    }
}
