package cn.crane4j.core.annotation;

import cn.crane4j.core.container.ConstantContainer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示注解的枚举可以被转为指定类型的枚举容器
 *
 * @author huangchengxing
 * @see ConstantContainer#forEnum
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerEnum {

    /**
     * 数据源容器对应的命名空间，若为空则默认为该类的{@link Class#getSimpleName()}
     *
     * @return 命名空间
     */
    String namespace() default "";

    /**
     * 枚举的key字段，若不指定则默认为枚举{@link Enum#name()}
     *
     * @return 枚举的key字段
     */
    String key() default "";

    /**
     * 跟key值对应的value值，若不填则默认为枚举项本身
     *
     * @return value值
     */
    String value() default "";
}
