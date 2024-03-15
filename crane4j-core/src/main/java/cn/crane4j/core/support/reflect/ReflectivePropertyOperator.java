package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.ParameterConvertibleMethodInvoker;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ReflectUtils;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
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
 * @since 2.0.0
 */
@Setter
public class ReflectivePropertyOperator implements PropertyOperator {

    public static final ReflectivePropertyOperator INSTANCE = new ReflectivePropertyOperator();

    /**
     * converter register
     */
    @Nullable
    protected ConverterManager converterManager;

    /**
     * Whether to throw an exception if no matching method or field is found.
     */
    protected boolean throwIfNoAnyMatched = false;

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
     * Get property descriptor.
     *
     * @param targetType target type
     * @return property descriptor
     * @since 2.7.0
     */
    @Override
    public @NonNull PropDesc getPropertyDescriptor(Class<?> targetType) {
        return new ReflectivePropDesc(targetType, converterManager, throwIfNoAnyMatched);
    }

    /**
     * The reflection based property descriptor.
     *
     * @author huangchengxing
     * @since 2.7.0
     */
    protected static class ReflectivePropDesc extends AbstractPropDesc {

        /**
         * converter register
         */
        @Nullable
        protected final ConverterManager converterManager;

        /**
         * Whether to throw an exception if no matching method or field is found.
         */
        private final boolean throwIfNoAnyMatched;

        public ReflectivePropDesc(
            Class<?> beanType, @Nullable ConverterManager converterManager, boolean throwIfNoAnyMatched) {
            super(beanType);
            this.converterManager = converterManager;
            this.throwIfNoAnyMatched = throwIfNoAnyMatched;
        }

        /**
         * Get getter method.
         *
         * @param propertyName property name
         * @return getter method
         */
        @Nullable
        @Override
        public MethodInvoker findGetter(String propertyName) {
            Method method = findGetterMethod(beanType, propertyName);
            MethodInvoker methodInvoker = resolveInvokerForMethod(propertyName, method);
            if (methodInvoker != null) {
                return methodInvoker;
            }

            Field field = ReflectUtils.getField(beanType, propertyName);
            MethodInvoker methodInvokerForGetter = resolveGetterInvokerForField(propertyName, field);
            if (methodInvokerForGetter != null) {
                return methodInvokerForGetter;
            }

            Asserts.isFalse(throwIfNoAnyMatched, "No getter method found for property [{}] in [{}] ", propertyName, beanType.getName());
            return null;
        }

        /**
         * Get setter method.
         *
         * @param propertyName property name
         * @return setter method
         */
        @Nullable
        @Override
        public MethodInvoker findSetter(String propertyName) {
            Method method = findSetterMethod(beanType, propertyName);
            MethodInvoker methodInvoker = resolveInvokerForMethod(propertyName, method);
            if (methodInvoker != null) {
                return methodInvoker;
            }

            Field field = ReflectUtils.getField(beanType, propertyName);
            MethodInvoker methodInvokerForSetter = resolveSetterInvokerForField(propertyName, field);
            if (methodInvokerForSetter != null) {
                return methodInvokerForSetter;
            }

            Asserts.isFalse(throwIfNoAnyMatched, "No setter method found for property [{}] in [{}] ", propertyName, beanType.getName());
            return null;
        }

        // region ========= find by method =========

        /**
         * Resolve the invoker which finally to used.
         *
         * @param propertyName property name
         * @param method method
         * @return method invoker instance
         * @see #createInvokerForMethod
         * @see ParameterConvertibleMethodInvoker
         */
        @Nullable
        protected final MethodInvoker resolveInvokerForMethod(String propertyName, @Nullable Method method) {
            if (Objects.isNull(method)) {
                return null;
            }
            MethodInvoker invoker = createInvokerForMethod(propertyName, method);
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
         * @param propertyName property name
         * @param method       getter method or setter method
         * @return {@link MethodInvoker}
         */
        @Nullable
        protected MethodInvoker createInvokerForMethod(String propertyName, Method method) {
            return ReflectiveMethodInvoker.create(null, method, false);
        }

        // endregion

        // region ========= find by field =========

        /**
         * Resolve invoker which finally be used to set the value of the specified field.
         *
         * @param propertyName property name
         * @param field        field to be set.
         * @return the {@link MethodInvoker} instance for setting the value of the specified field.
         */
        @Nullable
        protected final MethodInvoker resolveSetterInvokerForField(String propertyName, @Nullable Field field) {
            if (Objects.isNull(field)) {
                return null;
            }
            MethodInvoker invoker = createSetterInvokerForField(propertyName, field);
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
         * @param propertyName property name
         * @param field        field to be set.
         * @return The {@link MethodInvoker} instance for setting the value of the specified field.
         */
        protected MethodInvoker createSetterInvokerForField(String propertyName, Field field) {
            return ReflectiveFieldAdapterMethodInvoker.createSetter(field);
        }

        /**
         * Resolve invoker which finally be used to get the value of the specified field.
         *
         * @param propertyName property name
         * @param field        field to be got.
         * @return the {@link MethodInvoker} instance for getting the value of the specified field.
         */
        protected final MethodInvoker resolveGetterInvokerForField(String propertyName, @Nullable Field field) {
            if (Objects.isNull(field)) {
                return null;
            }
            return createGetterInvokerForField(propertyName, field);
        }

        /**
         * Creates a {@link MethodInvoker} for getting the value of the specified field.
         *
         * @param propertyName property name
         * @param field        field to be got.
         * @return The {@link MethodInvoker} instance for getting the value of the specified field.
         */
        protected MethodInvoker createGetterInvokerForField(String propertyName, Field field) {
            return ReflectiveFieldAdapterMethodInvoker.createGetter(field);
        }

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
}
