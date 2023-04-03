package cn.crane4j.core.parser;

import cn.crane4j.core.exception.OperationParseException;
import cn.crane4j.core.support.Sorted;
import cn.hutool.core.collection.CollUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
 */
@Slf4j
public class TypeHierarchyBeanOperationParser implements BeanOperationParser {

    protected final Collection<BeanOperationsResolver> beanOperationsResolvers;
    private final Map<Class<?>, BeanOperations> resolvedTypes = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, BeanOperations> currentlyInCreation = new LinkedHashMap<>(16);

    /**
     * Create a {@link TypeHierarchyBeanOperationParser} instance.
     *
     * @param beanOperationsResolvers beanOperationsResolvers
     */
    public TypeHierarchyBeanOperationParser(
        Collection<BeanOperationsResolver> beanOperationsResolvers) {
        this.beanOperationsResolvers = CollUtil.sort(beanOperationsResolvers, Sorted.comparator());
    }

    /**
     * <p>Parse the class and class attribute information,
     * and generate the corresponding {@link BeanOperations} instance.<br />
     * If there is a cache, it will be obtained from the cache first.
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
                // target is resolved ?
                beanOperations = resolvedTypes.get(beanType);
                if (Objects.isNull(beanOperations)) {
                    // target is in creation?
                    beanOperations = currentlyInCreation.get(beanType);
                    // target need create
                    if (Objects.isNull(beanOperations)) {
                        beanOperations = createBeanOperations(beanType);
                        OperationParseContext context = new OperationParseContext(beanOperations, resolvedTypes, this);
                        beanOperations.setActive(false);
                        currentlyInCreation.put(beanType, beanOperations);
                        doParse(beanType, context);
                        resolvedTypes.put(beanType, currentlyInCreation.remove(beanType));
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
