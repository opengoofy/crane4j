package cn.crane4j.core.parser;

import cn.crane4j.core.exception.OperationParseException;
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
 * as the context for this execution, Then successively call all registered {@link BeanOperationsResolver}
 * to collect the configuration information into the {@link BeanOperations} in context.
 *
 * <p>After the parsing is completed, the {@link BeanOperations} instance
 * corresponding to the {@link Class} will be cached,
 * and the cache will be used preferentially for the next access.
 *
 * @author huangchengxing
 * @see BeanOperationsResolver
 * @see OperationParseContext
 * @since 1.2.0
 */
@Slf4j
public class TypeHierarchyBeanOperationParser implements BeanOperationParser {

    protected Set<BeanOperationsResolver> beanOperationsResolvers;
    private final Map<Class<?>, BeanOperations> resolvedTypes = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, BeanOperations> currentlyInParsing = new LinkedHashMap<>(16);

    /**
     * Create a {@link TypeHierarchyBeanOperationParser} instance.
     *
     * @param beanOperationsResolvers beanOperationsResolvers
     */
    public TypeHierarchyBeanOperationParser(
        Collection<BeanOperationsResolver> beanOperationsResolvers) {
        this.beanOperationsResolvers = beanOperationsResolvers.stream()
            .sorted(Sorted.comparator())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Add bean operations resolvers.
     *
     * @param resolver resolver
     */
    public void addBeanOperationsResolver(BeanOperationsResolver resolver) {
        Objects.requireNonNull(resolver);
        if (!beanOperationsResolvers.contains(resolver)) {
            beanOperationsResolvers.add(resolver);
            this.beanOperationsResolvers = beanOperationsResolvers.stream()
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
        beanOperationsResolvers.forEach(resolver -> resolver.resolve(context, type));
    }
}
