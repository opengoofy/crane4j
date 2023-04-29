package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.ProvideData;
import cn.crane4j.core.container.DynamicSourceContainerProvider;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.DataProvider;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A factory that creates proxy method that supports the {@link ProvideData} annotation.
 *
 * @author huangchengxing
 * @see ProvideData
 * @see DynamicSourceContainerProvider
 * @since  1.3.0
 */
@RequiredArgsConstructor
public class DynamicSourceProxyMethodFactory implements OperatorProxyFactory.ProxyMethodFactory {

    public static final int ORDER = 0;
    private final AnnotationFinder annotationFinder;
    private final ParameterNameFinder parameterNameFinder;
    private final DynamicSourceContainerProvider dynamicSourceContainerProvider;
    private final boolean clearContextAfterInvoke;

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    @Override
    public int getSort() {
        return ORDER;
    }

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
        Map<String, Parameter> parameterMap = ReflectUtils.resolveParameterNames(parameterNameFinder, method);
        DataProviderFactory[] dataProviderFactories = new DataProviderFactory[parameterMap.size()];
        int paramIndex = 0;
        boolean noneContainerParameters = true;
        for (Map.Entry<String, Parameter> entry : parameterMap.entrySet()) {
            String parameterName = entry.getKey();
            Parameter parameter = entry.getValue();
            // has parameter annotated by @ProvideData?
            ProvideData annotation = annotationFinder.getAnnotation(parameter, ProvideData.class);
            if (Objects.nonNull(annotation)) {
                noneContainerParameters = false;
                String namespace = StringUtils.emptyToDefault(annotation.value(), parameterName);
                Class<?> parameterType = parameter.getType();
                dataProviderFactories[paramIndex] = resolveParameter(namespace, parameterType);
            }
            paramIndex++;
        }
        return noneContainerParameters ? null : new ProxyMethod(
            beanOperations, beanOperationExecutor, dataProviderFactories, dynamicSourceContainerProvider, clearContextAfterInvoke
        );
    }

    /**
     * Resolve container parameter for the given namespace and parameter type to a {@link DataProviderFactory} instance.
     *
     * @param namespace namespace
     * @param parameterType parameter type
     * @return {@link DataProviderFactory} instance.
     */
    @SuppressWarnings("unchecked")
    protected DataProviderFactory resolveParameter(String namespace, Class<?> parameterType) {
        if (Map.class.isAssignableFrom(parameterType)) {
            return new DataProviderFactory(
                namespace, arg -> DataProvider.fixed((Map<Object, Object>)arg)
            );
        } else if (DataProvider.class.isAssignableFrom(parameterType)) {
            return new DataProviderFactory(namespace, arg -> (DataProvider<Object, Object>)arg);
        }
        throw new Crane4jException("Unsupported container parameter type: " + parameterType.getName());
    }

    /**
     * Operator method that support container parameter.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    private static class ProxyMethod implements MethodInvoker {

        private final BeanOperations operations;
        private final BeanOperationExecutor beanOperationExecutor;
        private final DataProviderFactory[] dataProviderFactories;
        private final DynamicSourceContainerProvider dynamicSourceContainerProvider;
        private final boolean clearContextAfterInvoke;

        @Override
        public Object invoke(Object target, Object... args) {
            Object targetObject = args[0];
            if (Objects.nonNull(targetObject)) {
                doInvoke(targetObject, args);
            }
            return targetObject;
        }

        private void doInvoke(Object targetObject, Object[] args) {
            // set container data for context
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                DataProviderFactory parameter = dataProviderFactories[i];
                if (Objects.nonNull(parameter) && Objects.nonNull(arg)) {
                    dynamicSourceContainerProvider.setDataProvider(
                        parameter.getNamespace(), parameter.getFactory().apply(arg)
                    );
                }
            }
            // execute operation
            beanOperationExecutor.execute(CollectionUtils.adaptObjectToCollection(targetObject), operations);
            // clear container data for context if necessary
            if (clearContextAfterInvoke) {
                for (DataProviderFactory parameter : dataProviderFactories) {
                    if (Objects.nonNull(parameter)) {
                        dynamicSourceContainerProvider.removeDataProvider(parameter.getNamespace());
                    }
                }
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class DataProviderFactory {
        private final String namespace;
        private final Function<Object, DataProvider<Object, Object>> factory;
    }
}
