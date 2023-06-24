package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.TypeDynamitedDisassembleOperation;
import cn.crane4j.core.parser.operation.TypeFixedDisassembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Annotation-based {@link OperationAnnotationHandler} implementation
 * that the construction of operation configuration
 * by resolving annotations based on {@link Disassemble} on classes and attributes.
 *
 * @author huangchengxing
 * @see Disassemble
 * @since 1.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DisassembleAnnotationHandler implements OperationAnnotationHandler {

    protected final AnnotationFinder annotationFinder;
    protected final Crane4jGlobalConfiguration globalConfiguration;
    protected final Comparator<KeyTriggerOperation> operationComparator;

    /**
     * <p>Create a {@link DisassembleAnnotationHandler} instance.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public DisassembleAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Crane4jGlobalSorter.instance());
    }

    /**
     * Resolve operations from type
     *
     * @param parser         parser
     * @param beanOperations bean operations to be handler
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
        Map<Field, Disassemble> fieldLevelAnnotations = resolveFieldLevelAnnotations(beanType);
        Collection<Disassemble> classLevelAnnotations = resolveClassLevelAnnotations(beanType);
        // create operations
        return Stream.concat(
            fieldLevelAnnotations.entrySet().stream(),
            classLevelAnnotations.stream().map(a -> new AbstractMap.SimpleEntry<>(beanType, a))
        )
            .map(e -> createDisassembleOperation(beanType, e.getKey(), e.getValue(), parser))
            .sorted(operationComparator)
            .collect(Collectors.toList());
    }

    /**
     * Parse {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     */
    protected Map<Field, Disassemble> resolveFieldLevelAnnotations(Class<?> beanType) {
        Map<Field, Disassemble> disassembles = new LinkedHashMap<>();
        ReflectUtils.parseAnnotationForDeclaredFields(annotationFinder, beanType, Disassemble.class, (a, f) -> {
            disassembles.put(f, a);
            return a;
        });
        return disassembles;
    }

    /**
     * Create {@link DisassembleOperation} instance from annotation.
     *
     * @param type type
     * @param annotation annotation
     * @return {@link DisassembleOperation}
     */
    protected DisassembleOperation createDisassembleOperation(
        Class<?> type, AnnotatedElement element, Disassemble annotation, BeanOperationParser parser) {
        // get handler
        String handler = StringUtils.emptyToDefault(annotation.handler(), ReflectiveDisassembleOperationHandler.class.getSimpleName());
        DisassembleOperationHandler disassembleOperationHandler = globalConfiguration.getDisassembleOperationHandler(handler);
        Asserts.isNotNull(
            disassembleOperationHandler, "disassemble handler [{}] not found", annotation.handler()
        );

        // resolve trigger key
        String key = parseKey(element, annotation);
        // resolve sort value
        int sort = parseSort(element, annotation);

        // wait until runtime to dynamically determine the actual type if no type is specified
        DisassembleOperation operation;
        if (Objects.equals(Object.class, annotation.type()) || Objects.equals(Void.TYPE, annotation.type())) {
            operation = new TypeDynamitedDisassembleOperation(
                key, sort, type, disassembleOperationHandler, parser, globalConfiguration.getTypeResolver()
            );
        }
        // complete the parsing now if the type has been specified in the annotation
        else {
            BeanOperations operations = parser.parse(annotation.type());
            operation = new TypeFixedDisassembleOperation(
                key, sort, type, operations, disassembleOperationHandler
            );
        }

        // set group
        operation.getGroups().addAll(Arrays.asList(annotation.groups()));
        return operation;
    }

    /**
     * Parse sort value from given element and annotation. 
     *
     * @param element element
     * @param annotation annotation
     * @return sort value
     * @see Crane4jGlobalSorter#getSortValue 
     */
    protected int parseSort(AnnotatedElement element, Disassemble annotation) {
        return Crane4jGlobalSorter.INSTANCE.getSortValue(element, annotation.sort());
    }

    /**
     * Parse operation trigger key from given element and annotation.
     *
     * @param element element
     * @param annotation annotation
     * @return operation trigger key
     */
    protected String parseKey(AnnotatedElement element, Disassemble annotation) {
        return (element instanceof Field) ? ((Field) element).getName() : annotation.key();
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
