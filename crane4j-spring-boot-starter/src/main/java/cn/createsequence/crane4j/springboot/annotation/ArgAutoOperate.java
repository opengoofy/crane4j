package cn.createsequence.crane4j.springboot.annotation;

import cn.createsequence.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;

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
 * 支持同时存在上述两种写法。
 *
 * @author huangchengxing
 * @see AutoOperate
 * @see MethodArgumentAutoOperateAspect
 */
public @interface ArgAutoOperate {

    /**
     * 参数的操作配置
     *
     * @return 参数的操作配置
     */
    AutoOperate[] value() default {};
}
