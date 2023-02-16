package cn.createsequence.crane4j.core.support.reflect;

import cn.createsequence.crane4j.core.support.MethodInvoker;
import cn.createsequence.crane4j.core.util.CollectionUtils;
import cn.createsequence.crane4j.core.util.ReflectUtils;
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
 * 具备基本缓存功能的{@link PropertyOperator}抽象实现类
 *
 * @author huangchengxing
 * @see AsmReflectPropertyOperator
 * @see ReflectPropertyOperator
 */
public abstract class CacheablePropertyOperator implements PropertyOperator {

    private static final String SETTER_PREFIX = "set-";
    private static final String GETTER_PREFIX = "get-";
    private static final Object NULL = new Object();

    /**
     * 方法缓存
     */
    private final Map<Class<?>, Map<String, Object>> methodCaches = CollectionUtils.newWeakConcurrentMap();

    /**
     * 获取指定属性
     *
     * @param targetType   目标类型
     * @param target       对象
     * @param propertyName 属性名称
     * @return 属性值
     */
    @Nullable
    @Override
    public Object readProperty(Class<?> targetType, Object target, String propertyName) {
        MethodInvoker getter = findGetter(targetType, propertyName);
        return Objects.isNull(getter) ? null : getter.invoke(target);
    }

    /**
     * 获取Getter方法
     *
     * @param targetType   目标类型
     * @param propertyName 方法名称
     * @return 找到的方法，若没找到则返回{@code null}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public MethodInvoker findGetter(Class<?> targetType, String propertyName) {
        if (Map.class.isAssignableFrom(targetType)) {
            return (t, args) -> ((Map<String, Object>)t).get(propertyName);
        }
        return getCachedInvoker(
            targetType, GETTER_PREFIX, propertyName,
            () -> ReflectUtils.findGetterMethod(targetType, propertyName)
                .map(method -> createInvoker(targetType, propertyName, method))
                .orElse(null)
        );
    }

    /**
     * 将值写入指定属性
     *
     * @param targetType   目标类型
     * @param target       对象
     * @param propertyName 属性名称
     * @param value        属性值
     */
    @Override
    public void writeProperty(Class<?> targetType, Object target, String propertyName, Object value) {
        MethodInvoker setter = findSetter(targetType, propertyName);
        if (Objects.nonNull(setter)) {
            setter.invoke(target, value);
        }
    }

    /**
     * 获取Setter方法
     *
     * @param targetType   目标类型
     * @param propertyName 方法名称
     * @return 找到的方法，若没找到则返回{@code null}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public MethodInvoker findSetter(Class<?> targetType, String propertyName) {
        if (Map.class.isAssignableFrom(targetType)) {
            return (t, args) -> ((Map<String, Object>)t).put(propertyName, args[0]);
        }
        return getCachedInvoker(
            targetType, SETTER_PREFIX, propertyName,
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
     * 根据指定方法创建调用器
     *
     * @param targetType 目标类型
     * @param propertyName 属性名称
     * @param method 属性的getter或setter方法
     * @return 调用器
     */
    protected abstract MethodInvoker createInvoker(Class<?> targetType, String propertyName, Method method);

    private MethodInvoker getCachedInvoker(Class<?> type, String prefix, String methodName, Supplier<MethodInvoker> invokerSupplier) {
        methodName = prefix + methodName;
        Map<String, Object> caches = MapUtil.computeIfAbsent(
            methodCaches, type, t -> new ConcurrentHashMap<>(8)
        );
        Object target = MapUtil.computeIfAbsent(
            caches, methodName, m -> createInvoker(invokerSupplier.get())
        );
        return getInvoker(target);
    }

    private static MethodInvoker getInvoker(Object o) {
        return o == NULL ? null : (MethodInvoker)o;
    }

    private static Object createInvoker(Object target) {
        return Objects.isNull(target) ? NULL : target;
    }
}
