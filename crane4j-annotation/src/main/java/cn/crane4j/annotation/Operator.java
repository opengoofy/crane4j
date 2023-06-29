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
 * @see cn.crane4j.core.support.operator.OperatorProxyFactory
 * @see cn.crane4j.core.support.operator.OperatorProxyMethodFactory
 * @since 1.3.0
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Operator {

    /**
     * The name of the executor to be used.
     *
     * @return executor name
     * @see cn.crane4j.core.executor.BeanOperationExecutor
     */
    String executor() default "";

    /**
     * The type of the executor to be used.
     *
     * @return executor name
     * @see cn.crane4j.core.executor.BeanOperationExecutor
     */
    Class<?> executorType() default Object.class;

    /**
     * The name of the operation parser to be used.
     *
     * @return parser name
     * @see cn.crane4j.core.parser.BeanOperationParser
     */
    String parser() default "";

    /**
     * The type of the operation parser to be used.
     *
     * @return parser name
     * @see cn.crane4j.core.parser.BeanOperationParser
     */
    Class<?> parserType() default Object.class;
}
