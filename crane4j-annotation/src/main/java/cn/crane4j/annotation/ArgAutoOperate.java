package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Declaration requires automatic processing of method input parameters, for example：
 * <pre class="code">
 * &#64;ArgAutoOperate({
 *     &#64;AutoOperate(value = "list", type = A.class),
 *     &#64;AutoOperate(value = "b", type = B.class)
 * })
 * public void doSomething(List<A> list, B b) {
 *     // do something
 * }
 * </pre>
 * or:
 * <pre class="code">
 * &#64;ArgAutoOperate
 * public void doSomething(
 *     &#64;AutoOperate(value = "list", type = A.class) List<A> list,
 *     &#64;AutoOperate(value = "b", type = B.class) B b) {
 *     // do something
 * }
 * </pre>
 * <p>NOTE：The annotation configuration on the parameter
 * takes precedence over the annotation configuration on the method.
 *
 * @author huangchengxing
 * @see AutoOperate
 * @see cn.crane4j.core.support.aop.MethodArgumentAutoOperateSupport
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ArgAutoOperate {

    /**
     * Operation configuration of parameters.
     *
     * @return configuration of parameters
     */
    AutoOperate[] value() default {};
}
