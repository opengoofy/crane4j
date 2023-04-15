package cn.crane4j.core.parser;

import cn.crane4j.core.exception.OperationParseException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.support.Sorted;
import cn.hutool.core.collection.CollUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>General implementation of {@link BeanOperationParser}.
 *
 * <p>When parsing the configuration, the parser will create a {@link OperationParseContext}
 * as the context for this execution, Then successively call all registered {@link OperationAnnotationResolver}
 * to collect the configuration information into the {@link BeanOperations} in context.
 *
 * <p>After the parsing is completed, the {@link BeanOperations} instance
 * corresponding to the {@link Class} will be cached,
 * and the cache will be used preferentially for the next access.
 *
 * <p>The sequence of operations obtained through the parser follows:
 * <ul>
 *     <li>The calling order of {@link OperationAnnotationResolver};</li>
 *     <li>their order in link {@link OperationAnnotationResolver};</li>
 * </ul>
 * It should be noted that this order does not represent the order in which the final operation will be executed.
 * This order is guaranteed by the executor {@link BeanOperationExecutor}.
 *
 * @author huangchengxing
 * @see OperationAnnotationResolver
 * @see OperationParseContext
 * @since 1.2.0
 */
@Slf4j
public class TypeHierarchyBeanOperationParser implements BeanOperationParser {

    protected Set<OperationAnnotationResolver> operationAnnotationResolvers;
    private final Map<Class<?>, BeanOperations> resolvedTypes = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, BeanOperations> currentlyInParsing = new LinkedHashMap<>(16);

    /**
     * Create a {@link TypeHierarchyBeanOperationParser} instance.
     *
     * @param operationAnnotationResolvers operationAnnotationResolvers
     */
    public TypeHierarchyBeanOperationParser(
        Collection<OperationAnnotationResolver> operationAnnotationResolvers) {
        this.operationAnnotationResolvers = operationAnnotationResolvers.stream()
            .sorted(Sorted.comparator())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Add bean operations resolvers.
     *
     * @param resolver resolver
     */
    public void addBeanOperationsResolver(OperationAnnotationResolver resolver) {
        Objects.requireNonNull(resolver);
        if (!operationAnnotationResolvers.contains(resolver)) {
            operationAnnotationResolvers.add(resolver);
            this.operationAnnotationResolvers = operationAnnotationResolvers.stream()
                .sorted(Sorted.comparator())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    /**
     * <p>Parse the class and class attribute information,
     * and generate the corresponding {@link BeanOperations} instance.<br />
     * If there is a cache, it will be obtained from the cache first.
     *
     * <p><b>NOTE:</b>The {@link BeanOperations} obtained may still be being parsed.
     * Please confirm whether it is ready through {@link BeanOperations#isActive()}.
     *
     * @param beanType bean type
     * @return {@link BeanOperations}
     * @throws OperationParseException thrown when configuration resolution exception
     */
    @NonNull
    @Override
    public BeanOperations parse(Class<?> beanType) throws OperationParseException {
        Objects.requireNonNull(beanType);
        try {
            return parseIfNecessary(beanType);
        } catch (Exception e) {
            throw new OperationParseException(e);
        }
    }

    /**
     * Create {@link BeanOperations} instance
     *
     * @param beanType bean type
     * @return {@link BeanOperations}
     */
    protected BeanOperations createBeanOperations(Class<?> beanType) {
        return new SimpleBeanOperations(beanType);
    }

    private BeanOperations parseIfNecessary(Class<?> beanType) {
        BeanOperations beanOperations = resolvedTypes.get(beanType);
        if (Objects.isNull(beanOperations)) {
            synchronized (this) {
                // target is parsed ?
                beanOperations = resolvedTypes.get(beanType);
                if (Objects.isNull(beanOperations)) {
                    // target is in parsing?
                    beanOperations = currentlyInParsing.get(beanType);
                    // target need parse, do it!
                    if (Objects.isNull(beanOperations)) {
                        beanOperations = createBeanOperations(beanType);
                        OperationParseContext context = new OperationParseContext(beanOperations, this);
                        beanOperations.setActive(false);
                        currentlyInParsing.put(beanType, beanOperations);
                        doParse(beanType, context);
                        resolvedTypes.put(beanType, currentlyInParsing.remove(beanType));
                        beanOperations.setActive(true);
                    }
                }
            }
        }
        return beanOperations;
    }

    private void doParse(Class<?> beanType, OperationParseContext context) {
        log.debug("parse operations from [{}]", beanType);
        Set<Class<?>> accessed = new HashSet<>();
        Deque<Class<?>> typeQueue = new LinkedList<>();
        typeQueue.add(beanType);

        while (!typeQueue.isEmpty()) {
            Class<?> type = typeQueue.removeFirst();
            accessed.add(type);
            // resolve operations for current type
            resolveBeanOperations(context, type);
            // then find superclass and interfaces
            Class<?> superclass = type.getSuperclass();
            if (Objects.nonNull(superclass) && !Objects.equals(superclass, Object.class) && !accessed.contains(superclass)) {
                typeQueue.add(superclass);
            }
            CollUtil.addAll(typeQueue, type.getInterfaces());
        }
    }

    private void resolveBeanOperations(OperationParseContext context, Class<?> type) {
        operationAnnotationResolvers.forEach(resolver -> resolver.resolve(context, type));
    }
}
