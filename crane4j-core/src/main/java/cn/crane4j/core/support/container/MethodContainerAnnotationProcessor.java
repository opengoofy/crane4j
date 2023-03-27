package cn.crane4j.core.support.container;

import cn.crane4j.annotation.Bind;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.AnnotationFinder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>An {@link ContainerMethod} annotation processor.
 * Scan methods annotated directly by {@link ContainerMethod}
 * in the class or methods bound by annotations on class,
 * and adapt it to {@link Container} instance
 * according to given {@link MethodContainerFactory}.
 *
 * @author huangchengxing
 * @see ContainerMethod
 */
public class MethodContainerAnnotationProcessor {

    /**
     * annotation finder
     */
    protected final AnnotationFinder annotationFinder;

    /**
     * method container factories
     */
    protected final Collection<MethodContainerFactory> methodContainerFactories;

    /**
     * non annotated classes
     */
    protected final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    /**
     * Create a {@link MethodContainerAnnotationProcessor} instance.
     *
     * @param annotationFinder annotation finder
     * @param methodContainerFactories method container factories
     */
    public MethodContainerAnnotationProcessor(
        AnnotationFinder annotationFinder, Collection<MethodContainerFactory> methodContainerFactories) {
        this.annotationFinder = annotationFinder;
        this.methodContainerFactories = methodContainerFactories.stream()
            .sorted(Comparator.comparing(MethodContainerFactory::getSort))
            .collect(Collectors.toList());
    }

    /**
     * process target
     *
     * @param target target
     * @param type type
     * @return containers
     */
    public final Collection<Container<Object>> process(Object target, Class<?> type) {
        if (nonAnnotatedClasses.contains(type)) {
            return Collections.emptyList();
        }
        Multimap<Method, ContainerMethod> annotatedMethods = HashMultimap.create();
        Method[] allMethods = collectMethodLevelAnnotatedMethods(type, annotatedMethods);
        collectClassLevelAnnotatedMethods(type, allMethods, annotatedMethods);
        if (annotatedMethods.isEmpty()) {
            nonAnnotatedClasses.add(type);
            return Collections.emptyList();
        }
        return processAnnotatedMethod(target, type, annotatedMethods);
    }

    /**
     * Collect methods bound by class level annotation {@link Bind}.
     *
     * @param type type
     * @param allMethods all methods in type
     * @param annotatedMethods annotated methods
     */
    protected void collectClassLevelAnnotatedMethods(
        Class<?> type, Method[] allMethods, Multimap<Method, ContainerMethod> annotatedMethods) {
        Map<String, List<Method>> methodGroup = Stream.of(allMethods).collect(Collectors.groupingBy(Method::getName));
        Collection<ContainerMethod> classLevelAnnotation = annotationFinder.findAllAnnotations(type, ContainerMethod.class);
        for (ContainerMethod annotation : classLevelAnnotation) {
            Method resolvedMethod = resolveMethodForClassLevelAnnotation(methodGroup, annotation);
            annotatedMethods.put(resolvedMethod, annotation);
        }
    }

    /**
     * Collect methods annotated with {@link ContainerMethod}.
     *
     * @param type type
     * @param annotatedMethods annotated methods
     * @return all checked methods
     */
    protected Method[] collectMethodLevelAnnotatedMethods(Class<?> type, Multimap<Method, ContainerMethod> annotatedMethods) {
        Method[] methods = ReflectUtil.getMethods(type);
        for (Method method : methods) {
            Collection<ContainerMethod> annotations = resolveMethodForMethodLevelAnnotation(method);
            if (annotations.isEmpty()) {
                continue;
            }
            annotatedMethods.putAll(method, annotations);
        }
        return methods;
    }

    /**
     * Check whether {@code method} is the method bound by the annotation.
     *
     * @param annotation class level annotation
     * @param method method to be checked
     * @return boolean
     */
    protected boolean checkMethodMatch(ContainerMethod annotation, Method method) {
        Bind bind = annotation.bind();
        Class<?>[] paramTypes = bind.paramTypes();
        return ArrayUtil.equals(method.getParameterTypes(), paramTypes);
    }

    /**
     * process annotated method.
     *
     * @param target target
     * @param type type
     * @param annotatedMethods annotated methods
     */
    protected Collection<Container<Object>> processAnnotatedMethod(Object target, Class<?> type, Multimap<Method, ContainerMethod> annotatedMethods) {
        return annotatedMethods.keys().stream()
            .map(method -> createMethodContainer(target, method))
            .filter(CollUtil::isNotEmpty)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private Collection<Container<Object>> createMethodContainer(Object bean, Method method) {
        return methodContainerFactories.stream()
            .filter(factory -> factory.support(bean, method))
            .findFirst()
            .map(factory -> factory.get(bean, method))
            .orElse(Collections.emptyList());
    }

    private Collection<ContainerMethod> resolveMethodForMethodLevelAnnotation(Method method) {
        return annotationFinder.findAllAnnotations(method, ContainerMethod.class);
    }

    private Method resolveMethodForClassLevelAnnotation(Map<String, List<Method>> methodGroup, ContainerMethod annotation) {
        Bind bind = annotation.bind();
        String methodName = CharSequenceUtil.emptyToDefault(bind.value(), annotation.namespace());
        return methodGroup.getOrDefault(methodName, Collections.emptyList()).stream()
            .filter(method -> checkMethodMatch(annotation, method))
            .findFirst()
            .orElseThrow(() -> new Crane4jException("method cannot be bind to annotation: [{}]", bind));
    }

    @Getter
    @RequiredArgsConstructor
    protected static class ProcessContext<T extends Annotation> {
        private final Object target;
        private final Class<?> type;
        private final Set<T> classLevelAnnotations = new LinkedHashSet<>();
        private final Multimap<Method, T> annotatedMethods = HashMultimap.create();
    }
}
