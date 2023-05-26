package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * <p>Processor for wrap the container as a cacheable container
 * according to container-cache configuration before registering it.
 *
 * @author huangchengxing
 * @see CacheableContainer
 */
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class CacheableContainerProcessor implements ContainerLifecycleProcessor {

    /**
     * Cache manager.
     */
    private final CacheManager cacheManager;

    /**
     * <p>Cache Selector.<br/>
     * If the obtained cache name is null or an empty string, it will not be wrapped into a cache container.
     */
    @Setter
    private BiFunction<ContainerDefinition, Container<Object>, String> cacheSelector = (definition, container) -> null;

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
        String cacheName = cacheSelector.apply(definition, container);
        if (StringUtils.isNotEmpty(cacheName)) {
            log.info("use cache [{}] for container [{}]", cacheName, container.getNamespace());
            container = new CacheableContainer<>(container, cacheManager, cacheName);
        }
        return container;
    }
}
