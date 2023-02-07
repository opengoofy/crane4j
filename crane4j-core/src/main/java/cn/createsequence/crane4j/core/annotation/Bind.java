package cn.createsequence.crane4j.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当{@link ContainerMethod}注解在类上时，通过当前注解指定要绑定的方法
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bind {

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
