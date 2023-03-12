package cn.crane4j.annotation.extension;

import cn.crane4j.annotation.Bind;
import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation on combination of {@link CacheContainerMethod} and {@link ContainerMethod}.
 *
 * @author huangchengxing
 * @see ContainerCache
 * @see ContainerMethod
 */
@ContainerMethod(resultType = Object.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheContainerMethod {

    /**
     * The cache name, when empty, defaults to {@link ContainerMethod#namespace()} of the marked method.
     *
     * @return cache name
     * @see ContainerCache#cacheName()
     */
    String cacheName() default "";

    /**
     * Namespace of the data source container, use method name when empty.
     *
     * @return namespace
     * @see ContainerMethod#namespace()
     */
    String namespace() default "";

    /**
     * The mapping relationship between the object returned by the method and the target object.
     *
     * @return mapping relationship
     * @see ContainerMethod#type()
     */
    MappingType type() default MappingType.ONE_TO_ONE;

    /**
     * The key field of the data source object returned by the method.
     *
     * @return key field name
     * @see ContainerMethod#resultKey()
     */
    String resultKey() default "id";

    /**
     * Data source object type returned by method.
     *
     * @return type
     * @see ContainerMethod#resultType()
     */
    Class<?> resultType();

    /**
     * When annotations are used on a class,
     * they are used to bind the corresponding methods in the class.
     *
     * @return method to find
     * @see ContainerMethod#bind()
     */
    Bind bind() default @Bind("");
}
