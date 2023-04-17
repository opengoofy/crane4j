package cn.crane4j.core.support.operator;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
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

    /**
     * Get operator proxy method.
     *
     * @param beanOperations        bean operations
     * @param method                method
     * @param beanOperationExecutor bean operation executor
     * @return operator proxy method if supported, null otherwise
     */
    @Nullable
    @Override
    public MethodInvoker get(BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
        return new ProxyMethod(beanOperations, beanOperationExecutor);
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
