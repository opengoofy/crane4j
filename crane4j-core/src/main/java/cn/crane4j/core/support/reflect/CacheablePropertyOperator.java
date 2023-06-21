package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * The wrapper class of {@link PropertyOperator} that adds support for invoker cache.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class CacheablePropertyOperator implements PropertyOperator {

    /**
     * Null cache object
     */
    private static final MethodInvoker NULL = (target, args) -> null;

    /**
     * getter cache
     */
    private final Map<Class<?>, Map<String, MethodInvoker>> getterCaches = CollectionUtils.newWeakConcurrentMap();

    /**
     * setter cache
     */
    private final Map<Class<?>, Map<String, MethodInvoker>> setterCaches = CollectionUtils.newWeakConcurrentMap();

    /**
     * Property operator
     */
    private final PropertyOperator propertyOperator;

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
        MethodInvoker invoker = findInvokerFromCache(
                getterCaches, targetType, propertyName, propertyOperator::findGetter
        );
        return resolve(invoker);
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
        MethodInvoker invoker = findInvokerFromCache(
                setterCaches, targetType, propertyName, propertyOperator::findSetter
        );
        return resolve(invoker);
    }

    @Nullable
    private MethodInvoker resolve(MethodInvoker invoker) {
        return invoker == NULL ? null : invoker;
    }

    @Nonnull
    private MethodInvoker findInvokerFromCache(
            Map<Class<?>, Map<String, MethodInvoker>> caches,
            Class<?> targetType, String propertyName,
            BiFunction<Class<?>, String, MethodInvoker> invokerFactory) {
        Map<String, MethodInvoker> invokers = CollectionUtils.computeIfAbsent(
                caches, targetType, t -> new ConcurrentHashMap<>(8)
        );
        return CollectionUtils.computeIfAbsent(invokers, propertyName, t -> {
            MethodInvoker target = invokerFactory.apply(targetType, propertyName);
            return Objects.isNull(target) ? NULL : target;
        });
    }
}
