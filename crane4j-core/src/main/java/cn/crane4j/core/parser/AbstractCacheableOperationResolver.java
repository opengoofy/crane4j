package cn.crane4j.core.parser;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.exception.OperationParseException;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An abstract {@link BeanOperationsResolver}
 * implementation that cacheable、 annotation aware and support operation compare.
 *
 * @author huangchengxing
 * @since 1.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCacheableOperationResolver implements BeanOperationsResolver {

    private final Map<Class<?>, TypeOperationMetadata> metadataCaches = CollectionUtils.newWeakConcurrentMap();
    protected final AnnotationFinder annotationFinder;
    protected final Comparator<KeyTriggerOperation> operationComparator;

    /**
     * <p>Resolve operations from type.<br />
     * If there is a cache, it will be obtained from the cache first.
     *
     * @param context context in parsing
     * @param type bean type
     * @throws OperationParseException thrown when configuration resolution exception
     */
    @Override
    public void resolve(OperationParseContext context, Class<?> type) {
        BeanOperations target = context.getRootOperations();
        log.debug("resolve operations from [{}]", type);
        TypeOperationMetadata cache = MapUtil.computeIfAbsent(
            metadataCaches, type, t -> createMetaDataCache(context, t)
        );
        cache.append(target);
    }

    private TypeOperationMetadata createMetaDataCache(OperationParseContext context, Class<?> type) {
        List<AssembleOperation> assembleOperations = parseAssembleOperations(context, type)
            .stream()
            .sorted(operationComparator)
            .collect(Collectors.toList());
        List<DisassembleOperation> disassembleOperations = parseDisassembleOperations(context, type)
            .stream()
            .sorted(operationComparator)
            .collect(Collectors.toList());
        return new TypeOperationMetadata(assembleOperations, disassembleOperations);
    }

    /**
     * Parse assemble operations for class.
     *
     * @param context context
     * @param beanType bean type
     * @return {@link AssembleOperation}
     * @see #parseAnnotationForDeclaredFields
     */
    protected abstract List<AssembleOperation> parseAssembleOperations(OperationParseContext context, Class<?> beanType);

    /**
     * Parse disassemble operations for class.
     *
     * @param context context
     * @param beanType bean type
     * @return {@link DisassembleOperation}
     * @see #parseAnnotationForDeclaredFields
     */
    protected abstract List<DisassembleOperation> parseDisassembleOperations(OperationParseContext context, Class<?> beanType);

    /**
     * Parse annotations for declared fields of {@code beanType}.
     *
     * @param beanType bean type
     * @param annotationType annotation type
     * @param mapper mapper for annotation and field
     * @return annotations
     */
    protected final <T extends Annotation, R> List<R> parseAnnotationForDeclaredFields(
        Class<?> beanType, Class<T> annotationType, BiFunction<T, Field, R> mapper) {
        Field[] fields = ReflectUtils.getDeclaredFields(beanType);
        List<R> results = new ArrayList<>(fields.length);
        for (Field field : fields) {
            Set<T> annotation = annotationFinder.findAllAnnotations(field, annotationType);
            if (CollUtil.isEmpty(annotation)) {
                continue;
            }
            for (T t : annotation) {
                R r = mapper.apply(t, field);
                results.add(r);
            }
        }
        return results;
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
    protected static class TypeOperationMetadata {
        private final List<AssembleOperation> assembleOperations;
        private final List<DisassembleOperation> disassembleOperations;
        public void append(BeanOperations beanOperations) {
            assembleOperations.forEach(beanOperations::addAssembleOperations);
            disassembleOperations.forEach(beanOperations::addDisassembleOperations);
        }
    }
}
