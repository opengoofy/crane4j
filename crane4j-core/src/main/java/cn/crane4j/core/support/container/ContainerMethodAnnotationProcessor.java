package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.MultiMap;
import cn.crane4j.core.util.ReflectUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>An {@link ContainerMethod} annotation processor.
 * Scan methods annotated directly by {@link ContainerMethod}
 * in the class or methods bound by annotations on class,
 * and adapt it to {@link Container} instance according to given {@link MethodContainerFactory}.
 *
 * @author huangchengxing
 * @see ContainerMethod
 * @see MethodContainerFactory
 */
@Slf4j
public class ContainerMethodAnnotationProcessor extends ContainerMethodSupport {

    /**
     * annotation finder
     */
    protected final AnnotationFinder annotationFinder;

    /**
     * non annotated classes
     */
    protected final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    /**
     * Create a {@link ContainerMethodAnnotationProcessor} instance.
     *
     * @param methodContainerFactories method container factories
     * @param annotationFinder annotation finder
     */
    public ContainerMethodAnnotationProcessor(
        Collection<MethodContainerFactory> methodContainerFactories, AnnotationFinder annotationFinder) {
        super(methodContainerFactories);
        this.annotationFinder = annotationFinder;
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
        MultiMap<Method, ContainerMethod> annotatedMethods = MultiMap.linkedHashMultimap();
        Method[] allMethods = collectMethodLevelAnnotatedMethods(type, annotatedMethods);
        collectClassLevelAnnotatedMethods(type, allMethods, annotatedMethods);
        if (annotatedMethods.isEmpty()) {
            nonAnnotatedClasses.add(type);
            return Collections.emptyList();
        }
        return processAnnotatedMethod(target, annotatedMethods);
    }

    /**
     * Collect methods bound by class level annotation.
     *
     * @param type type
     * @param methods all methods in type
     * @param annotatedMethods annotated methods
     */
    protected void collectClassLevelAnnotatedMethods(
        Class<?> type, Method[] methods, MultiMap<Method, ContainerMethod> annotatedMethods) {
        Collection<ContainerMethod> classLevelAnnotation = resolveAnnotationsForClass(type);
        for (ContainerMethod annotation : classLevelAnnotation) {
            Method resolvedMethod = findMatchedMethodForAnnotation(Arrays.asList(methods), annotation);
            if (Objects.nonNull(resolvedMethod)) {
                annotatedMethods.put(resolvedMethod, annotation);
            }
        }
    }

    /**
     * Collect methods annotated with {@link ContainerMethod}.
     *
     * @param type type
     * @param annotatedMethods annotated methods
     * @return all checked methods
     */
    protected Method[] collectMethodLevelAnnotatedMethods(Class<?> type, MultiMap<Method, ContainerMethod> annotatedMethods) {
        Method[] methods = ReflectUtils.getMethods(type);
        for (Method method : methods) {
            Collection<ContainerMethod> annotations = resolveAnnotationsForMethod(method);
            if (annotations.isEmpty()) {
                continue;
            }
            annotatedMethods.putAll(method, annotations);
        }
        return methods;
    }

    /**
     * process annotated method.
     *
     * @param target target
     * @param annotatedMethods annotated methods
     */
    protected Collection<Container<Object>> processAnnotatedMethod(
            Object target, MultiMap<Method, ContainerMethod> annotatedMethods) {
        return annotatedMethods.asMap().entrySet().stream()
            .map(e -> createMethodContainer(target, e.getKey(), e.getValue()))
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Resolve annotations for class.
     *
     * @param type type
     * @return annotations
     */
    protected Collection<ContainerMethod> resolveAnnotationsForClass(Class<?> type) {
        return annotationFinder.findAllAnnotations(type, ContainerMethod.class);
    }

    /**
     * Resolve annotations for class.
     *
     * @param method method
     * @return annotations
     */
    protected Collection<ContainerMethod> resolveAnnotationsForMethod(Method method) {
        return annotationFinder.getAllAnnotations(method, ContainerMethod.class);
    }
}
