package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * An {@link PropertyOperator} implementation that uses {@link java.lang.invoke.MethodHandle} to access properties.
 *
 * @author huangchengxing
 * @see MethodHandles
 * @see MethodHandle
 * @see 2.2.0
 */
@NoArgsConstructor
@Slf4j
public class MethodHandlePropertyOperator extends ReflectivePropertyOperator {

    /**
     * Create a property operator.
     *
     * @param converterManager converter manager
     */
    public MethodHandlePropertyOperator(@Nullable ConverterManager converterManager) {
        super(converterManager);
    }

    /**
     * Creates a {@link MethodInvoker} for setting the value of the specified field.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @param field        field to be set.
     * @return The {@link MethodInvoker} instance for setting the value of the specified field.
     */
    @SneakyThrows
    @Override
    protected MethodInvoker createSetterInvokerForField(Class<?> targetType, String propertyName, Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            return super.createSetterInvokerForField(targetType, propertyName, field);
        }
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return new MethodHandleSetter(lookup.unreflectSetter(field));
        } catch (Exception e) {
            log.warn("cannot find method handle of setter for field: {}", field, e);
        }
        return super.createSetterInvokerForField(targetType, propertyName, field);
    }

    /**
     * Creates a {@link MethodInvoker} for getting the value of the specified field.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @param field        field to be got.
     * @return The {@link MethodInvoker} instance for getting the value of the specified field.
     */
    @Override
    protected MethodInvoker createGetterInvokerForField(Class<?> targetType, String propertyName, Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            return super.createGetterInvokerForField(targetType, propertyName, field);
        }
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return new MethodHandleGetter(lookup.unreflectGetter(field));
        } catch (Exception e) {
            log.warn("cannot find method handle of getter for field: {}", field, e);
        }
        return super.createGetterInvokerForField(targetType, propertyName, field);
    }

    /**
     * Setter based on {@link MethodHandle}.
     *
     * @author huangchengxing
     * @since 2.2.0
     */
    @RequiredArgsConstructor
    public static class MethodHandleSetter implements MethodInvoker {

        /**
         * method handle.
         */
        private final MethodHandle methodHandle;

        /**
         * Invoke method.
         *
         * @param target target
         * @param args   args
         * @return result of invoke
         */
        @SneakyThrows
        @Override
        public Object invoke(Object target, Object... args) {
            return methodHandle.bindTo(target).invokeWithArguments(args);
        }
    }

    /**
     * Getter based on {@link MethodHandle}.
     *
     * @author huangchengxing
     * @since 2.2.0
     */
    @RequiredArgsConstructor
    public static class MethodHandleGetter implements MethodInvoker {

        /**
         * method handle.
         */
        private final MethodHandle methodHandle;

        /**
         * Invoke method.
         *
         * @param target target
         * @param args   args
         * @return result of invoke
         */
        @SneakyThrows
        @Override
        public Object invoke(Object target, Object... args) {
            return methodHandle.bindTo(target).invoke();
        }
    }
}
