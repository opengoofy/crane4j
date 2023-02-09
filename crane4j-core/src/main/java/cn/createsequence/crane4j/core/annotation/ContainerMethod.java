package cn.createsequence.crane4j.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

/**
 * <p>表示注解的方法可以被转为指定类型的枚举容器。
 * <ul>
 *     <li>方法可以为实例方法或静态方法；</li>
 *     <li>方法可以是无参方法或有参数方法；</li>
 *     <li>
 *         方法必须有返回值，且：
 *         <ol>
 *             <li>若{@link #type()}为{@link MappingType#MAPPED}，则返回值必须为{@link Map}；</li>
 *             <li>若{@link #type()}不为{@link MappingType#MAPPED}，则返回值可以是单个对象、数组或{@link Collection}集合；</li>
 *         </ol>
 *     </li>
 * </ul>
 *
 * <p>比如，下述例子描述了如何将<i>requestFoo</i>方法适配为一个数据源容器，：
 * <pre type="code">{@code
 * // 返回响应体的方法
 * @ContainerMethod(
 *     namespace = "foo",
 *     resultType = Foo.class, resultKey = "id"
 * )
 * public List<Foo> requestFoo(Set<Integer> ids) { // do something }
 * }</pre>
 * 该容器命名空间为<i>foo</i>，当执行时会使用对应的id集合从该方法中获得<i>Foo</i>对象集合，
 * 然后将其按<i>foo.id</i>分组后进行字段映射。
 *
 * @author huangchengxing
 */
@Repeatable(ContainerMethod.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerMethod {

    /**
     * 数据源容器的命名空间，若为空则为方法的名称
     *
     * @return java.lang.String
     */
    String namespace() default "";

    /**
     * 指定方法返回值类型
     *
     * @return 返回值类型
     */
    MappingType type() default MappingType.ONE_TO_ONE;

    /**
     * 方法返回的数据源对象的key字段
     *
     * @return 方法返回的数据源对象的key字段
     */
    String resultKey() default "id";

    /**
     * 方法返回的数据源对象类型
     *
     * @return 方法返回的数据源对象类型
     */
    Class<?> resultType();

    /**
     * 仅当方法注解在类上时使用，用于绑定类中对应的方法
     *
     * @return 要查找的方法
     */
    Bound bind() default @Bound("");

    /**
     * 当{@link ContainerMethod}注解在类上时，通过当前注解指定要绑定的方法
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Bound {

        /**
         * 方法名称
         *
         * @return 方法名称
         */
        String value();

        /**
         * 方法参数类型
         *
         * @return 方法参数类型
         */
        Class<?>[] paramTypes() default {};
    }

    /**
     * 批量操作
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ContainerMethod[] value() default {};
    }
}
