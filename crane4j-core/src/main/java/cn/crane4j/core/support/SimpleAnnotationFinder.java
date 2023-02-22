package cn.crane4j.core.support;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple implementation of {@link AnnotationFinder}.
 *
 * @author huangchengxing
 */
public class SimpleAnnotationFinder implements AnnotationFinder {

    /**
     * Get the specified annotation from the element.
     *
     * @param element element
     * @param annotationType annotation type
     * @param <A> annotation type
     * @return annotation
     */
    @Override
    public <A extends Annotation> A findAnnotation(@Nonnull AnnotatedElement element, Class<A> annotationType) {
        return element.getAnnotation(annotationType);
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
    public <A extends Annotation> Set<A> findAllAnnotations(@Nonnull AnnotatedElement element, Class<A> annotationType) {
        return Stream.of(element.getAnnotationsByType(annotationType))
            .collect(Collectors.toSet());
    }
}
