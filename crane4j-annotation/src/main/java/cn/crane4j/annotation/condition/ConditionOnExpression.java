package cn.crane4j.annotation.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An expression based condition what determine whether the operation should be executed.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.condition.ExpressionConditionParser
 * @since 2.6.0
 */
@Repeatable(value = ConditionOnExpression.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionOnExpression {

    /**
     * The id of operations which to bound.
     *
     * @return operation id.
     */
    String[] id() default {};

    /**
     * Expression
     *
     * @return expressions
     */
    String value() default "";

    // TODO support choose "all-match" or "any-match" mode
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ConditionOnExpression[] value();
    }
}
