package cn.crane4j.springboot.support;

import cn.crane4j.core.support.AnnotationFinder;
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
     * Get the specified annotation from the element.
     *
     * @param element element
     * @param annotationType annotation type
     * @param <A> annotation type
     * @return annotation
     */
    @Override
    public <A extends Annotation> A findAnnotation(@NonNull AnnotatedElement element, Class<A> annotationType) {
        return AnnotatedElementUtils.getMergedAnnotation(element, annotationType);
    }

    /**
     * Get all specified annotations from the element.
     *
     * @param element element
     * @param annotationType annotation type
     * @param <A> annotation type
     * @return annotations
     */
    @Override
    public <A extends Annotation> Set<A> findAllAnnotations(@NonNull AnnotatedElement element, Class<A> annotationType) {
        return AnnotatedElementUtils.getMergedRepeatableAnnotations(element, annotationType);
    }
}
