package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A {@link PropertyOperator} abstract implementation class with basic caching function.
 *
 * @author huangchengxing
 * @see AsmReflectPropertyOperator
 * @see ReflectPropertyOperator
 */
public abstract class CacheablePropertyOperator implements PropertyOperator {

    private static final Object NULL = new Object();

    /**
     * getter cache
     */
    private final Map<Class<?>, Map<String, Object>> getterCaches = CollectionUtils.newWeakConcurrentMap();

    /**
     * setter cache
     */
    private final Map<Class<?>, Map<String, Object>> setterCaches = CollectionUtils.newWeakConcurrentMap();

    /**
     * Get the specified property value.
     *
     * @param target target
     * @param targetType target type
     * @param propertyName property name
     * @return property value
     */
    @Nullable
    @Override
    public Object readProperty(Class<?> targetType, Object target, String propertyName) {
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
    @Override
    public MethodInvoker findGetter(Class<?> targetType, String propertyName) {
        return getCachedGetter(
            targetType, propertyName,
            () -> ReflectUtils.findGetterMethod(targetType, propertyName)
                .map(method -> createInvoker(targetType, propertyName, method))
                .orElse(null)
        );
    }

    /**
     * Set the specified property value.
     *
     * @param target target
     * @param targetType target type
     * @param propertyName property name
     * @param value property value
     */
    @Override
    public void writeProperty(Class<?> targetType, Object target, String propertyName, Object value) {
        MethodInvoker setter = findSetter(targetType, propertyName);
        if (Objects.nonNull(setter)) {
            setter.invoke(target, value);
        }
    }

    /**
     * Get setter method.
     *
     * @param targetType target type
     * @param propertyName property name
     * @return setter method
     */
    @Nullable
    @Override
    public MethodInvoker findSetter(Class<?> targetType, String propertyName) {
        return getCachedSetter(
            targetType, propertyName,
            () -> {
                Field field = ReflectUtil.getField(targetType, propertyName);
                if (Objects.isNull(field)) {
                    return null;
                }
                return ReflectUtils.findSetterMethod(targetType, field)
                    .map(method -> createInvoker(targetType, propertyName, method))
                    .orElse(null);
            }
        );
    }

    /**
     * Create {@link MethodInvoker} according to the specified method
     *
     * @param targetType target type
     * @param propertyName property name
     * @param method getter method or setter method
     * @return {@link MethodInvoker}
     */
    protected abstract MethodInvoker createInvoker(Class<?> targetType, String propertyName, Method method);

    private MethodInvoker getCachedGetter(Class<?> type, String methodName, Supplier<MethodInvoker> invokerSupplier) {
        Map<String, Object> caches = MapUtil.computeIfAbsent(getterCaches, type, t -> new ConcurrentHashMap<>(8));
        Object target = MapUtil.computeIfAbsent(caches, methodName, m -> createInvoker(invokerSupplier.get()));
        return getInvoker(target);
    }

    private MethodInvoker getCachedSetter(Class<?> type, String methodName, Supplier<MethodInvoker> invokerSupplier) {
        Map<String, Object> caches = MapUtil.computeIfAbsent(setterCaches, type, t -> new ConcurrentHashMap<>(8));
        Object target = MapUtil.computeIfAbsent(caches, methodName, m -> createInvoker(invokerSupplier.get()));
        return getInvoker(target);
    }

    private static MethodInvoker getInvoker(Object o) {
        return o == NULL ? null : (MethodInvoker)o;
    }

    private static Object createInvoker(Object target) {
        return Objects.isNull(target) ? NULL : target;
    }
}
