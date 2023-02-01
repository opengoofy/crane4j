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
