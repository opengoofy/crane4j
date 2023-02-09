package cn.createsequence.crane4j.springboot.support;

import cn.createsequence.crane4j.core.support.AnnotationFinder;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

/**
 * 支持spring元注解/合成注解机制的注解查找器
 *
 * @author huangchengxing
 * @see AnnotatedElementUtils
 */
public class MergedAnnotationFinder implements AnnotationFinder {

    /**
     * 从元素上获得指定注解
     *
     * @param element        要查找的元素
     * @param annotationType 注解类型
     * @return 注解对象
     */
    @Override
    public <A extends Annotation> A findAnnotation(@NonNull AnnotatedElement element, Class<A> annotationType) {
        return AnnotatedElementUtils.getMergedAnnotation(element, annotationType);
    }

    /**
     * 从元素上获得所有指定注解
     *
     * @param element        要查找的元素
     * @param annotationType 注解类型
     * @return 注解对象
     */
    @Override
    public <A extends Annotation> Set<A> findAllAnnotations(@NonNull AnnotatedElement element, Class<A> annotationType) {
        return AnnotatedElementUtils.getMergedRepeatableAnnotations(element, annotationType);
    }
}
