package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.Operator;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An operator proxy object factory that to generate proxy objects
 * that can call specific annotation marked interface methods.
 *
 * @author huangchengxing
 * @see Operator
 * @see ProxyMethodFactory
 * @since 1.3.0
 */
@Slf4j
public class OperatorProxyFactory {

    private static final Object NULL = new Object();
    private final Crane4jGlobalConfiguration globalConfiguration;
    private final AnnotationFinder annotationFinder;
    private final Collection<ProxyMethodFactory> proxyMethodFactories;
    private final Map<Class<?>, Object> proxyCaches = new ConcurrentHashMap<>(8);

    /**
     * Create an {@link OperatorProxyFactory} instance.
     *
     * @param globalConfiguration global configuration
     * @param annotationFinder annotation finder
     * @param proxyMethodFactories proxy method factories
     */
    public OperatorProxyFactory(
        Crane4jGlobalConfiguration globalConfiguration, AnnotationFinder annotationFinder,
        Collection<ProxyMethodFactory> proxyMethodFactories) {
        this.globalConfiguration = globalConfiguration;
        this.annotationFinder = annotationFinder;
        this.proxyMethodFactories = proxyMethodFactories.stream()
            .distinct()
            .sorted(Crane4jGlobalSorter.instance())
            .collect(Collectors.toList());
    }

    /**
     * Get the proxy object of the specified operator interface that annotated by {@link Operator}.
     *
     * @param operatorType operator type
     * @return proxy object
     * @see Operator
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(Class<T> operatorType) {
        Asserts.isTrue(
            Objects.nonNull(operatorType) && operatorType.isInterface(),
            "the operator type [{}] must be an interface.", operatorType
        );
        Object proxy = CollectionUtils.computeIfAbsent(proxyCaches, operatorType, this::doGetProxy);
        return proxy == NULL ? null : (T) proxy;
    }

    private Object doGetProxy(Class<?> operatorType) {
        Operator annotation = annotationFinder.findAnnotation(operatorType, Operator.class);
        if (Objects.isNull(annotation)) {
            return NULL;
        }

        // get component of executor and parser
        BeanOperationExecutor executor = globalConfiguration.getBeanOperationExecutor(annotation.executor());
        Asserts.isNotNull(executor, "the executor of the operator [{}] is not found.", operatorType.getName());
        BeanOperationParser parser = globalConfiguration.getBeanOperationsParser(annotation.parser());
        Asserts.isNotNull(parser, "the parser of the operator [{}] is not found.", operatorType.getName());

        // create proxy by executor and parser
        log.debug("create operator proxy for interface [{}].", operatorType);
        OperatorProxy proxy = createOperatorProxy(operatorType, parser, executor);
        return Proxy.newProxyInstance(operatorType.getClassLoader(), new Class[] {operatorType}, proxy);
    }

    private <T> OperatorProxy createOperatorProxy(
        Class<T> operatorType, BeanOperationParser beanOperationParser, BeanOperationExecutor beanOperationExecutor) {
        Map<String, MethodInvoker> beanOperationsMap = new HashMap<>(8);
        ReflectUtils.traverseTypeHierarchy(operatorType, type -> Stream
            .of(ReflectUtils.getDeclaredMethods(type))
            .filter(method -> !method.isDefault())
            .map(beanOperationParser::parse)
            .forEach(operations -> {
                checkOperationOfMethod(operations);
                Method method = (Method)operations.getSource();
                MethodInvoker invoker = createOperatorMethod(operations, method, beanOperationExecutor);
                beanOperationsMap.put(method.getName(), invoker);
            })
        );
        return new OperatorProxy(beanOperationsMap);
    }

    private void checkOperationOfMethod(BeanOperations operations) {
        Method method = (Method)operations.getSource();
        if (method.getParameterCount() < 1) {
            throw new Crane4jException(
                "the method [{}] parameter count is less than 1.", method.getName()
            );
        }
        if (operations.isEmpty()) {
            throw new Crane4jException(
                "the method [{}] are no executable operations found.", method.getName()
            );
        }
    }

    private MethodInvoker createOperatorMethod(
        BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
        return proxyMethodFactories.stream()
            .map(factory ->factory.get(beanOperations, method, beanOperationExecutor))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new Crane4jException("cannot create proxy for method [{}]", method));
    }

    /**
     * Operator proxy.
     *
     * @author huangchengxing
     * @since  1.3.0
     */
    @RequiredArgsConstructor
    private static class OperatorProxy implements InvocationHandler {
        private final Map<String, MethodInvoker> proxiedMethods;
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodInvoker invoker = proxiedMethods.get(method.getName());
            return Objects.isNull(invoker) ? method.invoke(proxy, args) : invoker.invoke(proxy, args);
        }
    }

    /**
     * Operator proxy method factory.
     *
     * @author huangchengxing
     * @see DefaultProxyMethodFactory
     * @since  1.3.0
     */
    public interface ProxyMethodFactory extends Sorted {

        /**
         * Get operator proxy method.
         *
         * @param beanOperations bean operations
         * @param method method with at least one parameter
         * @param beanOperationExecutor bean operation executor
         * @return operator proxy method if supported, null otherwise
         */
        @Nullable
        MethodInvoker get(
            BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor);
    }
}
