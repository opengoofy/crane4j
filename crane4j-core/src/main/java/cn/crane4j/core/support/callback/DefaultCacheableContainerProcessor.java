package cn.crane4j.core.support.callback;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * <p>The default implementation of {@link ContainerRegisterAware}. <br />
 * Used to wrap the container as a cacheable container
 * according to container-cache configuration before registering it.
 *
 * @author huangchengxing
 * @see CacheableContainer
 */
@Slf4j
public class DefaultCacheableContainerProcessor implements ContainerRegisterAware {

    private final CacheManager cacheManager;
    private final Map<String, String> containerConfigs;

    /**
     * Create a {@link DefaultCacheableContainerProcessor} instance.
     *
     * @param cacheManager cache manager
     * @param config config list of cache name and container
     */
    public DefaultCacheableContainerProcessor(CacheManager cacheManager, Map<String, String> config) {
        this.cacheManager = cacheManager;
        this.containerConfigs = config;
    }

    /**
     * Called before {@link Container} is registered to {@link ContainerProvider}.<br />
     * If the return value is {@code null}, the registration of the container will be abandoned
     *
     * @param operator  caller of the current method
     * @param container container
     * @return {@link Container} who really wants to be registered to {@link ContainerProvider}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Container<?> beforeContainerRegister(Object operator, @Nonnull Container<?> container) {
        String cacheName = containerConfigs.get(container.getNamespace());
        if (Objects.nonNull(cacheName)) {
            log.info("use cache [{}] for container [{}]", cacheName, container.getNamespace());
            container = new CacheableContainer<>((Container<Object>)container, cacheManager, cacheName);
        }
        return container;
    }
}
