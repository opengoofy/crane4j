package cn.crane4j.core.support;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Set;

/**
 * Annotation finder.
 *
 * @author huangchengxing
 * @see SimpleAnnotationFinder
 */
public interface AnnotationFinder {

    /**
     * default value name
     */
    String VALUE = "value";

    /**
     * Get the specified annotation from the element.
     *
     * @param element element
     * @param annotationType annotation type
     * @param <A> annotation type
     * @return annotation
     */
    <A extends Annotation> A findAnnotation(@Nonnull AnnotatedElement element, Class<A> annotationType);

    /**
     * Whether the specified annotation exists on the element.
     *
     * @param element element
     * @param annotationType annotation type
     * @return true if exists, false otherwise
     */
    default boolean hasAnnotation(@Nonnull AnnotatedElement element, Class<? extends Annotation> annotationType) {
        return Objects.nonNull(findAnnotation(element, annotationType));
    }

    /**
     * Get all specified annotations from the element.
     *
     * @param element element
     * @param annotationType annotation type
     * @param <A> annotation type
     * @return annotations
     */
    <A extends Annotation> Set<A> findAllAnnotations(@Nonnull AnnotatedElement element, Class<A> annotationType);
}
