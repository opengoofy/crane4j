package cn.crane4j.annotation;

import java.lang.annotation.*;

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
     * @return namespace
     */
    String namespace() default "";

    /**
     * Whether to reverse the key value pair.
     * When this item is set to {@code true},
     * the constant attribute value will be used as the key,
     * and the constant attribute name will be used as the value.
     *
     * @return boolean
     */
    boolean reverse() default false;

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
