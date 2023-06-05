package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author huangchengxing
 */
@Repeatable(value = AssembleEnum.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleEnum {

    /**
     * Enum class.
     *
     * @return enum class
     */
    Class<? extends Enum<?>> type();

    /**
     * Enum key field name.
     *
     * @return field name
     */
    String enumKey() default "";

    /**
     * Enum value field name.
     *
     * @return field name
     */
    String enumValue() default "";

    /**
     * <p>a quick set for reference field name, equivalent to {@code @Mapping(ref = "")}.<br />
     * when not empty, the value is jointly effective  with {@link #props()}.
     *
     * @return reference field name
     * @see #props()
     */
    String ref() default "";

    /**
     * If the {@link #type()} is annotated with {@link ContainerEnum},
     * the configuration defined by that annotation will be used first.
     *
     * @return boolean
     */
    boolean useContainerEnum() default false;

    // ================= common =================

    /**
     * Key field name for query, if it is empty, it defaults to the field annotated by {@code @TableId}.
     *
     * @return field name
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
     * The name of the handler to be used.
     *
     * @return name
     */
    String handlerName() default "";

    /**
     * The type of the handler to be used.
     *
     * @return type
     */
    Class<?> handler() default Object.class;

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
     * Batch operation.
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        AssembleEnum[] value() default {};
    }
}
