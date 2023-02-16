package cn.createsequence.crane4j.core.annotation;

import cn.createsequence.crane4j.core.container.CacheableMethodContainerFactory;
import cn.createsequence.crane4j.core.container.Container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 若一个方法已经被{@link ContainerMethod}注解，
 * 则在该方法上添加本注解可将其升级为带缓存功能的容器。
 *
 * @author huangchengxing
 * @see ContainerMethod
 * @see CacheableMethodContainerFactory
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerCache {

    /**
     * 缓存名，为空时默认为{@link Container#getNamespace()}
     *
     * @return 缓存的命名空间
     */
    String value() default "";
}
