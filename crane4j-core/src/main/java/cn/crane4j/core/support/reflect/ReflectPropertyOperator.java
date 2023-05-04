package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A {@link PropertyOperator} implementation based on JDK reflection.
 *
 * @author huangchengxing
 */
public class ReflectPropertyOperator extends CacheablePropertyOperator {

    /**
     * Create an {@link ReflectPropertyOperator} instance
     *
     * @param converterManager converter register
     */
    public ReflectPropertyOperator(ConverterManager converterManager) {
        super(converterManager);
    }

    /**
     * Create {@link MethodInvoker} according to the specified method
     *
     * @param targetType target type
     * @param propertyName property name
     * @param method getter method or setter method
     * @return {@link MethodInvoker}
     */
    @Override
    protected MethodInvoker createInvoker(Class<?> targetType, String propertyName, Method method) {
        Objects.requireNonNull(method);
        return ReflectMethodInvoker.create(null, method, false);
    }
}
