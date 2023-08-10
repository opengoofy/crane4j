package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.util.CollectionUtils;
import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link PropertyOperator} implementation based on {@link com.esotericsoftware.reflectasm}.
 *
 * @author huangchengxing
 * @author tangcent
 */
public class AsmReflectivePropertyOperator extends ReflectivePropertyOperator {

    /**
     * method access caches.
     */
    private final Map<Class<?>, MethodAccess> methodAccessCaches = new ConcurrentHashMap<>();

    /**
     * field access caches.
     */
    private final Map<Class<?>, FieldAccess> fieldAccessCaches = new ConcurrentHashMap<>();

    /**
     * Create an {@link AsmReflectivePropertyOperator} instance
     *
     * @param converterManager converter register
     */
    public AsmReflectivePropertyOperator(@Nullable ConverterManager converterManager) {
        super(converterManager);
    }

    /**
     * Create {@link MethodInvoker} according to the specified method
     *
     * @param targetType target type
     * @param propertyName property name
     * @param method getter method or setter method
     * @return {@link MethodInvoker}
     */
    @Override
    protected MethodInvoker createInvokerForMethod(Class<?> targetType, String propertyName, Method method) {
        MethodAccess access = CollectionUtils.computeIfAbsent(methodAccessCaches, targetType, MethodAccess::get);
        int methodIndex = access.getIndex(method.getName(), method.getParameterTypes());
        return new ReflectAsmMethodInvoker(methodIndex, access);
    }

    /**
     * {@link MethodInvoker} implementation based on {@link MethodAccess}
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    public static class ReflectAsmMethodInvoker implements MethodInvoker {

        private final int methodIndex;
        private final MethodAccess methodAccess;

        /**
         * Invoke method.
         *
         * @param target target
         * @param args args
         * @return result of invoke
         */
        @Override
        public Object invoke(@Nullable Object target, @Nullable Object... args) {
            return methodAccess.invoke(target, methodIndex, args);
        }
    }

    @Override
    protected MethodInvoker createSetterInvokerForField(Class<?> targetType, String propertyName, Field field) {
        if (Modifier.isPrivate(field.getModifiers())) {
            return super.createSetterInvokerForField(targetType, propertyName, field);
        }
        FieldAccess access = CollectionUtils.computeIfAbsent(fieldAccessCaches, targetType, FieldAccess::get);
        try {
            int fieldIndex = access.getIndex(field.getName());
            return new ReflectAsmFieldAdapterSetterInvoker(access, fieldIndex);
        } catch (IllegalArgumentException e) {
            return super.createSetterInvokerForField(targetType, propertyName, field);
        }
    }

    @Override
    protected MethodInvoker createGetterInvokerForField(Class<?> targetType, String propertyName, Field field) {
        if (Modifier.isPrivate(field.getModifiers())) {
            return super.createGetterInvokerForField(targetType, propertyName, field);
        }
        FieldAccess access = CollectionUtils.computeIfAbsent(fieldAccessCaches, targetType, FieldAccess::get);
        try {
            int fieldIndex = access.getIndex(field.getName());
            return new ReflectAsmFieldAdapterGetterInvoker(access, fieldIndex);
        } catch (IllegalArgumentException e) {
            return super.createGetterInvokerForField(targetType, propertyName, field);
        }
    }

    /**
     * {@link MethodInvoker} implementation based on {@link FieldAccess}
     */
    public abstract static class ReflectAsmFieldInvoker implements MethodInvoker {

        private final FieldAccess fieldAccess;
        private final int fieldIndex;

        protected ReflectAsmFieldInvoker(FieldAccess fieldAccess, int fieldIndex) {
            this.fieldAccess = fieldAccess;
            this.fieldIndex = fieldIndex;
        }

        @Override
        public Object invoke(@Nullable Object target, @Nullable Object... args) {
            return invoke(fieldAccess, fieldIndex, target, args);
        }

        protected abstract Object invoke(FieldAccess fieldAccess, int fieldIndex, @Nullable Object target, @Nullable Object... args);
    }

    /**
     * An implementation of the {@link ReflectAsmFieldInvoker} for getter.
     */
    public static class ReflectAsmFieldAdapterGetterInvoker extends ReflectAsmFieldInvoker {

        public ReflectAsmFieldAdapterGetterInvoker(FieldAccess fieldAccess, int fieldIndex) {
            super(fieldAccess, fieldIndex);
        }

        @Override
        protected Object invoke(FieldAccess fieldAccess, int fieldIndex, @Nullable Object target, @Nullable Object... args) {
            return fieldAccess.get(target, fieldIndex);
        }
    }

    /**
     * An implementation of the {@link ReflectAsmFieldInvoker} for setter.
     */
    public static class ReflectAsmFieldAdapterSetterInvoker extends ReflectAsmFieldInvoker {

        public ReflectAsmFieldAdapterSetterInvoker(FieldAccess fieldAccess, int fieldIndex) {
            super(fieldAccess, fieldIndex);
        }

        @Override
        protected Object invoke(FieldAccess fieldAccess, int fieldIndex, @Nullable Object target, @Nullable Object... args) {
            fieldAccess.set(target, fieldIndex, args[0]);
            return null;
        }
    }
}
