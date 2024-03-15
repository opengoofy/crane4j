package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>The {@link PropertyOperator} holder.
 * It's generally used as the {@link PropertyOperator} actually used by
 * the various components in the project.
 *
 * <p>In contrast to other implementation classes,
 * it implements functionality through the PropertyOperator of the internal proxy,
 * which can be dynamically replaced at runtime,
 * which allows us to replace {@link PropertyOperator} without
 * changing the references of other components to the PropertyOperator instance.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
@Setter
@Getter
@AllArgsConstructor
public class PropertyOperatorHolder implements DecoratedPropertyOperator {

    /**
     * delegate property operator
     */
    @NonNull
    private PropertyOperator propertyOperator;

    /**
     * Get property descriptor.
     *
     * @param targetType target type
     * @return property descriptor
     * @since 2.7.0
     */
    @Override
    public @NonNull PropDesc getPropertyDescriptor(Class<?> targetType) {
        return propertyOperator.getPropertyDescriptor(targetType);
    }

    /**
     * Get the specified property value.
     *
     * @param targetType   target type
     * @param target       target
     * @param propertyName property name
     * @return property value
     */
    @Override
    public @Nullable Object readProperty(Class<?> targetType, Object target, String propertyName) {
        return propertyOperator.readProperty(targetType, target, propertyName);
    }

    /**
     * Get getter method.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @return getter method
     */
    @Override
    public @Nullable MethodInvoker findGetter(Class<?> targetType, String propertyName) {
        return propertyOperator.findGetter(targetType, propertyName);
    }

    /**
     * Set the specified property value.
     *
     * @param targetType   target type
     * @param target       target
     * @param propertyName property name
     * @param value        property value
     */
    @Override
    public void writeProperty(Class<?> targetType, Object target, String propertyName, Object value) {
        propertyOperator.writeProperty(targetType, target, propertyName, value);
    }

    /**
     * Get setter method.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @return setter method
     */
    @Override
    public @Nullable MethodInvoker findSetter(Class<?> targetType, String propertyName) {
        return propertyOperator.findSetter(targetType, propertyName);
    }
}
