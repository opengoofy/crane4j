package cn.crane4j.core.support;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Set;

/**
 * 注解查找器
 *
 * @author huangchengxing
 * @see SimpleAnnotationFinder
 */
public interface AnnotationFinder {

    /**
     * 默认的注解属性值
     */
    String VALUE = "value";

    /**
     * 从元素上获得指定注解
     *
     * @param element 要查找的元素
     * @param annotationType 注解类型
     * @param <A> 注解类型
     * @return 注解对象
     */
    <A extends Annotation> A findAnnotation(@Nonnull AnnotatedElement element, Class<A> annotationType);

    /**
     * 元素上是否存在指定注解
     *
     * @param element 要查找的元素
     * @param annotationType 注解类型
     * @return 是否
     */
    default boolean hasAnnotation(@Nonnull AnnotatedElement element, Class<? extends Annotation> annotationType) {
        return Objects.nonNull(findAnnotation(element, annotationType));
    }
    
    /**
     * 从元素上获得所有指定注解
     *
     * @param element 要查找的元素
     * @param annotationType 注解类型
     * @param <A> 注解类型
     * @return 注解对象
     */
    <A extends Annotation> Set<A> findAllAnnotations(@Nonnull AnnotatedElement element, Class<A> annotationType);

}
