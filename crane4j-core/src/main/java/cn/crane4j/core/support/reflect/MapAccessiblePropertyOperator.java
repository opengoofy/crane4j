package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

/**
 * The wrapper class of {@link PropertyOperator} that
 * adds support for map operations to the original operator.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class MapAccessiblePropertyOperator implements PropertyOperator {

    /**
     * original operator
     */
    private final PropertyOperator propertyOperator;

    /**
     * Get the specified property value.
     *
     * @param targetType   target type
     * @param target       target
     * @param propertyName property name
     * @return property value
     */
    @Nullable
    @Override
    public Object readProperty(Class<?> targetType, Object target, String propertyName) {
        if (isMap(targetType)) {
            return castMap(target).get(propertyName);
        }
        return propertyOperator.readProperty(targetType, target, propertyName);
    }

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
        if (isMap(targetType)) {
            return (t, args) -> castMap(t).get(propertyName);
        }
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
        if (isMap(targetType)) {
            castMap(target).put(propertyName, value);
            return;
        }
        propertyOperator.writeProperty(targetType, target, propertyName, value);
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
        if (isMap(targetType)) {
            return (t, args) -> castMap(t).put(propertyName, args[0]);
        }
        return propertyOperator.findSetter(targetType, propertyName);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object target) {
        return (Map<String, Object>)target;
    }

    private static boolean isMap(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }
}
