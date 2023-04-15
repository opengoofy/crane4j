package cn.crane4j.core.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Operations;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Annotation-based {@link OperationAnnotationResolver} implementation
 * that the construction of operation configuration
 * by resolving annotations based on {@link Assemble}
 * and {@link Operations} on classes and attributes.
 *
 * @author huangchengxing
 * @see Operations
 * @see Assemble
 * @since 1.2.0
 */
@Slf4j
public class AssembleAnnotationResolver extends AbstractCacheableOperationAnnotationResolver {

    protected static final String ANNOTATION_KEY_ATTRIBUTE = "key";
    protected final Crane4jGlobalConfiguration globalConfiguration;

    /**
     * Create a {@link AssembleAnnotationResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param operationComparator operation comparator
     */
    public AssembleAnnotationResolver(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        Comparator<KeyTriggerOperation> operationComparator) {
        super(annotationFinder, operationComparator);
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * <p>Create a {@link AssembleAnnotationResolver} instance.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public AssembleAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Sorted.comparator());
    }

    /**
     * Parse assemble operations from {@link Assemble} annotations on class.
     *
     * @param context  context
     * @param element annotated element
     * @return {@link AssembleOperation}
     */
    @Override
    protected List<AssembleOperation> parseAssembleOperations(OperationParseContext context, AnnotatedElement element) {
        List<Assemble> annotations = new ArrayList<>();
        if (element instanceof Class<?>) {
            Class<?> beanType = (Class<?>) element;
            annotations.addAll(resolveFieldLevelAnnotations(beanType));
            annotations.addAll(resolveClassLevelAnnotations(beanType));
        } else {
            annotations.addAll(annotationFinder.findAllAnnotations(element, Assemble.class));
        }
        return annotations.stream()
            .map(this::createAssembleOperation)
            .sorted(operationComparator)
            .collect(Collectors.toList());
    }

    /**
     * Parse {@link Assemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Assemble}
     */
    protected List<Assemble> resolveFieldLevelAnnotations(Class<?> beanType) {
        return ReflectUtils.parseAnnotationForDeclaredFields(annotationFinder, beanType, Assemble.class, (a, f) -> {
            // force value to be set to the annotated attribute name
            ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, f.getName());
            return a;
        });
    }

    /**
     * Create {@link AssembleOperation} instance from annotation.
     *
     * @param annotation annotation
     * @return {@link AssembleOperation}
     */
    protected AssembleOperation createAssembleOperation(Assemble annotation) {
        Assert.isTrue(CharSequenceUtil.isNotBlank(annotation.key()), throwException("the key of assemble operation must not blank"));
        // get operation handler
        AssembleOperationHandler assembleOperationHandler = ConfigurationUtil.getAssembleOperationHandler(
            globalConfiguration, annotation.handlerName(), annotation.handler()
        );
        Assert.notNull(assembleOperationHandler, throwException("assemble operation handler [{}]({}) not found", annotation.handlerName(), annotation.handler()));

        // resolved property mapping from annotation and template
        Set<PropertyMapping> propertyMappings = Stream.of(annotation.props())
            .map(m -> ConfigurationUtil.createPropertyMapping(m, annotation.key()))
            .collect(Collectors.toSet());
        List<PropertyMapping> templateMappings = ConfigurationUtil.parsePropTemplateClasses(annotation.propTemplates(), annotationFinder);
        if (CollUtil.isNotEmpty(templateMappings)) {
            propertyMappings.addAll(templateMappings);
        }

        // get container
        Container<?> container = getContainer(annotation);

        // create operation
        AssembleOperation operation = new SimpleAssembleOperation(
            annotation.key(), annotation.sort(),
            propertyMappings, container, assembleOperationHandler
        );
        operation.getGroups().addAll(Arrays.asList(annotation.groups()));
        return operation;
    }

    /**
     * Get container.
     *
     * @param annotation annotation
     * @return container
     * @throws IllegalArgumentException thrown when the container is null
     */
    protected Container<?> getContainer(Assemble annotation) {
        // determine provider
        ContainerProvider provider = ConfigurationUtil.getContainerProvider(
            globalConfiguration, annotation.containerProviderName(), annotation.containerProvider()
        );
        provider = ObjectUtil.defaultIfNull(provider, globalConfiguration);
        // get from provider
        Container<?> container = CharSequenceUtil.isNotEmpty(annotation.container()) ?
            provider.getContainer(annotation.container()) : Container.empty();
        Assert.notNull(
            container, throwException("cannot find container [{}] from provider [{}]", annotation.container(), provider.getClass())
        );
        return container;
    }

    /**
     * Parse {@link Operations} and {@link Assemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Assemble}
     */
    protected Collection<Assemble> resolveClassLevelAnnotations(Class<?> beanType) {
        Set<Assemble> assembles = annotationFinder.getAllAnnotations(beanType, Assemble.class);
        List<Assemble> operations = Optional.ofNullable(annotationFinder.getAnnotation(beanType, Operations.class))
            .map(Operations::assembles)
            .map(Arrays::asList)
            .orElseGet(Collections::emptyList);
        assembles.addAll(operations);
        return assembles;
    }
}
