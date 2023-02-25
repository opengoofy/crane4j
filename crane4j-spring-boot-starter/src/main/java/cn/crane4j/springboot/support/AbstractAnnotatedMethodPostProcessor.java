package cn.crane4j.springboot.support;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A general {@link BeanPostProcessor} implementation for
 * process annotated methods in the post-processing stage of the bean.
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAnnotatedMethodPostProcessor<T extends Annotation>
    implements BeanPostProcessor, DisposableBean {

    /**
     * non annotated classes
     */
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    /**
     * annotation type
     */
    protected final Class<T> annotationType;

    /**
     * Clear resources when destroying beans
     */
    @Override
    public void destroy() {
        nonAnnotatedClasses.clear();
    }

    /**
     * Do nothing.
     *
     * @param bean     bean
     * @param beanName beanName
     * @return bean
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * <p>Scan and process the method with the specified annotation in the class.
     * If the annotation also exists in the class, find and process the corresponding method in the class.
     *
     * @param bean     bean
     * @param beanName beanName
     * @return bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanType = AopUtils.getTargetClass(bean);
        if (nonAnnotatedClasses.contains(beanType)) {
            return bean;
        }
        Multimap<Method, T> annotatedMethods = HashMultimap.create();
        resolveClassLevelAnnotations(beanType, annotatedMethods);
        resolveMethodLevelAnnotations(beanType, annotatedMethods);
        if (annotatedMethods.isEmpty()) {
            nonAnnotatedClasses.add(beanType);
            return bean;
        }
        log.debug("process [{}] annotated methods for bean [{}]", annotatedMethods.size(), beanName);
        processAnnotatedMethods(bean, beanType, annotatedMethods);
        return bean;
    }

    /**
     * Process annotated methods.
     *
     * @param bean bean
     * @param beanType bean type
     * @param annotatedMethods annotated methods
     */
    protected abstract void processAnnotatedMethods(
        Object bean, Class<?> beanType, Multimap<Method, T> annotatedMethods);

    /**
     * Find methods by class level annotations.
     *
     * @param beanType bean type
     * @param classLevelAnnotation class level annotation
     * @return method corresponding to annotation
     */
    @Nullable
    protected abstract Method findMethodForAnnotation(Class<?> beanType, T classLevelAnnotation);

    private void resolveMethodLevelAnnotations(Class<?> beanType, Multimap<Method, T> collectedMethods) {
        Map<Method, Set<T>> methodLevelAnnotations = MethodIntrospector.selectMethods(
            beanType, (MethodIntrospector.MetadataLookup<Set<T>>) method -> {
                Set<T> annotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(method, annotationType);
                return annotations.isEmpty() ? null : annotations;
            }
        );
        if (CollUtil.isNotEmpty(methodLevelAnnotations)) {
            methodLevelAnnotations.forEach(collectedMethods::putAll);
        }
    }

    private void resolveClassLevelAnnotations(
        Class<?> beanType, Multimap<Method, T> collectedMethods) {
        Set<T> classLevelAnnotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(beanType, annotationType);
        classLevelAnnotations.forEach(annotation -> {
            Method method = findMethodForAnnotation(beanType, annotation);
            if (Objects.nonNull(method)) {
                collectedMethods.put(method, annotation);
            }
        });
    }
}
