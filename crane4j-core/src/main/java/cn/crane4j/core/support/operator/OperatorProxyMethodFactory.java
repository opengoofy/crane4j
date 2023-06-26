package cn.crane4j.core.support.operator;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.Sorted;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;

/**
 * Operator proxy method factory.
 *
 * @author huangchengxing
 * @see DefaultOperatorProxyMethodFactory
 * @see DynamicContainerOperatorProxyMethodFactory
 * @since 1.3.0
 */
public interface OperatorProxyMethodFactory extends Sorted {

    /**
     * Get operator proxy method.
     *
     * @param beanOperations        bean operations
     * @param method                method with at least one parameter
     * @param beanOperationExecutor bean operation executor
     * @return operator proxy method if supported, null otherwise
     */
    @Nullable MethodInvoker get(BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor);
}
