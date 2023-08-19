package cn.crane4j.extension.spring.annotation;

import cn.crane4j.extension.spring.util.ContainerResolveUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A markup annotation indicating that the annotation with {@link ComponentTypeScan}
 * as a meta-annotation can provide some scanning configuration,
 * from which {@link ContainerResolveUtils#resolveComponentTypesFromMetadata} can obtain
 * the type of Crane4j component that needs to be parsed.
 *
 * @author huangchengxing
 * @see ContainerResolveUtils#resolveComponentTypesFromMetadata
 * @since 2.1.0
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentTypeScan {

    /**
     * The package path which will be scanned.
     *
     * @return package path
     */
    String[] includePackages() default {};

    /**
     * The class type which will be scanned.
     *
     * @return class type
     */
    Class<?>[] includeClasses() default {};

    /**
     * The class type which will not be scanned,
     * it will exclude the class type which is specified in {@link #includeClasses()}.
     *
     * @return class type
     */
    Class<?>[] excludeClasses() default {};
}
