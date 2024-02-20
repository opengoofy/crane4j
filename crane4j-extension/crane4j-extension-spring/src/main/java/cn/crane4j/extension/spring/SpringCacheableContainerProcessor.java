package cn.crane4j.extension.spring;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.core.cache.CacheDefinition;
import cn.crane4j.core.cache.CacheableContainerProcessor;
import cn.crane4j.core.container.Container;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.Objects;

/**
 * A {@link CacheableContainerProcessor} implementation
 * which support process spring annotation and bean proxy.
 *
 * @author huangchengxing
 */
public class SpringCacheableContainerProcessor extends CacheableContainerProcessor {

    public SpringCacheableContainerProcessor(Crane4jApplicationContext configuration) {
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
        ContainerCache annotation = findAnnotation(container);
        return Objects.isNull(annotation) ? null : new CacheDefinition.Impl(
            container.getNamespace(), annotation.cacheManager(),
            annotation.expirationTime(), annotation.timeUnit()
        );
    }

    @Nullable
    private ContainerCache findAnnotation(Container<Object> container) {
        Crane4jApplicationContext context = (Crane4jApplicationContext)configuration;
        String beanName = context.getBeanNameByNamespace(container.getNamespace());
        if (Objects.nonNull(beanName)) {
            ApplicationContext applicationContext = context.getApplicationContext();
            return applicationContext.findAnnotationOnBean(beanName, ContainerCache.class);
        }
        Class<?> containerClass = AopUtils.getTargetClass(container);
        return AnnotatedElementUtils.findMergedAnnotation(containerClass, ContainerCache.class);
    }
}
