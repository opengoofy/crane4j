package cn.crane4j.core.parser;

import cn.crane4j.core.exception.OperationParseException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.handler.OperationAnnotationHandler;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>General implementation of {@link BeanOperationParser}.
 *
 * <p>When parsing the configuration, the parser will create a root {@link BeanOperations}
 * as the context for this execution, Then successively call all registered {@link OperationAnnotationHandler}
 * to collect the configuration information into the {@link BeanOperations} in context.
 *
 * <p>After the parsing is completed, the {@link BeanOperations} instance
 * corresponding to the {@link AnnotatedElement} will be cached,
 * and the cache will be used preferentially for the next access.
 *
 * <p>When parsing element, if it is a:
 * <ul>
 *     <li>{@link Class}: it will check all parent classes and interfaces in its hierarchy;</li>
 *     <li>{@link Method}: it will check for methods with the same method signature in all parent classes and interfaces in its hierarchy;</li>
 *     <li>other: only the itself will be checked;</li>
 * </ul>
 *
 * <p>The sequence of operations obtained through the parser follows:
 * <ul>
 *     <li>The calling order of {@link OperationAnnotationHandler};</li>
 *     <li>their order in link {@link OperationAnnotationHandler};</li>
 * </ul>
 * It should be noted that this order does not represent the order in which the final operation will be executed.
 * This order is guaranteed by the executor {@link BeanOperationExecutor}.
 *
 * @author huangchengxing
 * @see OperationAnnotationHandler
 * @since 1.2.0
 */
@Slf4j
public class TypeHierarchyBeanOperationParser implements BeanOperationParser {

    /**
     * temp cache for operations of element that currently in parsing
     */
    protected final Map<AnnotatedElement, BeanOperations> currentlyInParsing = new LinkedHashMap<>(8);
    
    /**
     * temp cache for operations of resolved element where in type hierarchy.
     */
    protected Map<AnnotatedElement, BeanOperations> resolvedHierarchyElements = CollectionUtils.newWeakConcurrentMap();

    /**
     *  finally cache for operations of resolved element.
     */
    protected final Map<AnnotatedElement, BeanOperations> resolvedElements = new ConcurrentHashMap<>(64);

    /**
     * registered operation annotation resolvers.
     */
    protected Set<OperationAnnotationHandler> operationAnnotationHandlers;

    /**
     * Create a {@link TypeHierarchyBeanOperationParser} instance.
     *
     * @param operationAnnotationHandlers operationAnnotationHandlers
     */
    public TypeHierarchyBeanOperationParser(
        Collection<OperationAnnotationHandler> operationAnnotationHandlers) {
        this.operationAnnotationHandlers = operationAnnotationHandlers.stream()
            .sorted(Sorted.comparator())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Add bean operations resolvers.
     *
     * @param resolver handler
     */
    public void addBeanOperationsResolver(OperationAnnotationHandler resolver) {
        Objects.requireNonNull(resolver);
        if (!operationAnnotationHandlers.contains(resolver)) {
            operationAnnotationHandlers.add(resolver);
            this.operationAnnotationHandlers = operationAnnotationHandlers.stream()
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
     * @param element element to parse
     * @return {@link BeanOperations}
     * @throws OperationParseException thrown when configuration resolution exception
     */
    @NonNull
    @Override
    public BeanOperations parse(AnnotatedElement element) throws OperationParseException {
        Objects.requireNonNull(element);
        try {
            return parseIfNecessary(element);
        } catch (Exception e) {
            throw new OperationParseException(e);
        }
    }

    private BeanOperations parseIfNecessary(AnnotatedElement element) {
        BeanOperations result = resolvedElements.get(element);
        if (Objects.isNull(result)) {
            synchronized (this) {
                // target is parsed ?
                result = resolvedElements.get(element);
                if (Objects.isNull(result)) {
                    // target is in parsing?
                    result = currentlyInParsing.get(element);
                    // target need parse, do it!
                    if (Objects.isNull(result)) {
                        result = createBeanOperations(element);
                        result.setActive(false);
                        currentlyInParsing.put(element, result);
                        doParse(result);
                        resolvedElements.put(element, currentlyInParsing.remove(element));
                        result.setActive(true);
                    }
                }
            }
        }
        return result;
    }

    private void doParse(BeanOperations root) {
        AnnotatedElement source = root.getSource();
        log.debug("parse operations from element [{}]", source);

        // collected resolve operation from hierarchy of source
        Collection<BeanOperations> resolvedOperations;
        if (source instanceof Class) {
            // parse from type hierarchy
            resolvedOperations = doParseForType((Class<?>)source);
        }
        else if (source instanceof Method){
            // parse method and overwrite method from type hierarchy
            resolvedOperations = doParseForMethod((Method)source);
        }
        else {
            // parse for other type
            resolvedOperations = doParseForElement(source);
        }
        // TODO: all operations need sort again?
        resolvedOperations.forEach(op -> {
            op.getAssembleOperations().forEach(root::addAssembleOperations);
            op.getDisassembleOperations().forEach(root::addDisassembleOperations);
        });
    }

    /**
     * Create {@link BeanOperations} instance
     *
     * @param element element
     * @return {@link BeanOperations}
     */
    protected BeanOperations createBeanOperations(AnnotatedElement element) {
        return new SimpleBeanOperations(element);
    }

    /**
     * Parse operations form hierarchy of {@code element}.
     *
     * @param element element
     * @return operations form hierarchy of {@code element}
     * @see #resolveToOperations
     */
    protected Collection<BeanOperations> doParseForElement(AnnotatedElement element) {
        BeanOperations current = resolveToOperations(element);
        return Collections.singletonList(current);
    }

    /**
     * Parse operations form type hierarchy of {@code beanType}.
     *
     * @param beanType bean type
     * @return operations form type hierarchy of {@code beanType}
     * @see #resolveToOperations
     */
    protected Collection<BeanOperations> doParseForType(Class<?> beanType) {
        List<BeanOperations> results = new ArrayList<>();
        ReflectUtils.traverseTypeHierarchy(beanType, type -> {
            // current type is already resolved?
            BeanOperations current = resolveToOperations(type);
            results.add(current);
        });
        return results;
    }

    /**
     * Parse operations form method where in type hierarchy of {@code beanType}.
     *
     * @param method method
     * @return operations form method where in type hierarchy of {@code beanType}
     * @see #resolveToOperations
     */
    protected Collection<BeanOperations> doParseForMethod(Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        List<BeanOperations> results = new ArrayList<>();
        ReflectUtils.traverseTypeHierarchy(method.getDeclaringClass(), type -> {
            Method targetMethod = ReflectUtils.getDeclaredMethod(type, methodName, parameterTypes);
            if (Objects.nonNull(targetMethod)) {
                BeanOperations current = resolveToOperations(targetMethod);
                results.add(current);
            }
        });
        return results;
    }

    /**
     * Parse {@link BeanOperations} from {@code source} if necessary.
     *
     * @param source source
     * @return operations from source, may come from cache
     */
    protected final BeanOperations resolveToOperations(AnnotatedElement source) {
        return CollectionUtils.computeIfAbsent(resolvedHierarchyElements, source, s -> {
            if (ReflectUtils.isJdkElement(s)) {
                return BeanOperations.EmptyBeanOperations.INSTANCE;
            }
            BeanOperations operations = createBeanOperations(source);
            operationAnnotationHandlers.forEach(resolver -> resolver.resolve(this, operations));
            return operations;
        });
    }
}
