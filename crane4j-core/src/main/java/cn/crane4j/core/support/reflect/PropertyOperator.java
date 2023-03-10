package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;

import javax.annotation.Nullable;

/**
 * Property operator, used to read and write object attributes.
 *
 * @author huangchengxing
 * @see AsmReflectPropertyOperator
 * @see ReflectPropertyOperator
 */
public interface PropertyOperator {

    /**
     * Get the specified property value.
     *
     * @param target target
     * @param targetType target type
     * @param propertyName property name
     * @return property value
     */
    @Nullable
    Object readProperty(Class<?> targetType, Object target, String propertyName);

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
     * @param target target
     * @param targetType target type
     * @param propertyName property name
     * @param value property value
     */
    void writeProperty(Class<?> targetType, Object target, String propertyName, Object value);

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
