package cn.crane4j.core.cache;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>Processor for wrap the container as a cacheable container
 * according to container-cache configuration before registering it.
 *
 * @author huangchengxing
 * @see ContainerCache
 * @see CacheableContainer
 * @since 2.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CacheableContainerProcessor implements ContainerLifecycleProcessor {

    private final Crane4jGlobalConfiguration configuration;
    private final AnnotationFinder annotationFinder;

    /**
     * <p>Cache Selector.<br/>
     * If the obtained cache name is null or an empty string, it will not be wrapped into a cache container.
     *
     * @see CacheDefinition
     */
    @NonNull
    @Setter
    private CacheDefinitionRetriever cacheDefinitionRetriever = (definition, container) -> null;

    /**
     * Callback when container is created.
     *
     * @param definition definition of container
     * @param container  container
     * @return final effective container instance
     */
    @Nullable
    @Override
    public Container<Object> whenCreated(ContainerDefinition definition, Container<Object> container) {
        CacheDefinition cacheDefinition = retrieveCacheDefinition(definition, container);
        if (Objects.isNull(cacheDefinition)) {
            return container;
        }
        Long expireTime = cacheDefinition.getExpireTime();
        TimeUnit timeUnit = cacheDefinition.getTimeUnit();
        log.info("apply cache to container [{}], expire time is [{}] {}", cacheDefinition.getName(), expireTime, timeUnit.name().toLowerCase());
        // if cache manager is not specified, use default cache manager
        String cacheManagerName = StringUtils.emptyToDefault(
            cacheDefinition.getCacheManager(), CacheManager.DEFAULT_MAP_CACHE_MANAGER_NAME
        );
        CacheManager cacheManager = configuration.getCacheManager(cacheManagerName);
        container = new CacheableContainer<>(container, cacheDefinition, cacheManager);
        return container;
    }

    @Nullable
    private CacheDefinition retrieveCacheDefinition(ContainerDefinition definition, Container<Object> container) {
        // try retrieve by retriever
        CacheDefinition cacheDefinition = cacheDefinitionRetriever.retrieve(definition, container);
        if (Objects.nonNull(cacheDefinition)) {
            return cacheDefinition;
        }
        // try resolve from annotation
        Class<?> containerClass = container.getClass();
        ContainerCache cacheable = annotationFinder.findAnnotation(containerClass, ContainerCache.class);
        if (Objects.isNull(cacheable)) {
            return null;
        }
        return new CacheDefinition.Impl(
            container.getNamespace(), cacheable.cacheManager(),
            cacheable.expirationTime(), cacheable.timeUnit()
        );
    }

    /**
     * Retriever for cache definition.
     *
     * @since 2.4.0
     */
    @FunctionalInterface
    public interface CacheDefinitionRetriever {

        /**
         * Retrieve cache definition.
         *
         * @param containerDefinition container definition
         * @param container container
         * @return cache definition
         */
        CacheDefinition retrieve(ContainerDefinition containerDefinition, Container<Object> container);
    }
}
