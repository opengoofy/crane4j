package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.ParameterConvertibleMethodInvoker;
import cn.crane4j.core.util.ReflectUtils;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link PropertyOperator} abstract implementation class.
 *
 * @author huangchengxing
 */
@AllArgsConstructor
public class ReflectivePropertyOperator implements PropertyOperator {

    /**
     * converter register
     */
    @Nullable
    @Setter
    protected ConverterManager converterManager;

    /**
     * Get getter method.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @return getter method
     */
    @Nullable
    @Override
    public MethodInvoker findGetter(Class<?> targetType, String propertyName) {
        Method method = findGetterMethod(targetType, propertyName);
        return resolveInvoker(targetType, propertyName, method);
    }

    /**
     * Get setter method.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @return setter method
     */
    @Nullable
    @Override
    public MethodInvoker findSetter(Class<?> targetType, String propertyName) {
        Method method = findSetterMethod(targetType, propertyName);
        return resolveInvoker(targetType, propertyName, method);
    }

    /**
     * Find setter method by given type and field name.
     *
     * @param targetType target type
     * @param propertyName property name
     * @return setter method
     */
    @Nullable
    protected Method findSetterMethod(Class<?> targetType, String propertyName) {
        return Optional.ofNullable(ReflectUtils.getField(targetType, propertyName))
                .map(field -> ReflectUtils.findSetterMethod(targetType, field))
                .flatMap(Function.identity())
                .orElse(null);
    }

    /**
     * Find getter method by given type and field name.
     *
     * @param targetType target type
     * @param propertyName property name
     * @return getter method
     */
    @Nullable
    protected Method findGetterMethod(Class<?> targetType, String propertyName) {
        return Optional.ofNullable(ReflectUtils.getField(targetType, propertyName))
                .map(field -> ReflectUtils.findGetterMethod(targetType, field))
                .flatMap(Function.identity())
                .orElse(null);
    }

    /**
     * Create {@link MethodInvoker} according to the specified method.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @param method       getter method or setter method
     * @return {@link MethodInvoker}
     */
    @Nullable
    protected MethodInvoker createInvoker(Class<?> targetType, String propertyName, Method method) {
        return ReflectiveMethodInvoker.create(null, method, false);
    }

    /**
     * Resolve the invoker which finally to used.
     *
     * @param targetType target type
     * @param propertyName property name
     * @param method method
     * @return method invoker comparator
     * @see #createInvoker
     * @see ParameterConvertibleMethodInvoker
     */
    @Nullable
    protected final MethodInvoker resolveInvoker(Class<?> targetType, String propertyName, @Nullable Method method) {
        if (Objects.isNull(method)) {
            return null;
        }
        MethodInvoker invoker = createInvoker(targetType, propertyName, method);
        if (Objects.isNull(invoker)) {
            return null;
        }
        if (Objects.isNull(converterManager)) {
            return invoker;
        }
        return ParameterConvertibleMethodInvoker.create(invoker, converterManager, method.getParameterTypes());
    }
}
