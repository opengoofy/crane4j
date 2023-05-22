package cn.crane4j.core.parser;

import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.StandardAssembleAnnotation;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.util.*;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>An abstract {@link OperationAnnotationResolver} implementation
 * that comparator、annotation aware and support assemble operation compare.<br />
 * Overwrite {@link #createAssembleOperation} to implement custom parsing logic.
 *
 * @author huangchengxing
 * @param <T> annotation type
 * @see StandardAssembleAnnotation
 * @since 1.3.0
 */
@Accessors(chain = true)
@Slf4j
public abstract class StandardAssembleAnnotationResolver<T extends Annotation> implements OperationAnnotationResolver {

    protected static final String ANNOTATION_KEY_ATTRIBUTE = "key";
    private final StandardAssembleAnnotation standardAssembleAnnotation;
    protected final Class<T> annotationType;
    protected final AnnotationFinder annotationFinder;
    protected final Comparator<KeyTriggerOperation> operationComparator;
    protected final Crane4jGlobalConfiguration globalConfiguration;
    @Setter
    private boolean lazyLoadAssembleContainer = true;

    /**
     * Create an {@link StandardAssembleAnnotationResolver} instance.
     *
     * @param annotationType annotation type
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param globalConfiguration global configuration
     */
    protected StandardAssembleAnnotationResolver(
        Class<T> annotationType, AnnotationFinder annotationFinder,
        Comparator<KeyTriggerOperation> operationComparator, Crane4jGlobalConfiguration globalConfiguration) {
        this.annotationType = annotationType;
        this.annotationFinder = annotationFinder;
        this.operationComparator = operationComparator;
        this.globalConfiguration = globalConfiguration;
        this.standardAssembleAnnotation = annotationFinder.findAnnotation(annotationType, StandardAssembleAnnotation.class);
    }

    /**
     * Resolve operations from type
     *
     * @param parser parser
     * @param beanOperations bean operations to be resolver
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
        ReflectUtils.parseAnnotationForDeclaredFields(annotationFinder, beanType, annotationType, (annotation, field) -> result.put(field, annotation));
        result.forEach((e, a) -> ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, ((Field)e).getName()));
        return result;
    }

    // =============== resolve assemble operation ===============

    /**
     * Create assemble operation for given {@code element} and {@code annotation}
     *
     * @param beanOperations bean operations to resolve
     * @param element element
     * @param annotation annotation
     * @return {@link AssembleOperation} instance if element and annotation is resolvable, null otherwise
     * @see StandardAssembleAnnotation
     */
    @Nullable
    protected AssembleOperation createAssembleOperation(
        BeanOperations beanOperations, AnnotatedElement element, T annotation) {
        Asserts.isNotNull(
            standardAssembleAnnotation, "cannot find @StandardAssembleAnnotation in annotation [{}], it is not an standard assemble annotation", annotationType
        );
        Map<String, Object> attributes = ReflectUtils.getAnnotationAttributes(annotation);

        // get configuration of standard assemble operation
        String key = parseKey(annotation, attributes);
        AssembleOperationHandler assembleOperationHandler = parseAssembleOperationHandler(annotation, attributes);
        Set<PropertyMapping> propertyMappings = parsePropertyMappings(annotation, attributes, key);
        int sort = parseSort(annotation, attributes);
        Set<String> groups = parseGroups(annotation, attributes);

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
     * @return {@link AssembleOperation} instance
     */
    protected AssembleOperation createAssembleOperation(
        T annotation, int sort, String key, AssembleOperationHandler handler, Set<PropertyMapping> propertyMappings) {
        if (lazyLoadAssembleContainer) {
            Supplier<Container<?>> containerFactory = new Lazy<>(() -> getContainer(annotation));
            return new LazyAssembleOperation(key, sort, propertyMappings, containerFactory, handler);
        }
        Container<?> container = getContainer(annotation);
        return new SimpleAssembleOperation(key, sort, propertyMappings, container, handler);
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return {@link Container} instance
     */
    protected abstract Container<?> getContainer(T annotation);

    // =============== process standard configuration ===============

    /**
     * Get groups from given {@code attributes}.
     *
     * @param annotation annotation
     * @param attributes attributes
     * @return groups
     */
    protected String parseKey(T annotation, Map<String, Object> attributes) {
        String key = (String)attributes.get(standardAssembleAnnotation.keyAttribute());
        Asserts.isTrue(StringUtils.isNotBlank(key), "the key of assemble operation must not blank");
        return key;
    }

    /**
     * Get assemble operation groups from given {@code attributes}.
     *
     * @param annotation annotation
     * @param attributes attributes
     * @return assemble operation groups
     */
    protected AssembleOperationHandler parseAssembleOperationHandler(T annotation, Map<String, Object> attributes) {
        Class<?> handler = (Class<?>)attributes.get(standardAssembleAnnotation.handlerAttribute());
        String handlerName = (String)attributes.get(standardAssembleAnnotation.handlerNameAttribute());
        AssembleOperationHandler assembleOperationHandler = ConfigurationUtil.getAssembleOperationHandler(
            globalConfiguration, handlerName, handler
        );
        Asserts.isNotNull(assembleOperationHandler, "assemble operation handler [{}]({}) not found", handlerName, handler);
        return assembleOperationHandler;
    }

    /**
     * Get property mapping from given {@code attributes}.
     *
     * @param annotation annotation
     * @param attributes attributes
     * @param key key
     * @return assemble operation groups
     */
    protected Set<PropertyMapping> parsePropertyMappings(T annotation, Map<String, Object> attributes, String key) {
        Mapping[] props = (Mapping[])attributes.get(standardAssembleAnnotation.propsAttribute());
        Set<PropertyMapping> propertyMappings = Stream.of(props)
            .map(m -> ConfigurationUtil.createPropertyMapping(m, key))
            .collect(Collectors.toSet());
        Class<?>[] propTemplates = (Class<?>[])attributes.get(standardAssembleAnnotation.propTemplatesAttribute());
        List<PropertyMapping> templateMappings = ConfigurationUtil.parsePropTemplateClasses(propTemplates, annotationFinder);
        if (CollectionUtils.isNotEmpty(templateMappings)) {
            propertyMappings.addAll(templateMappings);
        }
        return propertyMappings;
    }

    /**
     * Get sort value from given {@code attributes}.
     *
     * @param annotation annotation
     * @param attributes attributes
     * @return assemble operation groups
     */
    protected int parseSort(T annotation, Map<String, Object> attributes) {
        return (int)attributes.get(standardAssembleAnnotation.sortAttribute());
    }

    /**
     * Get groups from given {@code attributes}.
     *
     * @param annotation annotation
     * @param attributes attributes
     * @return groups
     */
    protected Set<String> parseGroups(T annotation, Map<String, Object> attributes) {
        return Stream.of((String[])attributes.get(standardAssembleAnnotation.groupsAttribute()))
            .collect(Collectors.toSet());
    }
}
