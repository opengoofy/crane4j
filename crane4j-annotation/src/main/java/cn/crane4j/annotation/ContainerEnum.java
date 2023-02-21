package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The enumeration representing the annotation can be converted to an enumeration container of the specified type.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.container.ConstantContainer#forEnum
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerEnum {

    /**
     * The namespace corresponding to the data source container, {@link Class#getSimpleName()} if empty.
     *
     * @return namespace
     */
    String namespace() default "";

    /**
     * Key of item, {@link Enum#name()} if empty.
     *
     * @return key field name
     */
    String key() default "";

    /**
     * The value corresponding to the key value.
     * If it is not filled in, it defaults to the enumeration item itself
     *
     * @return value field name
     */
    String value() default "";
}
