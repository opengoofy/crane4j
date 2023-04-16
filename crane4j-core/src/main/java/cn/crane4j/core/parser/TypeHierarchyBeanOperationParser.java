package cn.crane4j.core.parser;

import cn.crane4j.core.exception.OperationParseException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.ReflectUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final Map<AnnotatedElement, BeanOperations> resolvedTypes = new ConcurrentHashMap<>(32);
    private final Map<AnnotatedElement, BeanOperations> currentlyInParsing = new LinkedHashMap<>(16);

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

    /**
     * Create {@link BeanOperations} instance
     *
     * @param element element
     * @return {@link BeanOperations}
     */
    protected BeanOperations createBeanOperations(AnnotatedElement element) {
        return new SimpleBeanOperations(element);
    }

    private BeanOperations parseIfNecessary(AnnotatedElement element) {
        BeanOperations beanOperations = resolvedTypes.get(element);
        if (Objects.isNull(beanOperations)) {
            synchronized (this) {
                // target is parsed ?
                beanOperations = resolvedTypes.get(element);
                if (Objects.isNull(beanOperations)) {
                    // target is in parsing?
                    beanOperations = currentlyInParsing.get(element);
                    // target need parse, do it!
                    if (Objects.isNull(beanOperations)) {
                        beanOperations = createBeanOperations(element);
                        OperationParseContext context = new OperationParseContext(beanOperations, this);
                        beanOperations.setActive(false);
                        currentlyInParsing.put(element, beanOperations);
                        doParse(element, context);
                        resolvedTypes.put(element, currentlyInParsing.remove(element));
                        beanOperations.setActive(true);
                    }
                }
            }
        }
        return beanOperations;
    }

    private void doParse(AnnotatedElement element, OperationParseContext context) {
        log.debug("parse operations from element [{}]", element);
        // parse from type hierarchy
        if (element instanceof Class) {
            doParseForType((Class<?>)element, context);
        }
        // parse method and overwrite method from type hierarchy
        else if (element instanceof Method){
            doParseForMethod((Method)element, context);
        }
        // parse for other type
        else {
            operationAnnotationResolvers.forEach(resolver -> resolver.resolve(context, element));
        }
    }

    private void doParseForType(Class<?> beanType, OperationParseContext context) {
        ReflectUtils.traverseTypeHierarchy(
            beanType, type -> operationAnnotationResolvers.forEach(resolver -> resolver.resolve(context, type))
        );
    }

    private void doParseForMethod(Method method, OperationParseContext context) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        ReflectUtils.traverseTypeHierarchy(
            method.getDeclaringClass(), type -> {
                Method targetMethod = ReflectUtils.getDeclaredMethod(type, methodName, parameterTypes);
                if (Objects.nonNull(targetMethod)) {
                    operationAnnotationResolvers.forEach(resolver -> resolver.resolve(context, targetMethod));
                }
            }
        );
    }
}
