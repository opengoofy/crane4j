package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.cache.CacheDefinition;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>The extension implementation of {@link DefaultMethodContainerFactory}.<br />
 * On the basis of the former, if {@link ContainerCache} annotation exists on the method,
 * the obtained method container will be wrapped as {@link CacheableContainer}.
 *
 * <p><b>NOTE</b>: Not recommended to use with {@link DefaultMethodContainerFactory},
 * in actual use, only one of them is needed to be configured.
 *
 * @author huangchengxing
 * @see ContainerCache
 * @see CacheableContainer
 */
@Slf4j
public class CacheableMethodContainerFactory extends DefaultMethodContainerFactory {

    private final Crane4jGlobalConfiguration configuration;

    public CacheableMethodContainerFactory(
        MethodInvokerContainerCreator methodInvokerContainerCreator,
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        super(methodInvokerContainerCreator, annotationFinder);
        this.configuration = configuration;
    }

    /**
     * Adapt methods to data source containers.
     *
     * @param source method's calling object
     * @param method method
     * @param annotations annotations
     * @return data source containers
     */
    @Override
    public List<Container<Object>> get(@Nullable Object source, Method method, Collection<ContainerMethod> annotations) {
        log.debug("create cacheable method container from [{}]", method);
        ContainerCache annotation = annotationFinder.findAnnotation(method, ContainerCache.class);
        if (Objects.isNull(annotation)) {
            return super.get(source, method, annotations);
        }
        // wrap method container as cacheable container
        String managerName = StringUtils.emptyToDefault(annotation.cacheManager(), CacheManager.DEFAULT_MAP_CACHE_MANAGER_NAME);
        CacheManager cacheManager = configuration.getCacheManager(managerName);
        return super.get(source, method, annotations).stream()
            .map(container -> {
                CacheDefinition cacheDefinition = new CacheDefinition.Impl(
                    container.getNamespace(), managerName,
                    annotation.expirationTime(), annotation.timeUnit()
                );
                return new CacheableContainer<>(container, cacheDefinition, cacheManager);
            })
            .collect(Collectors.toList());
    }
}
