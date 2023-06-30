package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.MultiMap;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
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
     * Collect methods bound by class level annotation.
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
        String methodName = StringUtils.emptyToDefault(annotation.bindMethod(), annotation.namespace());
        List<Method> candidates = methodGroup.get(methodName);
        if (CollectionUtils.isEmpty(candidates)) {
            log.debug("bound method not found: [{}] ({})", annotation.bindMethod(), Arrays.asList(annotation.bindMethodParamTypes()));
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        Method matched = findMostMatchMethod(candidates, annotation.bindMethodParamTypes());
        if (Objects.nonNull(matched)) {
            return matched;
        }
        if (log.isDebugEnabled()) {
            log.debug("bound method not found: [{}] ({})", annotation.bindMethod(), Arrays.asList(annotation.bindMethodParamTypes()));
        }
        return null;
    }

    /**
     * <p>Find most matched method by given method name and param types,
     * the method with the most matched parameter types will be returned.<br/>
     * If there are multiple methods with the same number of matched parameter types or no matched method,
     * the first one will be returned.
     *
     * @param candidates methods which have the same name, list size must be greater than 0
     * @param expectedTypes param types
     * @return most matched method
     */
    @Nullable
    protected Method findMostMatchMethod(@NonNull List<Method> candidates, @NonNull Class<?>[] expectedTypes) {
        // record the number of matched parameter types for each method:
        // n < 0: no match
        // n == 0: match but method has no parameter
        // n > 0: match and the parameter type is the same as expected
        int[] matchCounts = new int[candidates.size()];
        Arrays.fill(matchCounts, 0);
        Method mostMatchedMethod = matchMethods(candidates, matchCounts, expectedTypes);
        if (Objects.nonNull(mostMatchedMethod)) {
            return mostMatchedMethod;
        }

        // get index of the most matched method
        // if there are multiple methods with the same number of matched parameter types or no method is matched,
        // the first method will be returned
        int indexOfMostMatchMethod = -1;
        for (int i = 1; i < matchCounts.length; i++) {
            int matchCount = matchCounts[i];
            if (matchCount < 0) {
                continue;
            }
            if (indexOfMostMatchMethod < 0
                || matchCount > matchCounts[indexOfMostMatchMethod]) {
                indexOfMostMatchMethod = i;
            }
        }
        return indexOfMostMatchMethod < 0 ? null : candidates.get(indexOfMostMatchMethod);
    }

    private Method matchMethods(List<Method> candidates, int[] matchCounts, Class<?>[] expectedTypes) {
        int expectedCount = expectedTypes.length;
        for (int i = 0; i < candidates.size(); i++) {
            Method curr = candidates.get(i);
            Class<?>[] actualTypes = curr.getParameterTypes();
            int actualCount = actualTypes.length;
            if (actualCount < expectedCount) {
                matchCounts[i] = -1; // is not matched
                continue;
            }
            // record the number of matched parameter types
            int matchCount = 0;
            for (int j = 0; j < expectedCount; j++) {
                if (!expectedTypes[j].isAssignableFrom(actualTypes[j])) {
                    matchCounts[i] = -1; // is not matched
                    break;
                }
                matchCount++;
            }
            // all parameter types are matched, it must be the most matched method
            if (matchCount == expectedCount && actualCount == expectedCount) {
                return curr;
            }
            matchCounts[i] = matchCount;
        }
        return null;
    }
}
