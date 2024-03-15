package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The property descriptor of the bean.
 *
 * @author huangchengxing
 * @see AbstractPropDesc
 * @since 2.7.0
 */
public interface PropDesc {

    /**
     * Get the bean type.
     *
     * @return bean type
     */
    Class<?> getBeanType();

    /**
     * Get the getter method.
     *
     * @param propertyName property name
     * @return property getter
     */
    @Nullable
    MethodInvoker getGetter(String propertyName);

    /**
     * Get the specified property value.
     *
     * @param target       target
     * @param propertyName property name
     * @return property value
     */
    default Object readProperty(Object target, String propertyName) {
        MethodInvoker getter = getGetter(propertyName);
        return getter == null ? null : getter.invoke(target);
    }

    /**
     * Get the setter method.
     *
     * @param propertyName property name
     * @return property setter
     */
    @Nullable
    MethodInvoker getSetter(String propertyName);

    /**
     * Set the specified property value.
     *
     * @param target       target
     * @param propertyName property name
     * @param value        property value
     */
    default void writeProperty(Object target, String propertyName, Object value) {
        MethodInvoker setter = getSetter(propertyName);
        if (setter != null) {
            setter.invoke(target, value);
        }
    }
}
