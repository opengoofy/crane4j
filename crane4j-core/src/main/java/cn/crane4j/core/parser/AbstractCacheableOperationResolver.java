package cn.crane4j.core.parser;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.exception.OperationParseException;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>An abstract {@link BeanOperationsResolver} implementation
 * that cacheable、 annotation aware and support operation compare.<br />
 * Overwrite {@link #parseAssembleOperations} and {@link #parseDisassembleOperations}
 * to implement custom parsing logic.
 *
 * @author huangchengxing
 * @since 1.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCacheableOperationResolver implements BeanOperationsResolver {

    private final Map<AnnotatedElement, OperationMetadata> metadataCaches = CollectionUtils.newWeakConcurrentMap();
    protected final AnnotationFinder annotationFinder;
    protected final Comparator<KeyTriggerOperation> operationComparator;

    /**
     * <p>Resolve operations from type.<br />
     * If there is a cache, it will be obtained from the cache first.
     *
     * @param context context in parsing
     * @param annotatedElement annotated element
     * @throws OperationParseException thrown when configuration resolution exception
     */
    @Override
    public void resolve(OperationParseContext context, AnnotatedElement annotatedElement) {
        BeanOperations target = context.getRootOperations();
        log.debug("resolve operations from [{}]", annotatedElement);
        OperationMetadata cache = CollectionUtils.computeIfAbsent(
            metadataCaches, annotatedElement, t -> createMetaDataCache(context, t)
        );
        cache.append(target);
    }

    private OperationMetadata createMetaDataCache(OperationParseContext context, AnnotatedElement annotatedElement) {
        List<AssembleOperation> assembleOperations = parseAssembleOperations(context, annotatedElement)
            .stream()
            .sorted(operationComparator)
            .collect(Collectors.toList());
        List<DisassembleOperation> disassembleOperations = parseDisassembleOperations(context, annotatedElement)
            .stream()
            .sorted(operationComparator)
            .collect(Collectors.toList());
        return new OperationMetadata(assembleOperations, disassembleOperations);
    }

    /**
     * Parse assemble operations for class.
     *
     * @param context  context
     * @param element annotated element
     * @return {@link AssembleOperation}
     */
    protected List<AssembleOperation> parseAssembleOperations(OperationParseContext context, AnnotatedElement element) {
        return Collections.emptyList();
    }

    /**
     * Parse disassemble operations for class.
     *
     * @param context context
     * @param element annotated element
     * @return {@link DisassembleOperation}
     */
    protected List<DisassembleOperation> parseDisassembleOperations(OperationParseContext context, AnnotatedElement element) {
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

    /**
     * Annotation metadata cache on class.
     */
    @RequiredArgsConstructor
    protected static class OperationMetadata {
        private final List<AssembleOperation> assembleOperations;
        private final List<DisassembleOperation> disassembleOperations;
        public void append(BeanOperations beanOperations) {
            assembleOperations.forEach(beanOperations::addAssembleOperations);
            disassembleOperations.forEach(beanOperations::addDisassembleOperations);
        }
    }
}
