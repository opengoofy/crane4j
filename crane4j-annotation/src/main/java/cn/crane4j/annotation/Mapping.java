package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field mapping configuration,
 * which describes which data object attributes are mapped to which target object attributes.
 *
 * @author huangchengxing
 * @see Assemble#props()
 * @see MappingTemplate
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {

    /**
     * Set both {@link #src} and {@link #ref} attributes.
     *
     * @return java.lang.String
     */
    String value() default "";

    /**
     * Data source object properties to get.
     *
     * @return field name
     */
    String src() default "";

    /**
     * The target object attribute to be set.
     * If it is empty, it defaults to the key field.
     *
     * @return field name
     */
    String ref() default "";
}
