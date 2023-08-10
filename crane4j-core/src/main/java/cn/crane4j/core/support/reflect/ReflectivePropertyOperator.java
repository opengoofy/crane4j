package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.ParameterConvertibleMethodInvoker;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ReflectUtils;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <p>Implementation of {@link PropertyOperator} based on Java reflection.<br />
 * Supports reading and writing JavaBean properties via Field or getter/setter methods.
 * 
 * <p><strong>supports</strong>:
 * <p>This property operator can be used to handle the following scenarios:
 * <ul>
 *     <li>
 *         The property actually exists, and there is a corresponding getter or setter method:
 *         the getter/setter method is preferred to complete the reading and writing of the property value;
 *     </li>
 *     <li>
 *         The property actually exists, but there is no corresponding getter or setter method:
 *         directly use Field to complete the reading and writing of the property value;
 *     </li>
 *     <li>
 *         The property does not actually exist, but there are getter or setter methods that conform to the specification:
 *         use the getter/setter method to complete the reading and writing of the property value;
 *     </li>
 * </ul>
 *
 * <p><strong>throws exception</strong>:
 * <p>If the specified property cannot find a Field or getter/setter method that supports read and write,
 * the operation will be aborted and null (if there is a return value) will be returned.<br />
 * We can set {@link #throwIfNoAnyMatched} to {@code true} to throw an exception in this case.
 *
 * <p><strong>type conversion</strong>:
 * <p>Setting {@link ConverterManager}, the class supports a certain degree of automatic conversion
 * of parameter types when writing property values.
 *
 * @author huangchengxing
 * @author tangcent
 */
public class ReflectivePropertyOperator implements PropertyOperator {

    /**
     * converter register
     */
    @Nullable
    @Setter
    protected ConverterManager converterManager;

    /**
     * Whether to throw an exception if no matching method or field is found.
     */
    @Setter
    private boolean throwIfNoAnyMatched = false;

    /**
     * Create a property operator.
     *
     * @param converterManager converter manager
     */
    public ReflectivePropertyOperator(@Nullable ConverterManager converterManager) {
        this.converterManager = converterManager;
    }

    /**
     * Create a property operator.
     */
    public ReflectivePropertyOperator() {
        this(null);
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
        Method method = findGetterMethod(targetType, propertyName);
        MethodInvoker methodInvoker = resolveInvokerForMethod(targetType, propertyName, method);
        if (methodInvoker != null) {
            return methodInvoker;
        }

        Field field = ReflectUtils.getField(targetType, propertyName);
        MethodInvoker methodInvokerForGetter = resolveGetterInvokerForField(targetType, propertyName, field);
        if (methodInvokerForGetter != null) {
            return methodInvokerForGetter;
        }

        Asserts.isFalse(throwIfNoAnyMatched, "No getter method found for property [{}] in [{}] ", propertyName, targetType.getName());
        return null;
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
        Method method = findSetterMethod(targetType, propertyName);
        MethodInvoker methodInvoker = resolveInvokerForMethod(targetType, propertyName, method);
        if (methodInvoker != null) {
            return methodInvoker;
        }

        Field field = ReflectUtils.getField(targetType, propertyName);
        MethodInvoker methodInvokerForSetter = resolveSetterInvokerForField(targetType, propertyName, field);
        if (methodInvokerForSetter != null) {
            return methodInvokerForSetter;
        }

        Asserts.isFalse(throwIfNoAnyMatched, "No setter method found for property [{}] in [{}] ", propertyName, targetType.getName());
        return null;
    }

    // region ========= find by method =========

    /**
     * Resolve the invoker which finally to used.
     *
     * @param targetType target type
     * @param propertyName property name
     * @param method method
     * @return method invoker instance
     * @see #createInvokerForMethod
     * @see ParameterConvertibleMethodInvoker
     */
    @Nullable
    protected final MethodInvoker resolveInvokerForMethod(Class<?> targetType, String propertyName, @Nullable Method method) {
        if (Objects.isNull(method)) {
            return null;
        }
        MethodInvoker invoker = createInvokerForMethod(targetType, propertyName, method);
        if (Objects.isNull(invoker)) {
            return null;
        }
        if (Objects.isNull(converterManager)) {
            return invoker;
        }
        return ParameterConvertibleMethodInvoker.create(invoker, converterManager, method.getParameterTypes());
    }

    /**
     * Create {@link MethodInvoker} according to the specified method.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @param method       getter method or setter method
     * @return {@link MethodInvoker}
     */
    @Nullable
    protected MethodInvoker createInvokerForMethod(Class<?> targetType, String propertyName, Method method) {
        return ReflectiveMethodInvoker.create(null, method, false);
    }

    // endregion

    // region ========= find by field =========

    /**
     * Resolve invoker which finally be used to set the value of the specified field.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @param field        field to be set.
     * @return the {@link MethodInvoker} instance for setting the value of the specified field.
     */
    @Nullable
    protected final MethodInvoker resolveSetterInvokerForField(Class<?> targetType, String propertyName, @Nullable Field field) {
        if (Objects.isNull(field)) {
            return null;
        }
        MethodInvoker invoker = createSetterInvokerForField(targetType, propertyName, field);
        if (Objects.isNull(invoker)) {
            return null;
        }
        if (Objects.isNull(converterManager)) {
            return invoker;
        }
        return ParameterConvertibleMethodInvoker.create(invoker, converterManager, new Class[] {field.getType()});
    }

    /**
     * Creates a {@link MethodInvoker} for setting the value of the specified field.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @param field        field to be set.
     * @return The {@link MethodInvoker} instance for setting the value of the specified field.
     */
    protected MethodInvoker createSetterInvokerForField(Class<?> targetType, String propertyName, Field field) {
        return ReflectiveFieldAdapterMethodInvoker.createSetter(field);
    }

    /**
     * Resolve invoker which finally be used to get the value of the specified field.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @param field        field to be got.
     * @return the {@link MethodInvoker} instance for getting the value of the specified field.
     */
    protected final MethodInvoker resolveGetterInvokerForField(Class<?> targetType, String propertyName, @Nullable Field field) {
        if (Objects.isNull(field)) {
            return null;
        }
        return createGetterInvokerForField(targetType, propertyName, field);
    }

    /**
     * Creates a {@link MethodInvoker} for getting the value of the specified field.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @param field        field to be got.
     * @return The {@link MethodInvoker} instance for getting the value of the specified field.
     */
    protected MethodInvoker createGetterInvokerForField(Class<?> targetType, String propertyName, Field field) {
        return ReflectiveFieldAdapterMethodInvoker.createGetter(field);
    }

    // ================== static methods ==================

    // endregion

    /**
     * Find setter method by given type and field name.
     *
     * @param targetType target type
     * @param propertyName property name
     * @return setter method
     */
    @Nullable
    private static Method findSetterMethod(Class<?> targetType, String propertyName) {
        Field field = ReflectUtils.getField(targetType, propertyName);
        if (Objects.isNull(field)) {
            return ReflectUtils.findSetterMethod(targetType, propertyName)
                .orElse(null);
        } else {
            return ReflectUtils.findSetterMethod(targetType, field)
                .orElse(null);
        }
    }

    /**
     * Find getter method by given type and field name.
     *
     * @param targetType target type
     * @param propertyName property name
     * @return getter method
     */
    @Nullable
    private static Method findGetterMethod(Class<?> targetType, String propertyName) {
        Field field = ReflectUtils.getField(targetType, propertyName);
        if (Objects.isNull(field)) {
            return ReflectUtils.findGetterMethod(targetType, propertyName)
                .orElse(null);
        } else {
            return ReflectUtils.findGetterMethod(targetType, field)
                .orElse(null);
        }
    }
}
