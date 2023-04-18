package cn.crane4j.core.parser;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.AnnotationFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>An abstract {@link OperationAnnotationResolver} implementation
 * that cacheable、 annotation aware and support operation compare.<br />
 * Overwrite {@link #parseAssembleOperations} and {@link #parseDisassembleOperations}
 * to implement custom parsing logic.
 *
 * @author huangchengxing
 * @since 1.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOperationAnnotationResolver implements OperationAnnotationResolver {

    protected final AnnotationFinder annotationFinder;
    protected final Comparator<KeyTriggerOperation> operationComparator;

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

        // resolve disassemble operations
        List<DisassembleOperation> disassembleOperations = parseDisassembleOperations(parser, beanOperations)
            .stream()
            .sorted(operationComparator)
            .collect(Collectors.toList());
        disassembleOperations.forEach(beanOperations::addDisassembleOperations);
    }

    /**
     * Parse assemble operations for class.
     *
     * @param beanOperations operations of current to resolve
     * @return {@link AssembleOperation}
     */
    protected List<AssembleOperation> parseAssembleOperations(BeanOperations beanOperations) {
        return Collections.emptyList();
    }

    /**
     * Parse disassemble operations for class.
     *
     * @param parser parser
     * @param beanOperations operations of current to resolve
     * @return {@link DisassembleOperation}
     */
    protected List<DisassembleOperation> parseDisassembleOperations(BeanOperationParser parser, BeanOperations beanOperations) {
        return Collections.emptyList();
    }

    /**
     * Get supplier of {@link Crane4jException}.
     *
     * @param errTemp errTemp
     * @param args args
     * @return supplier of exception
     */
    protected static Supplier<Crane4jException> throwException(String errTemp, Object... args) {
        return () -> new Crane4jException(errTemp, args);
    }
}
