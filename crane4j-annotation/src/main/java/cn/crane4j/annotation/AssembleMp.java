package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare an assembly operation using the mybatis plus default interface method as the data source.
 *
 * @author huangchengxing
 * @see cn.crane4j.extension.mybatis.plus.MpAnnotationOperationsResolver
 * @since 1.2.0
 */
@Repeatable(value = AssembleMp.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleMp {

    /**
     * Bean name of mapper interface.
     *
     * @return bean name
     */
    String mapper();

    /**
     * Fields to query, if it is empty, all table columns will be queried by default.
     *
     * @return field names
     */
    String where() default "";

    /**
     * Fields to query, if it is empty, all table columns will be queried by default.
     *
     * @return field names
     */
    String[] selects() default {};

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
        AssembleMp[] value() default {};
    }
}
