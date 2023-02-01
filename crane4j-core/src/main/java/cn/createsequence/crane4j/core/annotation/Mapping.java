package cn.createsequence.crane4j.core.annotation;

import cn.createsequence.crane4j.core.parser.PropertyMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段映射配置，描述将哪个数据对象的属性映射到哪个目标对象的属性中
 *
 * @author huangchengxing
 * @see Assemble#props()
 * @see MappingTemplate
 * @see PropertyMapping
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {

    /**
     * 要获取的数据源对象属性
     *
     * @return 数据源对象属性
     */
    String src() default "";

    /**
     * 要设置的目标对象属性，如果为空则默认为key字段
     *
     * @return 目标对象属性
     */
    String ref() default "";
}
