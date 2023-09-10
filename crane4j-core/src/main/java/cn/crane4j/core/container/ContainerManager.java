package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.util.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * The central interface used for managing the container and its lifecycle-related components.
 *
 * @author huangchengxing
 * @see ContainerLifecycleProcessor
 * @see ContainerDefinition
 * @since 1.3.0
 */
public interface ContainerManager extends ConfigurableContainerProvider {

    /**
     * <p>Prefix of namespace which create by container provider.
     * for example: <br/>
     * {@code getContainer("namespace&&providerName")} is equivalent to {@code getContainer("namespace", "providerName")}
     *
     * @see #getContainer(String)
     * @see #getContainer(String, String)
     */
    String PROVIDER_NAME_PREFIX = "&&";

    /**
     * Canonicalize namespace.
     *
     * @param namespace namespace
     * @return container
     */
    static String canonicalNamespace(String namespace, @Nullable String providerName) {
        return StringUtils.isEmpty(providerName) ?
                namespace : providerName + PROVIDER_NAME_PREFIX + namespace;
    }

    /**
     * Clear all data caches.
     *
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    void clear();

    // =============== lifecycle lifecycle  ===============

    /**
     * Register {@link ContainerLifecycleProcessor}.
     *
     * @param lifecycle lifecycle
     */
    void registerContainerLifecycleProcessor(ContainerLifecycleProcessor lifecycle);

    /**
     * Get all registered {@link ContainerLifecycleProcessor}.
     *
     * @return {@link ContainerLifecycleProcessor}
     */
    Collection<ContainerLifecycleProcessor> getContainerLifecycleProcessors();

    // =============== container provider ===============

    /**
     * Register {@link ContainerProvider} by given name.
     *
     * @param name              name
     * @param containerProvider containerProvider
     */
    void registerContainerProvider(String name, ContainerProvider containerProvider);

    /**
     * Get {@link ContainerProvider} by given name.
     *
     * @param name name
     * @return {@link ContainerProvider} instance
     */
    @Nullable
    <T extends ContainerProvider> T getContainerProvider(String name);

    // =============== register container  ===============

    /**
     * Register container definition.<br />
     * This operation will overwrite the existing container definition.
     *
     * @param definition definition of container
     * @return old container instance or container definition
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Nullable
    Object registerContainer(ContainerDefinition definition);

    /**
     * Register container definition by given arguments.<br />
     * This operation will overwrite the existing container definition.
     *
     * @param namespace namespace of container
     * @param factory factory method of container instance
     * @return old container instance or container definition
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Nullable
    default Object registerContainer(
        String namespace, Supplier<Container<Object>> factory) {
        ContainerDefinition definition = new ContainerDefinition.SimpleContainerDefinition(namespace, null, factory);
        return registerContainer(definition);
    }

    /**
     * <p>Register container definition by given container instance.<br />
     * This operation will overwrite the existing container definition.
     *
     * @param container container
     * @return old container instance or container definition
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Override
    @SuppressWarnings("unchecked")
    default Object registerContainer(@NonNull Container<?> container) {
        return registerContainer(container.getNamespace(), () -> (Container<Object>) container);
    }

    // =============== container ===============

    /**
     * Obtaining and caching container instances from provider or definition.
     *
     * @param namespace namespace of container, which can also be the cache name for the container instance.
     * @param <K> key type
     * @return container instance
     * @see ContainerLifecycleProcessor#whenCreated
     */
    @Nullable
    @Override
    <K> Container<K> getContainer(String namespace);

    /**
     * Obtaining and caching container instances from the specified container provider..
     *
     * @param providerName container provider name
     * @param namespace namespace of container
     * @param <K> key type
     * @return container provider
     * @see ContainerLifecycleProcessor#whenCreated
     */
    @Nullable
    default <K> Container<K> getContainer(String providerName, String namespace) {
        return getContainer(canonicalNamespace(namespace, providerName));
    }
}
