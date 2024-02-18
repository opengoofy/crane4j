package cn.crane4j.core.parser.handler;

import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleKeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalSorter;
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
import java.lang.reflect.Field;
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
        } else {
            annotations.putAll(source, parseAnnotationForElement(source));
        }
        return annotations.entries().stream()
            .map(e -> {
                AnnotatedElement element = e.getKey();
                A annotation = e.getValue();
                StandardAnnotation standardAnnotation = getStandardAnnotation(beanOperations, element, annotation);
                return createOperation(parser, beanOperations, element, standardAnnotation);
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
     * @param element element
     * @param standardAnnotation standard annotation
     * @return {@link KeyTriggerOperation} instance if element and annotation is resolvable, null otherwise
     */
    protected KeyTriggerOperation createOperation(
        BeanOperationParser parser, BeanOperations beanOperations, AnnotatedElement element, StandardAnnotation standardAnnotation) {
        return SimpleKeyTriggerOperation.builder()
            .id(parseId(element, standardAnnotation))
            .key(parseKey(element, standardAnnotation))
            .sort(parseSort(element, standardAnnotation))
            .groups(parseGroups(element, standardAnnotation))
            .build();
    }

    /**
     * Parse id from given {@link StandardAnnotation}.
     *
     * @param element element
     * @param annotation standard annotation
     * @return id
     * @since 2.6.0
     */
    protected String parseId(
        AnnotatedElement element, StandardAnnotation annotation) {
        String id = annotation.getId();
        if (StringUtils.isNotEmpty(id)) {
            return id;
        }
        return element instanceof Field ?
            ((Field)element).getName() : annotation.getKey();
    }

    /**
     * Get groups from given {@link StandardAnnotation}.
     *
     * @param element element
     * @param standardAnnotation standard annotation
     * @return groups
     */
    protected String parseKey(AnnotatedElement element, StandardAnnotation standardAnnotation) {
        // we should allow the key to be empty,
        // where the key value is the targets themselves.
        return (element instanceof Field) ?
            ((Field) element).getName() : standardAnnotation.getKey();
    }

    /**
     * Get sort value from given {@link StandardAnnotation}.
     *
     * @param element element
     * @param standardAnnotation standard annotation
     * @return assemble operation groups
     */
    protected int parseSort(AnnotatedElement element, StandardAnnotation standardAnnotation) {
        return Crane4jGlobalSorter.INSTANCE.getSortValue(element, standardAnnotation.getSort());
    }

    /**
     * Get groups from given {@link StandardAnnotation}.
     *
     * @param element element
     * @param standardAnnotation standard assemble operation
     * @return groups
     */
    @SuppressWarnings("unused")
    protected Set<String> parseGroups(AnnotatedElement element, StandardAnnotation standardAnnotation) {
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
    protected abstract StandardAnnotation getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, A annotation);

    /**
     * Standard annotation
     *
     * @author huangchengxing
     */
    public interface StandardAnnotation {

        /**
         * Get annotation.
         *
         * @return annotation
         * @since 2.6.0
         */
        Annotation getAnnotation();

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
    public static class StandardAnnotationAdapter implements StandardAnnotation {
        private final Annotation annotation;
        private final String id;
        private final String key;
        private final int sort;
        private final String[] groups;
    }
}
