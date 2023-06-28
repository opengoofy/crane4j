package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.MultiMap;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

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
 * <p>An abstract {@link OperationAnnotationHandler} implementation
 * for resolve assemble operation annotation.<br />
 * pre-implements the defined the logic of parsing and
 * constructing {@link AssembleOperation} based on standard components.
 *
 * @author huangchengxing
 * @param <T> annotation type
 * @see StandardAnnotation
 * @since 1.3.0
 */
@Accessors(chain = true)
@Slf4j
public abstract class AbstractAssembleAnnotationHandler<T extends Annotation> implements OperationAnnotationHandler {

    protected final Class<T> annotationType;
    protected final AnnotationFinder annotationFinder;
    protected final Comparator<KeyTriggerOperation> operationComparator;
    protected final Crane4jGlobalConfiguration globalConfiguration;

    /**
     * Create an {@link AbstractAssembleAnnotationHandler} comparator.
     *
     * @param annotationType annotation type
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param globalConfiguration global configuration
     */
    protected AbstractAssembleAnnotationHandler(
        Class<T> annotationType, AnnotationFinder annotationFinder,
        Comparator<KeyTriggerOperation> operationComparator, Crane4jGlobalConfiguration globalConfiguration) {
        this.annotationType = annotationType;
        this.annotationFinder = annotationFinder;
        this.operationComparator = operationComparator;
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * Resolve operations from type
     *
     * @param parser parser
     * @param beanOperations bean operations to be handler
     */
    @Override
    public void resolve(BeanOperationParser parser, BeanOperations beanOperations) {
        AnnotatedElement source = beanOperations.getSource();
        log.debug("resolve operations from [{}]", source);

        // resolve assemble operations
        List<AssembleOperation> assembleOperations = parseAssembleOperations(beanOperations)
            .stream()
            .sorted(operationComparator)
            .collect(Collectors.toList());
        assembleOperations.forEach(beanOperations::addAssembleOperations);
    }

    /**
     * Parse assemble operations for class.
     *
     * @param beanOperations operations of current to resolve
     * @return {@link AssembleOperation}
     */
    private List<AssembleOperation> parseAssembleOperations(BeanOperations beanOperations) {
        AnnotatedElement source = beanOperations.getSource();
        MultiMap<AnnotatedElement, T> annotations = MultiMap.arrayListMultimap();
        if (source instanceof Class<?>) {
            Class<?> beanType = (Class<?>)source;
            annotations.putAll(beanType, parseAnnotationForClass(beanType));
            annotations.putAll(parseAnnotationForFields(beanType));
        } else {
            annotations.putAll(source, parseAnnotationForElement(source));
        }
        return annotations.entries().stream()
            .map(e -> createAssembleOperation(beanOperations, e.getKey(), e.getValue()))
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
    protected Set<T> parseAnnotationForElement(AnnotatedElement element) {
        return annotationFinder.getAllAnnotations(element, annotationType);
    }

    /**
     * Parse annotation for class.
     *
     * @param beanType bean type
     * @return annotations
     */
    protected Set<T> parseAnnotationForClass(Class<?> beanType) {
        return parseAnnotationForElement(beanType);
    }
    
    /**
     * Parse annotation for fields
     *
     * @param beanType bean type
     * @return element and annotation map
     */
    protected MultiMap<AnnotatedElement, T> parseAnnotationForFields(Class<?> beanType) {
        MultiMap<AnnotatedElement, T> result = MultiMap.arrayListMultimap();
        ReflectUtils.scanAllAnnotationFromElements(
            annotationFinder, annotationType, ReflectUtils.getDeclaredFields(beanType), result::put);
        return result;
    }

    // =============== resolve assemble operation ===============

    /**
     * Create assemble operation for given {@code element} and {@code annotation}
     *
     * @param beanOperations bean operations to resolve
     * @param element element
     * @param annotation annotation
     * @return {@link AssembleOperation} comparator if element and annotation is resolvable, null otherwise
     */
    @Nullable
    protected AssembleOperation createAssembleOperation(
        BeanOperations beanOperations, AnnotatedElement element, T annotation) {
        StandardAnnotation standardAnnotation = getStandardAnnotation(beanOperations, element, annotation);

        // get configuration of standard assemble operation
        String key = parseKey(element, standardAnnotation);
        AssembleOperationHandler assembleOperationHandler = parseAssembleOperationHandler(element, standardAnnotation);
        Set<PropertyMapping> propertyMappings = parsePropertyMappings(element, standardAnnotation, key);
        int sort = parseSort(element, standardAnnotation);
        Set<String> groups = parseGroups(element, standardAnnotation);

        // create operation
        AssembleOperation operation = createAssembleOperation(annotation, sort, key, assembleOperationHandler, propertyMappings);
        operation.getGroups().addAll(groups);
        return operation;
    }

    /**
     * Create assemble operation for given {@code annotation}, {@code sort}, {@code key}, {@code assembleOperationHandler} and {@code propertyMappings}
     *
     * @param annotation annotation
     * @param sort sort
     * @param key key
     * @param handler assemble operation handler
     * @param propertyMappings property mappings
     * @return {@link AssembleOperation} comparator
     */
    protected AssembleOperation createAssembleOperation(
        T annotation, int sort, String key, AssembleOperationHandler handler, Set<PropertyMapping> propertyMappings) {
        String namespace = getContainerNamespace(annotation);
        return new SimpleAssembleOperation(key, sort, propertyMappings, namespace, handler);
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return namespace of {@link Container}
     * @implNote if the container needs to be obtained through a specific provider,
     * the name of the provider and the namespace of the container need to be concatenated through {@link ContainerManager#canonicalNamespace}
     * @see ContainerManager#canonicalNamespace
     */
    protected abstract String getContainerNamespace(T annotation);

    /**
     * Get {@link StandardAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element element
     * @param annotation annotation
     * @return {@link StandardAnnotation} comparator
     */
    protected abstract StandardAnnotation getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, T annotation);

    // =============== process standard configuration ===============

    /**
     * Get groups from given {@link StandardAnnotation}.
     *
     * @param element element
     * @param standardAnnotation standard annotation
     * @return groups
     */
    protected String parseKey(AnnotatedElement element, StandardAnnotation standardAnnotation) {
        String key = (element instanceof Field) ?
            ((Field) element).getName() : standardAnnotation.getKey();
        Asserts.isTrue(StringUtils.isNotBlank(key), "the key of assemble operation must not blank");
        return key;
    }

    /**
     * Get assemble operation groups from given {@link StandardAnnotation}.
     *
     * @param element element
     * @param standardAnnotation standard annotation
     * @return assemble operation groups
     */
    protected AssembleOperationHandler parseAssembleOperationHandler(
        AnnotatedElement element, StandardAnnotation standardAnnotation) {
        String handler = StringUtils.emptyToDefault(standardAnnotation.getHandler(), OneToOneAssembleOperationHandler.class.getSimpleName());
        AssembleOperationHandler assembleOperationHandler = globalConfiguration.getAssembleOperationHandler(handler);
        Asserts.isNotNull(assembleOperationHandler, "assemble operation handler [{}] not found", handler);
        return assembleOperationHandler;
    }

    /**
     * Get property mapping from given {@link StandardAnnotation}.
     *
     * @param element element
     * @param standardAnnotation standard annotation
     * @param key key
     * @return assemble operation groups
     */
    protected Set<PropertyMapping> parsePropertyMappings(
        AnnotatedElement element, StandardAnnotation standardAnnotation, String key) {
        Mapping[] props = standardAnnotation.getProps();
        Set<PropertyMapping> propertyMappings = Stream.of(props)
            .map(m -> ConfigurationUtil.createPropertyMapping(m, key))
            .collect(Collectors.toSet());
        Class<?>[] propTemplates = standardAnnotation.getMappingTemplates();
        List<PropertyMapping> templateMappings = ConfigurationUtil.parsePropTemplateClasses(propTemplates, annotationFinder);
        if (CollectionUtils.isNotEmpty(templateMappings)) {
            propertyMappings.addAll(templateMappings);
        }
        return propertyMappings;
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
    protected Set<String> parseGroups(AnnotatedElement element, StandardAnnotation standardAnnotation) {
        return Stream.of(standardAnnotation.getGroups())
            .collect(Collectors.toSet());
    }

    /**
     * Standard annotation
     *
     * @author huangchengxing
     * @see StandardAnnotationAdapter
     */
    public interface StandardAnnotation {

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
         * The name of the handler to be used.
         *
         * @return name of the handler
         */
        String getHandler();

        /**
         * <p>Mapping template classes.
         * specify a class, if {@link MappingTemplate} exists on the class,
         * it will scan and add {@link Mapping} to {@link #getProps}。
         *
         * @return java.lang.Class<?>[]
         */
        Class<?>[] getMappingTemplates();

        /**
         * Attributes that need to be mapped
         * between the data source object and the current object.
         *
         * @return attributes mappings
         */
        Mapping[] getProps();
        
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
    @Getter
    @RequiredArgsConstructor
    public static class StandardAnnotationAdapter implements StandardAnnotation {
        private final Annotation annotation;
        private final String key;
        private final int sort;
        private final String handler;
        private final Class<?>[] mappingTemplates;
        private final Mapping[] props;
        private final String[] groups;
    }
}
