package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * The abstract implementation of {@link PropDesc}.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public abstract class AbstractPropDesc implements PropDesc {

    private static final MethodInvoker NULL = (target, args) -> null;
    private final ConcurrentMap<String, MethodInvoker> getterCache = CollectionUtils.newWeakConcurrentMap();
    private final ConcurrentMap<String, MethodInvoker> setterCache = CollectionUtils.newWeakConcurrentMap();
    @Getter
    protected final Class<?> beanType;

    /**
     * Get the getter method.
     *
     * @param propertyName property name
     * @return property getter
     */
    @Nullable
    @Override
    public MethodInvoker getGetter(String propertyName) {
        MethodInvoker invoker = obtainInvokerFromCache(
            getterCache, propertyName, this::findGetter
        );
        return invoker == NULL ? null : invoker;
    }

    /**
     * Get the setter method.
     *
     * @param propertyName property name
     * @return property setter
     */
    @Nullable
    @Override
    public MethodInvoker getSetter(String propertyName) {
        MethodInvoker invoker = obtainInvokerFromCache(
            setterCache, propertyName, this::findSetter
        );
        return invoker == NULL ? null : invoker;
    }

    private MethodInvoker obtainInvokerFromCache(
        ConcurrentMap<String, MethodInvoker> cache, String propertyName,
        Function<String, MethodInvoker> invokerSupplier) {
        return CollectionUtils.computeIfAbsent(cache, propertyName, prop -> {
            MethodInvoker invoker = invokerSupplier.apply(prop);
            return invoker == null ? NULL : invoker;
        });
    }

    /**
     * find getter.
     *
     * @param propertyName property name
     * @return getter method
     */
    @Nullable
    protected abstract MethodInvoker findGetter(String propertyName);

    /**
     * find setter.
     *
     * @param propertyName property name
     * @return setter method
     */
    @Nullable
    protected abstract MethodInvoker findSetter(String propertyName);
}
