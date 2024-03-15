package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * Property operator, used to read and write object attributes.
 *
 * @author huangchengxing
 * @see PropDesc
 * @see AsmReflectivePropertyOperator
 * @see ReflectivePropertyOperator
 */
public interface PropertyOperator {

    /**
     * Get property descriptor.
     *
     * @param targetType target type
     * @return property descriptor
     * @since 2.7.0
     */
    @NonNull
    PropDesc getPropertyDescriptor(Class<?> targetType);

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
    default MethodInvoker findGetter(Class<?> targetType, String propertyName) {
        return getPropertyDescriptor(targetType).getGetter(propertyName);
    }

    /**
     * Set the specified property value.
     *
     * @param target       target
     * @param targetType   target type
     * @param propertyName property name
     * @param value        property value
     */
    default void writeProperty(Class<?> targetType, Object target, String propertyName, Object value) {
        getPropertyDescriptor(targetType).writeProperty(target, propertyName, value);
    }

    /**
     * Get setter method.
     *
     * @param targetType target type
     * @param propertyName property name
     * @return setter method
     */
    @Nullable
    default MethodInvoker findSetter(Class<?> targetType, String propertyName) {
        return getPropertyDescriptor(targetType).getSetter(propertyName);
    }
}
