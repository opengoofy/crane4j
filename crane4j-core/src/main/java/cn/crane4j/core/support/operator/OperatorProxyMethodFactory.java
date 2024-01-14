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
 * @see OperationAnnotationProxyMethodFactory
 * @see DynamicContainerOperatorProxyMethodFactory
 * @see ParametersFillProxyMethodFactory
 * @see ArgAutoOperateProxyMethodFactory
 * @since 1.3.0
 */
public interface OperatorProxyMethodFactory extends Sorted {

    int OPERATION_ANNOTATION_PROXY_METHOD_FACTORY_ORDER = 0;
    int DYNAMIC_CONTAINER_OPERATOR_PROXY_METHOD_FACTORY_ORDER = 1;
    int ARG_AUTO_OPERATE_PROXY_METHOD_FACTORY_ORDER = 2;
    int PARAMETERS_FILL_PROXY_METHOD_FACTORY_ORDER = 3;

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
