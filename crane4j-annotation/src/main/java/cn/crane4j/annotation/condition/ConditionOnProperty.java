package cn.crane4j.annotation.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A property value based condition what determine whether the operation should be executed.
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
     * The id of operations which to bound.
     *
     * @return operation id.
     */
    String[] id() default {};

    /**
     * The property name.
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
