package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Property operator, used to read and write object attributes.
 *
 * @author huangchengxing
 * @see AsmReflectivePropertyOperator
 * @see ReflectivePropertyOperator
 */
public interface PropertyOperator {

    /**
     * Get the specified property value.
     *
     * @param target       target
     * @param targetType   target type
     * @param propertyName property name
     * @return property value
     */
    @Nullable
    default Object readProperty(Class<?> targetType, Object target, String propertyName) {
        MethodInvoker getter = findGetter(targetType, propertyName);
        return Objects.isNull(getter) ? null : getter.invoke(target);
    }

    /**
     * Get getter method.
     *
     * @param targetType target type
     * @param propertyName property name
     * @return getter method
     */
    @Nullable
    MethodInvoker findGetter(Class<?> targetType, String propertyName);

    /**
     * Set the specified property value.
     *
     * @param target       target
     * @param targetType   target type
     * @param propertyName property name
     * @param value        property value
     */
    default void writeProperty(Class<?> targetType, Object target, String propertyName, Object value) {
        MethodInvoker setter = findSetter(targetType, propertyName);
        if (Objects.nonNull(setter)) {
            setter.invoke(target, value);
        }
    }

    /**
     * Get setter method.
     *
     * @param targetType target type
     * @param propertyName property name
     * @return setter method
     */
    @Nullable
    MethodInvoker findSetter(Class<?> targetType, String propertyName);
}
