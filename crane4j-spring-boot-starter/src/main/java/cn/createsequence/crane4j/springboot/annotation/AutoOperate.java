package cn.createsequence.crane4j.springboot.annotation;

import cn.createsequence.crane4j.core.executor.BeanOperationExecutor;
import cn.createsequence.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.createsequence.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.createsequence.crane4j.core.parser.BeanOperationParser;
import cn.createsequence.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.createsequence.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * <p>声明方法的返回值或者参数(若方法被{@link ArgAutoOperate}注解)需要自动进行填充处理，
 * 注解的方法将会根据配置，在切面中对返回值进行拆卸和装配操作。<br />
 * 该功能支持处理{@link Collection}集合、数组以及单个对象。
 *
 * @author huangchengxing
 * @see ArgAutoOperate
 * @see MethodResultAutoOperateAspect
 * @see MethodArgumentAutoOperateAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoOperate {

    /**
     * 当用于{@link ArgAutoOperate}中时，用于绑定对应的参数名
     *
     * @return java.lang.String
     */
    String value() default "";

    /**
     * <p>方法返回值中需要处理的对象类型，首次执行时，
     * 将会使用{@link #parser()}指定的解析器对该类型进行解析，然后得到操作配置。
     *
     * @return 类型
     */
    Class<?> type();

    /**
     * <p>当返回值为一个包装类时，可以指定从包装类的特定字段获得待处理的数据集，然后再进行处理。<br />
     * 该配置一般用于处理Controller中返回通用响应体的方法。
     *
     * <p>比如：
     * <pre type="code">{@code
     * // 方法返回的响应体
     * public static class Result<T> {
     *     private Integer code;
     *     private T data; // 待处理的对象
     * }
     * // 返回响应体的方法
     * @AutoOperate(type = Foo.class, on = "data")
     * public Result<Foo> requestFoo() { // do something }
     * }</pre>
     * 方法的返回值为<i>Result</i>，但是待填充的数据在<i>Result.data</i>中，
     * 此时通过<i>on</i>指定从特定字段获取数据进行填充。
     *
     * @return 字段名
     */
    String on() default "";

    /**
     * 用于完成操作的执行器
     *
     * @return 执行器类型
     */
    Class<? extends BeanOperationExecutor> executor() default DisorderedBeanOperationExecutor.class;

    /**
     * 用于解析返回对象类型的操作解析器
     *
     * @return 解析器类型
     */
    Class<? extends BeanOperationParser> parser() default AnnotationAwareBeanOperationParser.class;

    /**
     * 待执行操作的组别，为空时默认全部执行
     *
     * @return 待执行操作的组别
     */
    String[] includes() default {};

    /**
     * 不执行的操作的组别，该配置的优先级要高于{@link #includes()}
     *
     * @return 不执行的操作的组别
     */
    String[] excludes() default {};

    /**
     * <p>应用条件，支持SpEL表达式，仅当表达式结果为{@code true}或"true"字符串时才会执行操作。
     *
     * <p>在表达式中默认执行下述操作：
     * <ul>
     *     <li>通过{@code @bean}的方式引用容器中的bean；</li>
     *     <li>通过{@code #参数名}的方式引用方法入参；</li>
     *     <li>通过{@code #result}的方式引用方法返回值；</li>
     * </ul>
     * 可以在{@link MethodResultAutoOperateAspect 切面}的构造函数中指定上下文工厂以便支持更多功能。
     *
     * @return 应用条件
     * @see MethodResultAutoOperateAspect#methodBaseExpressionEvaluator
     */
    String condition() default "";
}
