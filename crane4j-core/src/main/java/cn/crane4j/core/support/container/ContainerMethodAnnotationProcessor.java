package cn.crane4j.core.support.container;

import cn.crane4j.annotation.Bind;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.ArrayUtils;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.MultiMap;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>An {@link ContainerMethod} annotation lifecycle.
 * Scan methods annotated directly by {@link ContainerMethod}
 * in the class or methods bound by annotations on class,
 * and adapt it to {@link Container} comparator according to given {@link MethodContainerFactory}.
 *
 * @author huangchengxing
 * @see ContainerMethod
 * @see MethodContainerFactory
 */
@Slf4j
public class ContainerMethodAnnotationProcessor {

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
     * Create a {@link ContainerMethodAnnotationProcessor} comparator.
     *
     * @param methodContainerFactories method container factories
     * @param annotationFinder annotation finder
     */
    public ContainerMethodAnnotationProcessor(
        Collection<MethodContainerFactory> methodContainerFactories, AnnotationFinder annotationFinder) {
        this.methodContainerFactories = methodContainerFactories.stream()
            .sorted(Comparator.comparing(MethodContainerFactory::getSort))
            .collect(Collectors.toList());
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
     * Collect methods bound by class level annotation {@link Bind}.
     *
     * @param type type
     * @param allMethods all methods in type
     * @param annotatedMethods annotated methods
     */
    protected void collectClassLevelAnnotatedMethods(
        Class<?> type, Method[] allMethods, MultiMap<Method, ContainerMethod> annotatedMethods) {
        Map<String, List<Method>> methodGroup = Stream.of(allMethods).collect(Collectors.groupingBy(Method::getName));
        Collection<ContainerMethod> classLevelAnnotation = resolveAnnotationsForClass(type);
        for (ContainerMethod annotation : classLevelAnnotation) {
            Method resolvedMethod = resolveMethodForClassLevelAnnotation(methodGroup, annotation);
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
     * Check whether {@code method} is the method bound by the annotation.
     *
     * @param annotation class level annotation
     * @param method method to be checked
     * @return boolean
     */
    protected boolean checkMethodMatch(ContainerMethod annotation, Method method) {
        Bind bind = annotation.bind();
        return Objects.equals(method.getName(), bind.value())
            && ArrayUtils.isEquals(method.getParameterTypes(), bind.paramTypes());
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

    private Collection<Container<Object>> createMethodContainer(
            Object bean, Method method, Collection<ContainerMethod> annotations) {
        return methodContainerFactories.stream()
            .filter(factory -> factory.support(bean, method, annotations))
            .findFirst()
            .map(factory -> factory.get(bean, method, annotations))
            .orElse(Collections.emptyList());
    }

    @Nullable
    private Method resolveMethodForClassLevelAnnotation(Map<String, List<Method>> methodGroup, ContainerMethod annotation) {
        Bind bind = annotation.bind();
        String methodName = StringUtils.emptyToDefault(bind.value(), annotation.namespace());
        Method resolvedMethod = methodGroup.getOrDefault(methodName, Collections.emptyList()).stream()
            .filter(method -> checkMethodMatch(annotation, method))
            .findFirst()
            .orElse(null);
        if (Objects.isNull(resolvedMethod)) {
            log.debug("bound method not found: [{}]", bind);
            return null;
        }
        return resolvedMethod;
    }
}
