package cn.crane4j.annotation.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A condition what apply the operation only when
 * the target which to be operated is assignable from the specified type.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.condition.ConditionOnTargetTypeParser
 * @since 2.6.0
 */
@Repeatable(value = ConditionOnTargetType.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionOnTargetType {

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
     * Type of the target.
     *
     * @return namespace
     */
    Class<?>[] value() default {};

    /**
     * Whether the target type should be strictly matched.
     *
     * @return true if the target type should be strictly matched, otherwise false
     */
    boolean strict() default false;

    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ConditionOnTargetType[] value();
    }
}
