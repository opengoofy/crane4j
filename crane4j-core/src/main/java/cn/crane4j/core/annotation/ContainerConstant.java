package cn.crane4j.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表明一个类中的成员变量可作为容器
 *
 * @author huangchengxing
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerConstant {

    /**
     * 是否仅处理公共的属性
     *
     * @return 是否
     */
    boolean onlyPublic() default true;

    /**
     * 仅处理被{@link ContainerConstant.Include}注解的属性
     *
     * @return 是否
     */
    boolean onlyExplicitlyIncluded() default false;

    /**
     * 数据源容器对应的命名空间，若为空则默认为所属类{@link Class#getSimpleName()}。
     *
     * @return 命名空间
     */
    String namespace() default "";

    /**
     * 为属性指定key名称
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Name {
        String value();
    }

    /**
     * 包含特定属性
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Include {}

    /**
     * 包含特定属性
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Exclude {}
}
