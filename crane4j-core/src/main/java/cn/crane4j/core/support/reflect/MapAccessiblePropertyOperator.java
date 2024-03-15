package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
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

    private static final MapPropDesc MAP_PROP_DESC = new MapPropDesc();

    /**
     * original operator
     */
    private final PropertyOperator delegate;

    /**
     * Get property descriptor.
     *
     * @param targetType target type
     * @return property descriptor
     * @since 2.7.0
     */
    @Override
    public @NonNull PropDesc getPropertyDescriptor(Class<?> targetType) {
        return Map.class.isAssignableFrom(targetType) ?
            MAP_PROP_DESC : delegate.getPropertyDescriptor(targetType);
    }

    /**
     * A property descriptor for map.
     *
     * @author huangchengxing
     * @since 2.7.0
     */
    private static class MapPropDesc implements PropDesc {
        @Override
        public Class<?> getBeanType() {
            return Map.class;
        }
        @Override
        public @Nullable MethodInvoker getGetter(String propertyName) {
            return (t, args) -> castMap(t).get(propertyName);
        }
        @Override
        public @Nullable MethodInvoker getSetter(String propertyName) {
            return (t, arg) -> castMap(t).put(propertyName, arg[0]);
        }
        @SuppressWarnings("unchecked")
        private static Map<String, Object> castMap(Object target) {
            return (Map<String, Object>)target;
        }
    }
}
