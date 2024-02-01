package cn.crane4j.extension.spring;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.core.cache.CacheDefinition;
import cn.crane4j.core.cache.CacheableContainerProcessor;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.Objects;

/**
 * A {@link CacheableContainerProcessor} implementation
 * which support process spring annotation and bean proxy.
 *
 * @author huangchengxing
 */
public class SpringCacheableContainerProcessor extends CacheableContainerProcessor {

    public SpringCacheableContainerProcessor(Crane4jGlobalConfiguration configuration) {
        super(configuration);
    }

    /**
     * Get container class.
     *
     * @param container container
     * @return annotation
     */
    @Override
    protected CacheDefinition getCacheDefinitionFromContainer(Container<Object> container) {
        CacheDefinition definition = super.getCacheDefinitionFromContainer(container);
        if (Objects.nonNull(definition)) {
            return definition;
        }
        Class<?> containerClass = AopUtils.getTargetClass(container);
        ContainerCache annotation = AnnotatedElementUtils.findMergedAnnotation(containerClass, ContainerCache.class);
        return Objects.isNull(annotation) ? null : new CacheDefinition.Impl(
            container.getNamespace(), annotation.cacheManager(),
            annotation.expirationTime(), annotation.timeUnit()
        );
    }
}
