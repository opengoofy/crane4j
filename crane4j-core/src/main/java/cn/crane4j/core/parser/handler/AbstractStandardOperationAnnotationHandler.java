package cn.crane4j.core.parser.handler;

import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleKeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.MultiMap;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A basic {@link OperationAnnotationHandler} implementation.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractStandardOperationAnnotationHandler<A extends Annotation> implements OperationAnnotationHandler  {

    protected final Class<A> annotationType;
    protected final AnnotationFinder annotationFinder;
    @NonNull
    @Setter
    protected Comparator<KeyTriggerOperation> operationComparator;

    /**
     * Resolve operations from element.
     *
     * @param parser bean operation parser
     * @param beanOperations bean operations to be handler
     */
    @Override
    public void resolve(BeanOperationParser parser, BeanOperations beanOperations) {
        AnnotatedElement source = beanOperations.getSource();
        log.debug("resolve operations from [{}]", source);
        // resolve assemble operations
        List<KeyTriggerOperation> operations = parseOperations(parser, beanOperations)
            .stream()
            .sorted(operationComparator)
            .collect(Collectors.toList());
        doResolve(beanOperations, operations);
    }

    /**
     * Do resolve for operations.
     *
     * @param beanOperations bean operations
     * @param operations operations
     */
    protected abstract void doResolve(BeanOperations beanOperations, List<KeyTriggerOperation> operations);

    // =============== scan annotations ===============

    /**
     * Parse assemble operations for class.
     *
     * @param parser bean operation parser
     * @param beanOperations operations of current to resolve
     */
    private List<KeyTriggerOperation> parseOperations(
        BeanOperationParser parser, BeanOperations beanOperations) {
        AnnotatedElement source = beanOperations.getSource();
        MultiMap<AnnotatedElement, A> annotations = MultiMap.arrayListMultimap();
        if (source instanceof Class<?>) {
            Class<?> beanType = (Class<?>)source;
            annotations.putAll(beanType, parseAnnotationForClass(beanType));
            annotations.putAll(parseAnnotationForFields(beanType));
            annotations.putAll(parseAnnotationForMethods(beanType));
        } else {
            annotations.putAll(source, parseAnnotationForElement(source));
        }
        return annotations.entries().stream()
            .map(e -> {
                AnnotatedElement element = e.getKey();
                A annotation = e.getValue();
                StandardAnnotation<A> standardAnnotation = getStandardAnnotation(beanOperations, element, annotation);
                return createOperation(parser, beanOperations, standardAnnotation);
            })
            .filter(Objects::nonNull)
            .sorted(operationComparator)
            .collect(Collectors.toList());
    }

    /**
     * Parse annotation for element.
     *
     * @param element element
     * @return annotations
     */
    protected Set<A> parseAnnotationForElement(AnnotatedElement element) {
        return annotationFinder.getAllAnnotations(element, annotationType);
    }

    /**
     * Parse annotation for class.
     *
     * @param beanType bean type
     * @return annotations
     */
    protected Set<A> parseAnnotationForClass(Class<?> beanType) {
        return parseAnnotationForElement(beanType);
    }

    /**
     * Parse annotation for methods
     *
     * @param beanType bean type
     * @return element and annotation map
     * @since 2.6.0
     */
    protected MultiMap<AnnotatedElement, A> parseAnnotationForMethods(Class<?> beanType) {
        MultiMap<AnnotatedElement, A> result = MultiMap.arrayListMultimap();
        Method[] methods = Stream.of(ReflectUtils.getDeclaredMethods(beanType))
            .filter(method -> method.getParameterCount() == 0)
            .filter(method -> !Objects.equals(method.getReturnType(), Void.TYPE))
            .toArray(Method[]::new);
        ReflectUtils.scanAllAnnotationFromElements(
            annotationFinder, annotationType, methods, result::put);
        return result;
    }

    /**
     * Parse annotation for fields
     *
     * @param beanType bean type
     * @return element and annotation map
     */
    protected MultiMap<AnnotatedElement, A> parseAnnotationForFields(Class<?> beanType) {
        MultiMap<AnnotatedElement, A> result = MultiMap.arrayListMultimap();
        ReflectUtils.scanAllAnnotationFromElements(
            annotationFinder, annotationType, ReflectUtils.getDeclaredFields(beanType), result::put);
        return result;
    }

    // =============== process standard configuration ===============

    /**
     * Create assemble operation for given {@code element} and {@code annotation}
     *
     * @param parser bean operation parser
     * @param beanOperations bean operations to resolve
     * @param standardAnnotation standard annotation
     * @return {@link KeyTriggerOperation} instance if element and annotation is resolvable, null otherwise
     */
    protected KeyTriggerOperation createOperation(
        BeanOperationParser parser, BeanOperations beanOperations, StandardAnnotation<A> standardAnnotation) {
        AnnotatedElement element = standardAnnotation.getAnnotatedElement();
        return SimpleKeyTriggerOperation.builder()
            .id(parseId(standardAnnotation))
            .key(parseKey(standardAnnotation))
            .sort(parseSort(standardAnnotation))
            .groups(parseGroups(standardAnnotation))
            .source(element)
            .build();
    }

    /**
     * Parse id from given {@link StandardAnnotation}.
     *
     * @param standardAnnotation standard annotation
     * @return id
     * @since 2.6.0
     */
    protected String parseId(StandardAnnotation<A> standardAnnotation) {
        AnnotatedElement element = standardAnnotation.getAnnotatedElement();
        String id = standardAnnotation.getId();
        return StringUtils.isNotEmpty(id) ?
            id : ConfigurationUtil.getElementIdentifier(element, standardAnnotation.getKey());
    }

    /**
     * Get groups from given {@link StandardAnnotation}.
     *
     * @param standardAnnotation standard annotation
     * @return groups
     */
    protected String parseKey(StandardAnnotation<A> standardAnnotation) {
        AnnotatedElement element = standardAnnotation.getAnnotatedElement();
        return ConfigurationUtil.getElementIdentifier(element, standardAnnotation.getKey());
    }

    /**
     * Get sort value from given {@link StandardAnnotation}.
     *
     * @param standardAnnotation standard annotation
     * @return assemble operation groups
     */
    protected int parseSort(StandardAnnotation<A> standardAnnotation) {
        AnnotatedElement element = standardAnnotation.getAnnotatedElement();
        return Crane4jGlobalSorter.INSTANCE.getSortValue(element, standardAnnotation.getSort());
    }

    /**
     * Get groups from given {@link StandardAnnotation}.
     *
     * @param standardAnnotation standard assemble operation
     * @return groups
     */
    protected Set<String> parseGroups(StandardAnnotation<A> standardAnnotation) {
        return Stream.of(standardAnnotation.getGroups())
            .collect(Collectors.toSet());
    }
    
    /**
     * Get {@link StandardAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element element
     * @param annotation annotation
     * @return {@link StandardAnnotation} instance
     */
    protected abstract StandardAnnotation<A> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, A annotation);

    /**
     * Standard annotation
     *
     * @author huangchengxing
     */
    public interface StandardAnnotation<A> {

        /**
         * Get the element which annotated by the annotation.
         *
         * @return element
         */
        AnnotatedElement getAnnotatedElement();

        /**
         * Get annotation.
         *
         * @return annotation
         * @since 2.6.0
         */
        A getAnnotation();

        /**
         * The id of the current operation.
         *
         * @return id
         * @since 2.6.0
         */
        String getId();

        /**
         * Key field name for query
         *
         * @return key field name
         */
        String getKey();

        /**
         * <p>Sort values.
         * The lower the value, the higher the priority.
         *
         * @return sort values
         */
        int getSort();
        /**
         * The group to which the current operation belongs.
         *
         * @return group names
         */
        String[] getGroups();
    }

    /**
     * Adapting annotation to {@link StandardAnnotation}
     *
     * @author huangchengxing
     */
    @SuperBuilder
    @Getter
    public static class StandardAnnotationAdapter<A extends Annotation> implements StandardAnnotation<A> {
        @lombok.NonNull
        private final AnnotatedElement annotatedElement;
        @lombok.NonNull
        private final A annotation;
        @lombok.NonNull
        private final String id;
        @lombok.NonNull
        private final String key;
        private final int sort;
        @lombok.NonNull
        private final String[] groups;
    }
}
