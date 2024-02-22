package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare an operation of assembly based on the value of key property.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
 * @see cn.crane4j.core.parser.handler.AssembleKeyAnnotationHandler;
 * @since 2.6.0
 */
@Repeatable(value = AssembleKey.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleKey {

    String IDENTITY_HANDLER_PROVIDER = "NONE_HANDLE_STRATEGY";

    /**
     * The process strategy of the key value.
     *
     * @return strategy name
     */
    String provider() default IDENTITY_HANDLER_PROVIDER;

    /**
     * <p>The name of property which to reference the handled key value.<br />
     * if annotated on a field and the value is empty, the name of the annotated attribute will be used.
     *
     * @return property name
     */
    String ref() default "";

    // ================= common =================

    /**
     * Operation id.
     *
     * @return id
     */
    String id() default "";

    /**
     * <p>key field name.<br />
     * This field value will be used to obtain the associated
     * data source object from the data source container later.
     *
     * <p>When the annotation is on:
     * <ul>
     *     <li>
     *         field of this class,
     *         it will be forced to specify the name of the annotated attribute,
     *         the key value is the field value of current object;
     *     </li>
     *     <li>
     *         this class, and specify key,
     *         equivalent to directly annotating on a specified field;
     *     </li>
     *     <li>
     *         this class, and key is empty,
     *         the key value is the current object itself.
     *     </li>
     * </ul>
     *
     * @return key field name
     */
    String key() default "";

    /**
     * Sort values.
     * The lower the value, the higher the priority.
     *
     * @return sort values
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * Attributes that need to be mapped
     * between the data source object and the current object.
     *
     * @return attributes
     * @see #propTemplates()
     */
    Mapping[] props() default { };

    /**
     * <p>Mapping template classes.
     * specify a class, if {@link MappingTemplate} exists on the class,
     * it will scan and add {@link Mapping} to {@link #props()}。
     *
     * @return mapping templates
     */
    Class<?>[] propTemplates() default {};

    /**
     * The group to which the current operation belongs.
     *
     * @return groups
     */
    String[] groups() default {};

    /**
     * Get the name of property mapping strategy.
     *
     * @return strategy name
     * @see cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy
     */
    String propertyMappingStrategy() default "";

    /**
     * Batch operation.
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        AssembleKey[] value() default {};
    }
}
