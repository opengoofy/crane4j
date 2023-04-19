package cn.crane4j.annotation;

import java.lang.annotation.*;

/**
 * <p>Marker an annotation can parse to a standard assemble operation.<br />
 * standard assemble operation must have the following attributes:
 * <ul>
 *     <li>{@code String key() default ""};</li>
 *     <li>{@code int sort() default Integer.MAX_VALUE};</li>
 *     <li>{@code String handlerName() default ""};</li>
 *     <li>{@code Class<?> handler() default Object.class};</li>
 *     <li>{@code Mapping[] props() default { }};</li>
 *     <li>{@code Class<?>[] propTemplates() default {}};</li>
 *     <li>{@code String[] groups() default {}};</li>
 * </ul>
 *
 * @author huangchengxing
 * @see cn.crane4j.core.parser.StandardAssembleOperationResolver
 * @since 1.3.0
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StandardAssembleAnnotation {

    /**
     * {@code String key() default ""}.<br />
     * Key field name for query, if it is empty, it defaults to the field annotated by {@code @TableId}.
     *
     * @return field name
     */
    String keyAttribute() default "key";

    /**
     * {@code int sort() default Integer.MAX_VALUE}.<br />
     * Sort values.
     * The lower the value, the higher the priority.
     *
     * @return sort values
     */
    String sortAttribute() default "sort";

    /**
     * {@code String handlerName() default ""}.<br />
     * The name of the handler to be used.
     *
     * @return name
     */
    String handlerNameAttribute() default "handlerName";

    /**
     * {@code Class<?> handler() default Object.class}.<br />
     * The type of the handler to be used.
     *
     * @return type
     */
    String handlerAttribute() default "handler";

    /**
     * {@code Mapping[] props() default { }}.<br />
     * Attributes that need to be mapped
     * between the data source object and the current object.
     *
     * @return attributes
     */
    String propsAttribute() default "props";

    /**
     * {@code Class<?>[] propTemplates() default {}}.<br />
     * <p>Mapping template classes.
     * specify a class, if {@link MappingTemplate} exists on the class,
     * it will scan and add {@link Mapping} to {@link #props()}。
     *
     * @return mapping templates
     */
    String propTemplatesAttribute() default "propTemplates";

    /**
     * {@code String[] groups() default {}}.<br />
     * The group to which the current operation belongs.
     *
     * @return groups
     */
    String groupsAttribute() default "groups";
}
