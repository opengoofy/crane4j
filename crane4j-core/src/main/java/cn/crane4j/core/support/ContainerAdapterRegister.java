package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

/**
 * Container adapter register.
 *
 * @author huangchengxing
 * @see DefaultContainerAdapterRegister
 * @since 2.2.0
 */
public interface ContainerAdapterRegister {

    /**
     * Get target type.
     *
     * @param targetType target type
     * @return {@link Adapter} instance.
     */
    @Nullable
    Adapter getAdapter(Class<?> targetType);

    /**
     * Register adapter.
     *
     * @param targetType target type
     * @param adapter adapter
     */
    void registerAdapter(Class<?> targetType, Adapter adapter);

    /**
     * Wrap target to {@link Container} if possible.
     *
     * @param namespace namespace of container
     * @param target target
     * @param <T> key type of container
     * @return {@link Container} instant if possible, null otherwise
     */
    @SuppressWarnings("unchecked")
    @Nullable
    default <T> Container<T> wrapIfPossible(String namespace, Object target) {
        return (Container<T>)Optional.ofNullable(target)
            .map(Object::getClass)
            .map(this::getAdapter)
            .map(adapter -> adapter.wrapIfPossible(namespace, target))
            .orElse(null);
    }

    /**
     * An adapter for wrap object to {@link Container}.
     *
     * @author huangchengxing
     */
    @FunctionalInterface
    interface Adapter {

        /**
         * Wrap target to {@link Container} if possible.
         *
         * @param namespace namespace of container
         * @param target target
         * @return {@link Container} instant if possible, null otherwise
         */
        @Nullable
        Container<Object> wrapIfPossible(String namespace, @NonNull Object target);
    }
}
