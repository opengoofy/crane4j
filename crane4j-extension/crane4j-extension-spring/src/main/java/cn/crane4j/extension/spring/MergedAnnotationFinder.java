package cn.crane4j.extension.spring;

import cn.crane4j.core.support.AnnotationFinder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

/**
 * Annotation finder supporting spring meta-annotation composition annotation mechanism.
 *
 * @author huangchengxing
 * @see AnnotatedElementUtils
 * @see MergedAnnotation
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
    @Nullable
    @Override
    public <A extends Annotation> A getAnnotation(@NonNull AnnotatedElement element, Class<A> annotationType) {
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
    public <A extends Annotation> Set<A> getAllAnnotations(@NonNull AnnotatedElement element, Class<A> annotationType) {
        return AnnotatedElementUtils.getMergedRepeatableAnnotations(element, annotationType);
    }

    /**
     * Get the specified annotation from the element.
     *
     * @param element        element
     * @param annotationType annotation type
     * @return annotation
     */
    @Nullable
    @Override
    public <A extends Annotation> A findAnnotation(@NonNull AnnotatedElement element, Class<A> annotationType) {
        return AnnotatedElementUtils.findMergedAnnotation(element, annotationType);
    }

    /**
     * Get all specified annotations from the element.
     *
     * @param element        element
     * @param annotationType annotation type
     * @return annotations
     */
    @Override
    public <A extends Annotation> Set<A> findAllAnnotations(@NonNull AnnotatedElement element, Class<A> annotationType) {
        return AnnotatedElementUtils.findMergedRepeatableAnnotations(element, annotationType);
    }
}
