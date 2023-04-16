package cn.crane4j.core.support;

import cn.crane4j.annotation.Operator;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * An operator proxy object factory that to generate proxy objects
 * that can call specific annotation marked interface methods.
 *
 * @author huangchengxing
 * @see Operator
 */
@Slf4j
@RequiredArgsConstructor
public class OperatorProxyFactory {

    private static final Object NULL = new Object();
    private final Crane4jGlobalConfiguration globalConfiguration;
    private final AnnotationFinder annotationFinder;
    private final Map<Class<?>, Object> proxyCaches = new ConcurrentHashMap<>(8);

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
        Assert.isTrue(
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
        BeanOperationExecutor executor = ConfigurationUtil.getOperationExecutor(
            globalConfiguration, annotation.executorName(), annotation.executor()
        );
        Assert.notNull(executor, "the executor of the operator [{}] is not found.", operatorType.getName());
        BeanOperationParser parser = ConfigurationUtil.getOperationParser(
            globalConfiguration, annotation.parserName(), annotation.parser()
        );
        Assert.notNull(parser, "the parser of the operator [{}] is not found.", operatorType.getName());

        // create proxy by executor and parser
        log.debug("create operator proxy for interface [{}].", operatorType);
        OperatorProxy proxy = createOperatorProxy(operatorType, parser, executor);
        return Proxy.newProxyInstance(operatorType.getClassLoader(), new Class[] {operatorType}, proxy);
    }

    private <T> OperatorProxy createOperatorProxy(
        Class<T> operatorType, BeanOperationParser beanOperationParser, BeanOperationExecutor beanOperationExecutor) {
        Map<String, BeanOperations> beanOperationsMap = new HashMap<>(8);
        ReflectUtils.traverseTypeHierarchy(operatorType, type -> Stream
            .of(ReflectUtils.getDeclaredMethods(type))
            .map(beanOperationParser::parse)
            .peek(this::checkOperationOfMethod)
            .forEach(operations -> beanOperationsMap.put(((Method)operations.getSource()).getName(), operations))
        );
        return new OperatorProxy(beanOperationsMap, beanOperationExecutor);
    }

    private void checkOperationOfMethod(BeanOperations operations) {
        Method method = (Method)operations.getSource();
        if (method.isDefault()) {
            return;
        }
        if (method.getParameterCount() < 1) {
            throw new Crane4jException(
                "the method [{}] is not a default method, but the parameter count is less than 1.", method.getName()
            );
        }
        if (operations.getDisassembleOperations().isEmpty()
            && operations.getAssembleOperations().isEmpty()) {
            throw new Crane4jException(
                "the method [{}] is not a default method, but there are no executable operations found.", method.getName()
            );
        }
    }

    @RequiredArgsConstructor
    private static class OperatorProxy implements InvocationHandler {

        private final Map<String, BeanOperations> operationsOfMethod;
        private final BeanOperationExecutor beanOperationExecutor;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            BeanOperations operations = operationsOfMethod.get(method.getName());
            return Objects.isNull(operations) ? method.invoke(proxy, args) : operate(operations, args);
        }

        private Object operate(BeanOperations beanOperations, Object[] args) {
            if (args == null || args.length == 0) {
                return null;
            }
            Object target = args[0];
            if (Objects.nonNull(target)) {
                beanOperationExecutor.execute(CollectionUtils.adaptObjectToCollection(target), beanOperations);
            }
            return target;
        }
    }
}
