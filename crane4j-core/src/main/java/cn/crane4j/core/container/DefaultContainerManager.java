package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ConfigurationUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A base implementation of {@link ContainerManager}.
 *
 * @author huangchengxing
 */
@Slf4j
public class DefaultContainerManager implements ContainerManager {

    /**
     * Registered container definitions.
     */
    protected final Map<Object, ContainerDefinition> containerDefinitionMap = new ConcurrentHashMap<>(64);

    /**
     * Container singleton caches.
     */
    protected final Map<Object, Container<Object>> containerMap = new ConcurrentHashMap<>(64);

    /**
     * Registered container lifecycle callbacks.
     */
    protected final List<ContainerLifecycleProcessor> containerLifecycleProcessorList = new ArrayList<>();

    /**
     * Registered container provider.
     */
    protected final Map<String, ContainerProvider> containerProviderMap = new LinkedHashMap<>();

    // =============== lifecycle lifecycle  ===============

    /**
     * Register {@link ContainerLifecycleProcessor}.
     *
     * @param lifecycle lifecycle
     */
    @Override
    public void registerContainerLifecycleProcessor(ContainerLifecycleProcessor lifecycle) {
        containerLifecycleProcessorList.add(lifecycle);
    }

    /**
     * Get all registered {@link ContainerLifecycleProcessor}.
     *
     * @return {@link ContainerLifecycleProcessor}
     */
    @Override
    public Collection<ContainerLifecycleProcessor> getContainerLifecycleProcessors() {
        return containerLifecycleProcessorList;
    }

    // =============== container provider  ===============

    /**
     * Register {@link ContainerProvider} by given name.
     *
     * @param name              name
     * @param containerProvider containerProvider
     */
    @Override
    public void registerContainerProvider(String name, ContainerProvider containerProvider) {
        containerProviderMap.put(name, containerProvider);
    }

    /**
     * Get {@link ContainerProvider} by given name.
     *
     * @param name name
     * @return {@link ContainerProvider} instance
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends ContainerProvider> T getContainerProvider(String name) {
        return (T)containerProviderMap.get(name);
    }

    /**
     * Get all registered {@link ContainerProvider}.
     *
     * @return {@link ContainerProvider} instances
     */
    @Nullable
    @Override
    public Collection<ContainerProvider> getContainerProviders() {
        return containerProviderMap.values();
    }

    // =============== container  ===============

    /**
     * Register container
     *
     * @param definition definition of container
     * @return old definition
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Nullable
    @Override
    public ContainerDefinition registerContainer(ContainerDefinition definition) {
        Asserts.isNotNull(definition, "definition must not null");
        Object key = getCacheKey(definition.getNamespace());
        return containerDefinitionMap.compute(key, (k, oldDefinition) -> {
            // process new definition
            ContainerDefinition newDefinition = ConfigurationUtil.triggerWhenRegistered(
                definition, key.toString(), oldDefinition, containerLifecycleProcessorList, log
            );
            // no change if new definition is null
            if (Objects.isNull(newDefinition)) {
                return oldDefinition;
            }

            // remove old container if necessary
            Container<Object> oldContainer = containerMap.remove(k);
            if (Objects.nonNull(oldContainer)) {
                ConfigurationUtil.triggerWhenDestroyed(oldDefinition, oldContainer, containerLifecycleProcessorList);
            }
            return newDefinition;
        });
    }

    /**
     * Obtaining and caching container instances from provider or definition.
     *
     * @param namespace namespace of container, which can also be the cache name for the container instance.
     * @return container instance
     * @see ContainerLifecycleProcessor#whenCreated
     * @see #doGetContainer(CacheKey)
     */
    @SuppressWarnings("all")
    @Nullable
    @Override
    public <K> Container<K> getContainer(String namespace) {
        if (Objects.equals(namespace, Container.EMPTY_CONTAINER_NAMESPACE)) {
            return Container.empty();
        }
        // container already cached?
        Object key = getCacheKey(namespace);
        Container<Object> container = containerMap.get(key);
        if (Objects.nonNull(container)) {
            return (Container<K>) container;
        }
        // create if necessary
        return (Container<K>) containerMap.compute(key, (k, c) -> {
            // if already created
            if (Objects.nonNull(c)) {
                return c;
            }
            // get definition
            ContainerDefinition definition = containerDefinitionMap.get(k);
            if (Objects.isNull(definition)) {
                definition = containerDefinitionMap.compute(
                        k, (n, def) -> Objects.nonNull(def) ? def : createDefinition(n)
                );
            }
            // is unregistered container or processor returned null
            if (Objects.isNull(definition)) {
                return null;
            }

            // create instance
            return createContainer(k.toString(), definition);
        });
    }

    /**
     * Whether this provider has container of given {@code namespace}.
     *
     * @param namespace namespace
     * @return boolean
     */
    @Override
    public boolean containsContainer(String namespace) {
        Object key = getCacheKey(namespace);
        if (key instanceof String) {
            return containerMap.containsKey(key) || containerDefinitionMap.containsKey(key);
        }
        CacheKey ck = (CacheKey) key;
        return Optional.ofNullable(containerProviderMap.get(ck.getProviderName()))
                .map(containerProvider -> containerProvider.containsContainer(ck.getProviderName()))
                .orElse(false);
    }

    /**
     * Clear all data caches.
     *
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Override
    public void clear() {
        containerMap.forEach((ns, c) -> {
            Container<Object> container = containerMap.remove(ns);
            ContainerDefinition definition = containerDefinitionMap.remove(ns);
            if (Objects.nonNull(container)) {
                ConfigurationUtil.triggerWhenDestroyed(definition, container, containerLifecycleProcessorList);
            }
        });
        containerMap.clear();
        containerDefinitionMap.clear();
        containerProviderMap.clear();
        containerLifecycleProcessorList.clear();
    }

    // ================ product methods ================

    /**
     * <p>Create container instance by given {@link CacheKey}.<br/>
     * if definition is null, then try to create and register definition by provider based factory method first,
     * then create and cache container instance.
     * 
     * <p>It may lock {@link #containerMap}.
     *
     * @param namespace namespace
     * @param definition definition of container
     * @return container instance
     * @see ContainerLifecycleProcessor#whenCreated 
     */
    @Nullable
    protected Container<Object> createContainer(
            String namespace, ContainerDefinition definition) {
        Container<Object> container = definition.createContainer();
        return ConfigurationUtil.triggerWhenCreated(
                namespace, definition, container, containerLifecycleProcessorList, log
        );
    }

    /**
     * <p>Create definition by provider based factory method.<br/>
     * if namespace is not belong to any container which has specified provider, then return null.
     *
     * @param cacheKey namespace
     * @return container definition
     * @see ContainerLifecycleProcessor#whenRegistered
     */
    @Nullable
    protected ContainerDefinition createDefinition(Object cacheKey) {
        // is unregistered container
        if (!(cacheKey instanceof CacheKey)) {
            return null;
        }

        // the container need create from provider,
        // try to create and register definition by provider based factory method
        CacheKey key = (CacheKey) cacheKey;
        ContainerProvider containerProvider = getContainerProvider(key.getProviderName());
        if (Objects.isNull(containerProvider)) {
            return null;
        }
        ContainerDefinition definition = new ContainerDefinition.SimpleContainerDefinition(
                key.getNamespace(), key.getProviderName(),
            () -> containerProvider.getContainer(key.getNamespace())
        );
        return ConfigurationUtil.triggerWhenRegistered(
            definition, key.toString(), null, containerLifecycleProcessorList, log
        );
    }

    /**
     * Get cache key for container instance.
     *
     * @param namespace namespace of container,
     *                   which can also be the cache name for the container instance.
     * @return if namespace contains provider name, then return {@link CacheKey}, otherwise return {@link String}.
     * @see #PROVIDER_NAME_PREFIX
     */
    protected Object getCacheKey(String namespace) {
        int index = namespace.indexOf(PROVIDER_NAME_PREFIX);
        if (index < 0) {
            return namespace;
        }
        String containerName = namespace.substring(0, index);
        String providerName = namespace.substring(index + 2);
        return new CacheKey(containerName, providerName);
    }

    /**
     * Cache key for namespace with provider name
     *
     * @author huangchengxing
     */
    @EqualsAndHashCode
    @RequiredArgsConstructor
    @Getter
    protected static class CacheKey {
        @EqualsAndHashCode.Include
        private final String namespace;
        @EqualsAndHashCode.Include
        private final String providerName;
    }
}
