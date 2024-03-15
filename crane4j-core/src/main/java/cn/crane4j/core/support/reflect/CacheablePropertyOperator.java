package cn.crane4j.core.support.reflect;

import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * The wrapper class of {@link PropertyOperator} that adds support for invoker cache.
 *
 * @author huangchengxing
 * @since 2.0.0
 */
@RequiredArgsConstructor
public class CacheablePropertyOperator implements PropertyOperator {

    private final Map<Class<?>, PropDesc> getterCaches = CollectionUtils.newWeakConcurrentMap();
    private final PropertyOperator delegate;

    /**
     * Get property descriptor.
     *
     * @param targetType target type
     * @return property descriptor
     * @since 2.7.0
     */
    @NonNull
    @Override
    public PropDesc getPropertyDescriptor(Class<?> targetType) {
        return CollectionUtils.computeIfAbsent(getterCaches, targetType, delegate::getPropertyDescriptor);
    }
}
