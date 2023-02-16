package cn.createsequence.crane4j.springboot.annotation;

import cn.createsequence.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>声明需要对对方法入参进行自动处理，比如：
 * <pre class="code">
 * &#64;ArgAutoOperate({
 *     &#64;AutoOperate(value = "list", type = A.class),
 *     &#64;AutoOperate(value = "b", type = B.class)
 * })
 * public void doSomething(List<A> list, B b) {
 *     // do something
 * }
 * </pre>
 * 或者：
 * <pre class="code">
 * &#64;ArgAutoOperate
 * public void doSomething(
 *     &#64;AutoOperate(value = "list", type = A.class) List<A> list,
 *     &#64;AutoOperate(value = "b", type = B.class) B b) {
 *     // do something
 * }
 * </pre>
 * 参数上的注解配置优先级大于方法上的注解配置。
 *
 * @author huangchengxing
 * @see AutoOperate
 * @see MethodArgumentAutoOperateAspect
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ArgAutoOperate {

    /**
     * 参数的操作配置
     *
     * @return 参数的操作配置
     */
    AutoOperate[] value() default {};
}
