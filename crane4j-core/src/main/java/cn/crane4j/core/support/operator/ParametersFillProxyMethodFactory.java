package cn.crane4j.core.support.operator;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.ArrayUtils;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;

/**
 * <p>An operator proxy method factory that supports filling parameters when calling method.
 *
 * @author huangchengxing
 * @since 2.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class ParametersFillProxyMethodFactory implements OperatorProxyMethodFactory {

    private final BeanOperationParser beanOperationParser;

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    @Override
    public int getSort() {
        return OperatorProxyMethodFactory.PARAMETERS_FILL_PROXY_METHOD_FACTORY_ORDER;
    }

    /**
     * Get operator proxy method.
     *
     * @param beanOperations        bean operations
     * @param method                method with at least one parameter
     * @param beanOperationExecutor bean operation executor
     * @return operator proxy method if supported, null otherwise
     */
    @Nullable
    @Override
    public MethodInvoker get(BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return null;
        }
        BeanOperations[] beanOperationsArray = new BeanOperations[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            BeanOperations bo = beanOperationParser.parse(parameterType);
            beanOperationsArray[i] = bo.isEmpty() ? null : bo;
        }
        return new ParameterFillMethodInvoker(beanOperationsArray, beanOperationExecutor);
    }

    @RequiredArgsConstructor
    private static class ParameterFillMethodInvoker implements MethodInvoker {
        private final BeanOperations[] beanOperations;
        private final BeanOperationExecutor executor;
        @Override
        public Object invoke(Object target, Object... args) {
            if (ArrayUtils.isNotEmpty(args)) {
                for (int i = 0; i < args.length; i++) {
                    doInvoke(args, i);
                }
            }
            return null;
        }

        private void doInvoke(Object[] args, int i) {
            Object arg = args[i];
            if (arg == null) {
                return;
            }
            BeanOperations bo = beanOperations[i];
            if (bo == null) {
                return;
            }
            executor.execute(CollectionUtils.adaptObjectToCollection(arg), bo);
        }
    }
}
