package cn.crane4j.core.support.operator;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.ParameterConvertibleMethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A Default factory that creates proxy method.
 *
 * @author huangchengxing
 * @since  1.3.0
 */
@RequiredArgsConstructor
public class DefaultProxyMethodFactory implements OperatorProxyFactory.ProxyMethodFactory {

    private final ConverterManager converterManager;

    /**
     * Get operator proxy method.
     *
     * @param beanOperations        bean operations
     * @param method with at least one parameter
     * @param beanOperationExecutor bean operation executor
     * @return operator proxy method if supported, null otherwise
     */
    @Nullable
    @Override
    public MethodInvoker get(BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
        MethodInvoker invoker = new ProxyMethod(beanOperations, beanOperationExecutor);
        return ParameterConvertibleMethodInvoker.create(invoker, converterManager, method.getParameterTypes());
    }
    /**
     * Standard operator method.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    private static class ProxyMethod implements MethodInvoker {

        private final BeanOperations operations;
        private final BeanOperationExecutor beanOperationExecutor;

        @Override
        public Object invoke(Object target, Object... args) {
            Object arg = args[0];
            if (Objects.nonNull(arg)) {
                beanOperationExecutor.execute(CollectionUtils.adaptObjectToCollection(arg), operations);
            }
            return arg;
        }
    }
}
