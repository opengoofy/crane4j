package cn.crane4j.annotation.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A property-value-based condition what determine whether the operation should be executed.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.condition.PropertyConditionParser
 * @since 2.6.0
 */
@Repeatable(value = ConditionOnProperty.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionOnProperty {

    /**
     * <p>The id of operations which to bound.<br/>
     * If id is empty, the condition applies to all operations
     * what declared on the same element as annotated by current annotation.
     *
     * @return operation id.
     */
    String[] id() default {};

    /**
     * The type of multi conditions.
     *
     * @return condition type
     */
    ConditionType type() default ConditionType.AND;

    /**
     * Whether the current condition to be negated.
     *
     * @return boolean
     */
    boolean negation() default false;

    /**
     * Get the order of the condition.
     *
     * @return sort
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * <p>The property name.<br/>
     * When this annotation is used on a field, the property name is the field name by default.
     *
     * @return property name
     */
    String property() default "";

    /**
     * The expected property value.
     *
     * @return property value.
     */
    String value() default "";

    /**
     * The type of expected property value.
     *
     * @return expected property value
     */
    Class<?> valueType() default Object.class;

    /**
     * Whether to enable apply operation when the property value is null.
     *
     * @return true if enabled, otherwise false.
     */
    boolean enableNull() default false;

    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ConditionOnProperty[] value();
    }
}
