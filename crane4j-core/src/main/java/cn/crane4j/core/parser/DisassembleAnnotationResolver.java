package cn.crane4j.core.parser;

import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReflectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Annotation-based {@link OperationAnnotationResolver} implementation
 * that the construction of operation configuration
 * by resolving annotations based on {@link Disassemble} on classes and attributes.
 *
 * @author huangchengxing
 * @see Disassemble
 * @since 1.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DisassembleAnnotationResolver implements OperationAnnotationResolver {

    protected static final String ANNOTATION_KEY_ATTRIBUTE = "key";
    protected final AnnotationFinder annotationFinder;
    protected final Crane4jGlobalConfiguration globalConfiguration;
    protected final Comparator<KeyTriggerOperation> operationComparator;

    /**
     * <p>Create a {@link DisassembleAnnotationResolver} instance.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public DisassembleAnnotationResolver(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Sorted.comparator());
    }

    /**
     * Resolve operations from type
     *
     * @param parser         parser
     * @param beanOperations bean operations to be resolver
     */
    @Override
    public void resolve(BeanOperationParser parser, BeanOperations beanOperations) {
        List<DisassembleOperation> disassembleOperations = parseDisassembleOperations(parser, beanOperations)
            .stream()
            .sorted(operationComparator)
            .collect(Collectors.toList());
        disassembleOperations.forEach(beanOperations::addDisassembleOperations);
    }

    /**
     * Parse assemble operations from {@link Disassemble} annotations on class.
     *
     * @param parser parser
     * @param beanOperations operations of current to resolve
     * @return {@link DisassembleOperation}
     */
    protected List<DisassembleOperation> parseDisassembleOperations(BeanOperationParser parser, BeanOperations beanOperations) {
        AnnotatedElement source = beanOperations.getSource();
        if (!(source instanceof Class)) {
            return Collections.emptyList();
        }
        Class<?> beanType = (Class<?>)source;
        Collection<Disassemble> fieldLevelAnnotation = resolveFieldLevelAnnotations(beanType);
        Collection<Disassemble> classLevelAnnotations = resolveClassLevelAnnotations(beanType);
        return Stream.of(fieldLevelAnnotation, classLevelAnnotations)
            .flatMap(Collection::stream)
            .map(annotation -> createDisassembleOperation(beanType, annotation, parser))
            .sorted(operationComparator)
            .collect(Collectors.toList());
    }

    /**
     * Parse {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     */
    protected List<Disassemble> resolveFieldLevelAnnotations(Class<?> beanType) {
        return ReflectUtils.parseAnnotationForDeclaredFields(annotationFinder, beanType, Disassemble.class, (a, f) -> {
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
    protected DisassembleOperation createDisassembleOperation(Class<?> type, Disassemble annotation, BeanOperationParser parser) {
        // get handler
        DisassembleOperationHandler disassembleOperationHandler = ConfigurationUtil.getDisassembleOperationHandler(
            globalConfiguration, annotation.handlerName(), annotation.handler()
        );
        Asserts.isNotNull(
            disassembleOperationHandler, "disassemble handler [{}]({}) not found", annotation.handlerName(), annotation.handler()
        );

        // wait until runtime to dynamically determine the actual type if no type is specified
        DisassembleOperation operation;
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
     * Parse {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     */
    protected Collection<Disassemble> resolveClassLevelAnnotations(Class<?> beanType) {
        return annotationFinder.getAllAnnotations(beanType, Disassemble.class);
    }
}
