package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * <p>An annotation to mark a container as cacheable.<br />
 * It can be used on a method which annotated by {@link ContainerMethod},
 * or a class which implements {@link cn.crane4j.core.container.Container} interface.
 *
 * <p>The actual cache implementation is determined by what cache manager is specified in the annotation,
 * when container is created, the cache factory will be used to create a cache instance,
 * and wrap the container with {@link cn.crane4j.core.cache.CacheableContainer}.
 *
 * @author huangchengxing
 * @see ContainerMethod
 * @see cn.crane4j.core.cache.CacheManager
 * @see cn.crane4j.core.cache.CacheableContainerProcessor
 * @see cn.crane4j.core.support.container.CacheableMethodContainerFactory
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerCache {

    /**
     * The name of the cache manager
     *
     * @return cache manager name
     * @see cn.crane4j.core.cache.CacheManager
     *
     * @since 2.4.0
     */
    String cacheManager() default "";

    /**
     * The time to live of the cache,
     * default to -1L, which means the cache will never proactive evict.
     *
     * @return time to live
     * @since 2.4.0
     */
    long expirationTime() default -1L;

    /**
     * The time unit of the cache expiry time,
     * default to {@link TimeUnit#MILLISECONDS}.
     *
     * @return time unit
     * @since 2.4.0
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
