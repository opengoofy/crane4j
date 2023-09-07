package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>The extension implementation of {@link DefaultMethodContainerFactory}.<br />
 * On the basis of the former, if {@link ContainerCache} annotation exists on the method,
 * the obtained method container will be wrapped as {@link CacheableContainer}.
 *
 * @author huangchengxing
 * @see ContainerCache
 * @see CacheableContainer
 */
@Slf4j
public class CacheableMethodContainerFactory extends DefaultMethodContainerFactory {

    public static final int ORDER = DefaultMethodContainerFactory.ORDER - 1;
    private final CacheManager cacheManager;

    public CacheableMethodContainerFactory(
        MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder, CacheManager cacheManager) {
        super(methodInvokerContainerCreator, annotationFinder);
        this.cacheManager = cacheManager;
    }

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    @Override
    public int getSort() {
        return ORDER;
    }

    /**
     * Whether the method is supported.
     *
     * @param source method's calling object
     * @param method method
     * @param annotations annotations
     * @return true if supported, false otherwise
     */
    @Override
    public boolean support(@Nullable Object source, Method method, Collection<ContainerMethod> annotations) {
        ContainerCache annotation = annotationFinder.findAnnotation(method, ContainerCache.class);
        return Objects.nonNull(annotation) && super.support(source, method, annotations);
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
        // if cache name is not specified, the namespace of the container is taken by default
        Function<Container<Object>, String> cacheNameFactory = container -> StringUtils.emptyToDefault(annotation.cacheName(), container.getNamespace());
        return super.get(source, method, annotations).stream()
            .map(container -> new CacheableContainer<>(container, cacheManager, cacheNameFactory.apply(container)))
            .collect(Collectors.toList());
    }
}
