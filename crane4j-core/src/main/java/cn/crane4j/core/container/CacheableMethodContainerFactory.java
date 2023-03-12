package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.core.cache.Cache;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;

import java.lang.reflect.Method;
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
 */
public class CacheableMethodContainerFactory extends DefaultMethodContainerFactory {

    public static final int ORDER = DefaultMethodContainerFactory.ORDER - 1;
    private final CacheManager cacheManager;

    public CacheableMethodContainerFactory(
        PropertyOperator propertyOperator, AnnotationFinder annotationFinder, CacheManager cacheManager) {
        super(propertyOperator, annotationFinder);
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
     * @return true if supported, false otherwise
     */
    @Override
    public boolean support(Object source, Method method) {
        ContainerCache annotation = annotationFinder.findAnnotation(method, ContainerCache.class);
        return Objects.nonNull(annotation) && super.support(source, method);
    }

    /**
     * Adapt methods to data source containers.
     *
     * @param source method's calling object
     * @param method method
     * @return data source containers
     */
    @Override
    public List<Container<Object>> get(Object source, Method method) {
        ContainerCache annotation = annotationFinder.findAnnotation(method, ContainerCache.class);
        // if cache name is not specified, the namespace of the container is taken by default
        Function<Container<Object>, Cache<Object>> containerFactory = CharSequenceUtil.isEmpty(annotation.cacheName()) ?
            container -> cacheManager.getCache(annotation.cacheName()) : container -> cacheManager.getCache(container.getNamespace());
        return super.get(source, method).stream()
            .map(container -> new CacheableContainer<>(container, containerFactory.apply(container)))
            .collect(Collectors.toList());
    }
}
