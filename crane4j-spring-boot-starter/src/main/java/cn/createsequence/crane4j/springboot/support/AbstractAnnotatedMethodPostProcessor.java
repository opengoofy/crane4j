package cn.createsequence.crane4j.springboot.support;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
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
 * 用于在bean的后处理阶段，处理带有特定注解方法的通用后处理器
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public abstract class AbstractAnnotatedMethodPostProcessor<T extends Annotation>
    implements BeanPostProcessor, DisposableBean {

    /**
     * 无需处理的类型
     */
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    /**
     * 要处理的注解类型
     */
    protected final Class<T> annotationType;

    /**
     * 销毁Bean时清空资源
     */
    @Override
    public void destroy() {
        nonAnnotatedClasses.clear();
    }

    /**
     * 不做任何操作
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
     * 扫描类中带有指定注解的方法，若类上也存在该注解，则一并查找与其对应的类中方法，并进行处理
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
        processAnnotatedMethods(bean, beanType, annotatedMethods);
        return bean;
    }

    /**
     * 将被注解的方法适配为
     *
     * @param bean 目标对象
     * @param beanType 目标类型
     * @param annotatedMethods 被注解的方法
     */
    protected abstract void processAnnotatedMethods(
        Object bean, Class<?> beanType, Multimap<Method, T> annotatedMethods);

    /**
     * 根据类上注解，查找该注解对应的类中方法
     *
     * @param beanType 目标类型
     * @param classLevelAnnotation 类上的注解
     * @return 与注解对应的方法，若不存在则为{@code null}
     */
    @Nullable
    protected Method findMethodForAnnotation(Class<?> beanType, T classLevelAnnotation) {
        return null;
    }

    private void resolveMethodLevelAnnotations(Class<?> beanType, Multimap<Method, T> collectedMethods) {
        Map<Method, Set<T>> methodLevelAnnotations = MethodIntrospector.selectMethods(
            beanType, (MethodIntrospector.MetadataLookup<Set<T>>) method ->
            AnnotatedElementUtils.findAllMergedAnnotations(method, annotationType)
        );
        if (CollUtil.isNotEmpty(methodLevelAnnotations)) {
            methodLevelAnnotations.forEach(collectedMethods::putAll);
        }
    }

    private void resolveClassLevelAnnotations(
        Class<?> beanType, Multimap<Method, T> collectedMethods) {
        Set<T> classLevelAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(beanType, annotationType);
        classLevelAnnotations.forEach(annotation -> {
            Method method = findMethodForAnnotation(beanType, annotation);
            if (Objects.nonNull(method)) {
                collectedMethods.put(method, annotation);
            }
        });
    }
}
