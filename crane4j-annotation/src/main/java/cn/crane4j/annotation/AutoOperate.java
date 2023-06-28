package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Declare the return value or parameter of the method(if the method is annotated by {@link ArgAutoOperate}) need process.
 *
 * @author huangchengxing
 * @see ArgAutoOperate
 * @see cn.crane4j.core.support.aop.MethodResultAutoOperateSupport
 * @see cn.crane4j.core.support.aop.MethodArgumentAutoOperateSupport
 * @see cn.crane4j.core.support.aop.AutoOperateAnnotatedElementResolver
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoOperate {

    /**
     * When used in {@link ArgAutoOperate}, it is used to bind the corresponding parameter name.
     *
     * @return parameter name
     */
    String value() default "";

    /**
     * <p>The object type to be processed in the return value of the method.<br />
     * It will be parsed by the parser specified in {@link #parser()} when it is first executed,
     * and then the operation configuration will be obtained.
     *
     * <p>If the object is generic or cannot be confirmed, the default value is {@link Object},
     * and the type will be automatically inferred.
     *
     * @return type to be processed
     * @see cn.crane4j.core.support.TypeResolver
     */
    Class<?> type() default Object.class;

    /**
     * <p>When the return value is a wrapper class,
     * we can specify to obtain the data set to be processed
     * from the specific field of the wrapper class, and then process it。<br />
     *
     * <p>This configuration is generally used to process the method
     * of returning the general response body in the Controller。<br />
     * For example:
     * <pre type="code">{@code
     * // general response
     * public static class Result<T> {
     *     private Integer code;
     *     private T data; // objects to be processed
     * }
     * // process general response
     * @AutoOperate(type = Foo.class, on = "data")
     * public Result<Foo> requestFoo() { // do something }
     * }</pre>
     * The return value of the method is<i>Result</i>, but the data to be filled is in <i>Result.data</i>,
     * obtain data from specific fields for filling by <i>on</i>.
     *
     * @return field name
     */
    String on() default "";

    /**
     * The name of the executor to be used.
     *
     * @return executor name
     * @see cn.crane4j.core.executor.BeanOperationExecutor
     */
    String executor() default "DisorderedBeanOperationExecutor";

    /**
     * The name of the operation parser to be used.
     *
     * @return parser name
     * @see cn.crane4j.core.parser.BeanOperationParser
     */
    String parser() default "TypeHierarchyBeanOperationParser";

    /**
     * The group of operations to be performed. <br />
     * If it is blank, all operations will be performed by default.
     *
     * @return group names
     */
    String[] includes() default {};

    /**
     * <p>The group of operations not to be performed.<br />
     * The priority of this configuration is higher than {@link #includes()}
     *
     * @return group names
     */
    String[] excludes() default {};

    /**
     * <p>The expression of apply condition.
     * The operation will only be performed when the expression result is {@code true} or "true" string。
     *
     * <p>The following variables can be used by default in the expression：
     * <ul>
     *     <li>{@code @beanName}：beans in spring context；</li>
     *     <li>{@code #parameterName}: arguments of method；</li>
     *     <li>{@code #result}: return value of method；</li>
     * </ul>
     *
     * @return expression of apply condition
     */
    String condition() default "";
}
