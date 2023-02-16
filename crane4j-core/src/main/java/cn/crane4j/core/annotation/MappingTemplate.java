package cn.crane4j.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>注解在类上用于声明一组字段映射配置，
 * 在{@link Assemble#propTemplates()}通过指定被注解的类引入。<br />
 * 该注解适用于一次装配涉及的字段过多的场景，避免单个{@link Assemble}过于膨胀。
 * 推荐在常量接口/抽象类中以内部类的形式统一的管理。
 *
 * @author huangchengxing
 * @see Assemble#propTemplates()
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingTemplate {

    /**
     * 需要映射字段
     *
     * @return 字段映射配置
     */
    Mapping[] value() default {};
}
