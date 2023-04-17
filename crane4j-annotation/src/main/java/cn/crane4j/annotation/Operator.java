package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker an interface as an operator.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.support.OperatorProxyFactory
 * @since 1.3.0
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Operator {

    /**
     * The type of the operation executor to be used.
     *
     * @return executor type
     */
    Class<?> executor() default Object.class;

    /**
     * The name of the executor to be used.
     *
     * @return executor name
     */
    String executorName() default "";

    /**
     * The type of the operation parser to be used.
     *
     * @return parser type
     */
    Class<?> parser() default Object.class;

    /**
     * The name of the operation parser to be used.
     *
     * @return parser name
     */
    String parserName() default "";
}
