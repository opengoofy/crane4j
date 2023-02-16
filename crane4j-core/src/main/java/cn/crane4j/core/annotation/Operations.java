package cn.crane4j.core.annotation;

import cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>用于在类上集中配置一组装配和拆卸操作。<br />
 * 一般用于不便直接在属性上添加{@link Assemble}和{@link Disassemble}时使用，
 * 比如在子类中声明基于父类属性的操作。
 *
 * @author huangchengxing
 * @see AnnotationAwareBeanOperationParser
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Operations {

    /**
     * 装配操作
     *
     * @return 装配操作
     */
    Assemble[] assembles() default {};

    /**
     * 拆卸操作
     *
     * @return 拆卸操作
     */
    Disassemble[] disassembles() default {};
}
