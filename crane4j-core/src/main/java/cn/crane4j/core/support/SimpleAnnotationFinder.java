package cn.crane4j.core.support;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link AnnotationFinder}的简单实现。
 *
 * @author huangchengxing
 */
public class SimpleAnnotationFinder implements AnnotationFinder {

    /**
     * 从元素上获得指定注解
     *
     * @param element 要查找的元素
     * @param annotationType 注解类型
     * @param <A> 注解类型
     * @return 注解对象
     */
    @Override
    public <A extends Annotation> A findAnnotation(@Nonnull AnnotatedElement element, Class<A> annotationType) {
        return element.getAnnotation(annotationType);
    }

    /**
     * 从元素上获得所有指定注解
     *
     * @param element 要查找的元素
     * @param annotationType 注解类型
     * @param <A> 注解类型
     * @return 注解对象
     */
    @Override
    public <A extends Annotation> Set<A> findAllAnnotations(@Nonnull AnnotatedElement element, Class<A> annotationType) {
        return Stream.of(element.getAnnotationsByType(annotationType))
            .collect(Collectors.toSet());
    }
}
