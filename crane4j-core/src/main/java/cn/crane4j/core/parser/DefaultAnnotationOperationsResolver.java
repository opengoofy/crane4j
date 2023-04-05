package cn.crane4j.core.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Operations;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Annotation-based {@link BeanOperationsResolver} implementation.<br />
 * Support the construction of operation configuration
 * by resolving annotations based on {@link Assemble}, {@link Disassemble}
 * and {@link Operations} on classes and attributes.<br />
 *
 * <p>The parser will ensure the sorting between assembly and disassembly operations
 * in {@link BeanOperations} according to the configured sorting value,
 * which can be changed by specifying a comparator when the constructor calls.<br />
 * It should be noted that this order does not represent the order in which the final operation will be executed.
 * This order is guaranteed by the executor {@link BeanOperationExecutor}.
 *
 * @author huangchengxing
 * @since 1.2.0
 */
@Slf4j
public class DefaultAnnotationOperationsResolver extends AbstractCacheableOperationResolver {

    protected static final String ANNOTATION_KEY_ATTRIBUTE = "key";
    protected final Crane4jGlobalConfiguration globalConfiguration;

    /**
     * Create an operation parser that supports annotation configuration.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param operationComparator operation comparator
     */
    public DefaultAnnotationOperationsResolver(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        Comparator<KeyTriggerOperation> operationComparator) {
        super(annotationFinder, operationComparator);
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * <p>Create an operation parser that supports annotation configuration.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public DefaultAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Sorted.comparator());
    }

    // ======================= assemble operations =======================

    /**
     * Parse {@link Assemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Assemble}
     * @see #parseAnnotationForDeclaredFields
     */
    protected List<Assemble> parseAssembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(beanType, Assemble.class, (a, f) -> {
            // force value to be set to the annotated attribute name
            ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, f.getName());
            return a;
        });
    }

    /**
     * Parse assemble operations from {@link Assemble} annotations on class.
     *
     * @param context  context
     * @param beanType bean type
     * @return {@link AssembleOperation}
     * @see #parseAnnotationForDeclaredFields
     */
    @Override
    protected List<AssembleOperation> parseAssembleOperations(OperationParseContext context, Class<?> beanType) {
        Collection<Assemble> assembles = parseAssembleAnnotations(beanType);
        Operations operations = parseOperationsAnnotation(beanType);
        if (Objects.nonNull(operations)) {
            CollUtil.addAll(assembles, operations.assembles());
        }
        return assembles.stream()
            .map(this::createAssembleOperation)
            .sorted(operationComparator)
            .collect(Collectors.toList());
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

    // ======================= disassemble operations =======================

    /**
     * Parse {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     * @see #parseAnnotationForDeclaredFields
     */
    protected List<Disassemble> parseDisassembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(beanType, Disassemble.class, (a, f) -> {
            // force value to be set to the annotated attribute name
            ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, f.getName());
            return a;
        });
    }

    /**
     * Parse assemble operations from {@link Disassemble} annotations on class.
     *
     * @param context  context
     * @param beanType bean type
     * @return {@link DisassembleOperation}
     * @see #parseAnnotationForDeclaredFields
     */
    @Override
    protected List<DisassembleOperation> parseDisassembleOperations(OperationParseContext context, Class<?> beanType) {
        Collection<Disassemble> disassembles = parseDisassembleAnnotations(beanType);
        Operations operations = parseOperationsAnnotation(beanType);
        if (Objects.nonNull(operations)) {
            CollUtil.addAll(disassembles, operations.disassembles());
        }
        return disassembles.stream()
            .map(annotation -> createDisassembleOperation(beanType, annotation, context))
            .sorted(operationComparator)
            .collect(Collectors.toList());
    }

    /**
     * Create {@link DisassembleOperation} instance from annotation.
     *
     * @param type type
     * @param annotation annotation
     * @return {@link DisassembleOperation}
     */
    protected DisassembleOperation createDisassembleOperation(Class<?> type, Disassemble annotation, OperationParseContext context) {
        // get handler
        DisassembleOperationHandler disassembleOperationHandler = ConfigurationUtil.getDisassembleOperationHandler(
            globalConfiguration, annotation.handlerName(), annotation.handler()
        );
        Assert.notNull(disassembleOperationHandler, throwException("disassemble handler [{}]({}) not found", annotation.handlerName(), annotation.handler()));

        // wait until runtime to dynamically determine the actual type if no type is specified
        DisassembleOperation operation;
        BeanOperationParser parser = context.getParser();
        if (Objects.equals(Object.class, annotation.type()) || Objects.equals(Void.TYPE, annotation.type())) {
            operation = new TypeDynamitedDisassembleOperation(
                annotation.key(), annotation.sort(),
                type, disassembleOperationHandler, parser,
                globalConfiguration.getTypeResolver()
            );
        }
        // complete the parsing now if the type has been specified in the annotation
        else {
            BeanOperations operations = parser.parse(annotation.type());
            operation = new TypeFixedDisassembleOperation(
                annotation.key(), annotation.sort(),
                type, operations, disassembleOperationHandler
            );
        }

        // set group
        operation.getGroups().addAll(Arrays.asList(annotation.groups()));
        return operation;
    }

    // ======================= common operations =======================

    /**
     * Parse {@link Operations} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Operations}
     */
    protected Operations parseOperationsAnnotation(Class<?> beanType) {
        return annotationFinder.findAnnotation(beanType, Operations.class);
    }
}
