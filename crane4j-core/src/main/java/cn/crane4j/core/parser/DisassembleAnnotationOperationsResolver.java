package cn.crane4j.core.parser;

import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Operations;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.lang.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Annotation-based {@link BeanOperationsResolver} implementation
 * that the construction of operation configuration
 * by resolving annotations based on {@link Disassemble}
 * and {@link Operations} on classes and attributes.
 *
 * @author huangchengxing
 * @see Operations
 * @see Disassemble
 * @since 1.2.0
 */
public class DisassembleAnnotationOperationsResolver extends AbstractCacheableOperationResolver {

    protected static final String ANNOTATION_KEY_ATTRIBUTE = "key";
    protected final Crane4jGlobalConfiguration globalConfiguration;

    /**
     * Create a {@link DisassembleAnnotationOperationsResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param operationComparator operation comparator
     */
    public DisassembleAnnotationOperationsResolver(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        Comparator<KeyTriggerOperation> operationComparator) {
        super(annotationFinder, operationComparator);
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * <p>Create a {@link DisassembleAnnotationOperationsResolver} instance.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public DisassembleAnnotationOperationsResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Sorted.comparator());
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
        Collection<Disassemble> fieldLevelAnnotation = resolveFieldLevelAnnotations(beanType);
        Collection<Disassemble> classLevelAnnotations = resolveClassLevelAnnotations(beanType);
        return Stream.of(fieldLevelAnnotation, classLevelAnnotations)
            .flatMap(Collection::stream)
            .map(annotation -> createDisassembleOperation(beanType, annotation, context))
            .sorted(operationComparator)
            .collect(Collectors.toList());
    }

    /**
     * Parse {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     * @see #parseAnnotationForDeclaredFields
     */
    protected List<Disassemble> resolveFieldLevelAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(beanType, Disassemble.class, (a, f) -> {
            // force value to be set to the annotated attribute name
            ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, f.getName());
            return a;
        });
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

    /**
     * Parse {@link Operations} and {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     */
    protected Collection<Disassemble> resolveClassLevelAnnotations(Class<?> beanType) {
        Set<Disassemble> disassembles = annotationFinder.findAllAnnotations(beanType, Disassemble.class);
        List<Disassemble> operations = Optional.ofNullable(annotationFinder.findAnnotation(beanType, Operations.class))
            .map(Operations::disassembles)
            .map(Arrays::asList)
            .orElseGet(Collections::emptyList);
        disassembles.addAll(operations);
        return disassembles;
    }
}
