package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.ReflectUtils;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

/**
 * Adapter class that implements the {@link MethodInvoker} interface with fields
 * without explicit getter and setter methods.
 *
 * @author tangcent
 */
@RequiredArgsConstructor
public abstract class ReflectiveFieldAdapterMethodInvoker implements MethodInvoker {

    private final Field field;

    /**
     * Create a getter {@link ReflectiveFieldAdapterMethodInvoker} from the given field.
     *
     * @param field the field to access
     * @return the FieldAdapterMethodInvoker for getting the field value
     */
    public static ReflectiveFieldAdapterMethodInvoker createGetter(Field field) {
        ReflectUtils.setAccessible(field);
        return new ReflectiveFieldGetterInvoker(field);
    }

    /**
     * Create a setter {@link ReflectiveFieldAdapterMethodInvoker} from the given field.
     *
     * @param field the field to access
     * @return the FieldAdapterMethodInvoker for setting the field value
     */
    public static ReflectiveFieldAdapterMethodInvoker createSetter(Field field) {
        ReflectUtils.setAccessible(field);
        return new ReflectiveFieldSetterInvoker(field);
    }

    @Override
    public Object invoke(Object target, Object... args) {
        return accessField(target, field, args);
    }

    /**
     * Get or set the field value using reflection.
     *
     * @param target the object on which to invoke the method
     * @param field  the field to access
     * @param args   the arguments to pass to the method
     * @return the result of the method invocation
     */
    protected abstract Object accessField(Object target, Field field, Object... args);

    /**
     * An implementation of the {@link ReflectiveFieldAdapterMethodInvoker} for getter.
     */
    private static class ReflectiveFieldGetterInvoker extends ReflectiveFieldAdapterMethodInvoker {

        public ReflectiveFieldGetterInvoker(Field field) {
            super(field);
        }

        /**
         * Gets the value of the field using reflection.
         *
         * @param target the object on which to invoke the method
         * @param field  the field to access
         * @param args   the arguments to pass to the method (ignored)
         * @return the value of the field
         */
        @Override
        protected Object accessField(Object target, Field field, Object... args) {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to access field: " + field.getName(), e);
            }
        }
    }

    /**
     * An implementation of the {@link ReflectiveFieldAdapterMethodInvoker} for setter.
     */
    private static class ReflectiveFieldSetterInvoker extends ReflectiveFieldAdapterMethodInvoker {

        public ReflectiveFieldSetterInvoker(Field field) {
            super(field);
        }

        /**
         * Sets the value of the field using reflection.
         *
         * @param target the object on which to invoke the method
         * @param field  the field to access
         * @param args   the arguments to pass to the method (one argument, the value to set the field to)
         * @return null (the result of the method invocation)
         */
        @Override
        protected Object accessField(Object target, Field field, Object... args) {
            try {
                field.set(target, args[0]);
                return null;
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to access field: " + field.getName(), e);
            }
        }
    }
}
