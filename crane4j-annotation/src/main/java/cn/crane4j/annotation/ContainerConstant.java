package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that member variables in a class can be used as containers.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.container.ConstantContainer#forConstantClass
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerConstant {

    /**
     * whether to process only public attributes.
     *
     * @return boolean
     */
    boolean onlyPublic() default true;

    /**
     * Whether to process only attributes annotated by {@link ContainerConstant.Include}.
     *
     * @return boolean
     */
    boolean onlyExplicitlyIncluded() default false;

    /**
     * The namespace corresponding to the data source container.
     * defaults {@link Class#getSimpleName()} if empty.
     *
     * @return 命名空间
     */
    String namespace() default "";

    /**
     * Specify the key name for the attribute.
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Name {
        String value();
    }

    /**
     * Include specific properties.
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Include {}

    /**
     * Does not contain specific properties.
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Exclude {}
}
