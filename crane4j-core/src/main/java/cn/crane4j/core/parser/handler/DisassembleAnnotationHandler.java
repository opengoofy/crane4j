package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
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
import cn.crane4j.core.util.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

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
public class DisassembleAnnotationHandler
    extends AbstractStandardOperationAnnotationHandler<Disassemble> {

    protected final Crane4jGlobalConfiguration globalConfiguration;

    /**
     * <p>Create a {@link DisassembleAnnotationHandler} instance.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public DisassembleAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        super(Disassemble.class, annotationFinder, Crane4jGlobalSorter.comparator());
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * Do resolve for operations.
     *
     * @param beanOperations bean operations
     * @param operations     operations
     */
    @Override
    protected void doResolve(
        BeanOperations beanOperations, List<KeyTriggerOperation> operations) {
        operations.stream()
            .map(DisassembleOperation.class::cast)
            .forEach(beanOperations::addDisassembleOperations);
    }

    /**
     * Create assemble operation for given {@code element} and {@code annotation}
     *
     * @param parser         bean operation parser
     * @param beanOperations bean operations to resolve
     * @param element        element
     * @param standardAnnotation standard annotation
     * @return {@link KeyTriggerOperation} instance if element and annotation is resolvable, null otherwise
     */
    @Nullable
    @Override
    protected DisassembleOperation createOperation(
        BeanOperationParser parser, BeanOperations beanOperations, AnnotatedElement element, StandardAnnotation standardAnnotation) {
        KeyTriggerOperation keyTriggerOperation = super.createOperation(parser, beanOperations, element, standardAnnotation);
        Disassemble annotation = (Disassemble)standardAnnotation.getAnnotation();

        Class<?> sourceType = (Class<?>)beanOperations.getSource();
        DisassembleOperationHandler disassembleOperationHandler = globalConfiguration.getDisassembleOperationHandler(
            annotation.handler(), annotation.handlerType()
        );
        // wait until runtime to dynamically determine the actual type if no type is specified
        DisassembleOperation operation;
        if (ClassUtils.isObjectOrVoid(annotation.type())) {
            operation = TypeDynamitedDisassembleOperation.builder()
                .id(keyTriggerOperation.getId())
                .key(keyTriggerOperation.getKey())
                .sort(keyTriggerOperation.getSort())
                .groups(keyTriggerOperation.getGroups())
                .source(keyTriggerOperation.getSource())
                .sourceType(sourceType)
                .beanOperationParser(parser)
                .typeResolver(globalConfiguration.getTypeResolver())
                .disassembleOperationHandler(disassembleOperationHandler)
                .build();
        }
        // complete the parsing now if the type has been specified in the annotation
        else {
            BeanOperations operations = parser.parse(annotation.type());
            operation = TypeFixedDisassembleOperation.builder()
                .id(keyTriggerOperation.getId())
                .key(keyTriggerOperation.getKey())
                .sort(keyTriggerOperation.getSort())
                .groups(keyTriggerOperation.getGroups())
                .sourceType(sourceType)
                .internalBeanOperations(operations)
                .disassembleOperationHandler(disassembleOperationHandler)
                .build();
        }
        return operation;
    }

    /**
     * Get {@link StandardAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element        element
     * @param annotation     annotation
     * @return {@link StandardAnnotation} instance
     */
    @Override
    protected StandardAnnotation getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, Disassemble annotation) {
        return StandardAnnotationAdapter.builder()
            .annotation(annotation)
            .groups(annotation.groups())
            .id(annotation.id())
            .key(annotation.key())
            .sort(annotation.sort())
            .build();
    }
}
