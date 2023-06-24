package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a method has been annotated by {@link ContainerMethod},
 * upgrade it to a cacheable container.
 *
 * @author huangchengxing
 * @see ContainerMethod
 * @see cn.crane4j.core.support.container.CacheableMethodContainerFactory
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerCache {

    // TODO supprot specified cache manager and expire time

    /**
     * The cache name, when empty, defaults to {@link ContainerMethod#namespace()} of the marked method.
     *
     * @return cache name
     */
    String cacheName() default "";
}
