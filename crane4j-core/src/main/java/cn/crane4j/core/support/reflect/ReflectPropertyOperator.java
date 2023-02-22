package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.hutool.core.util.ReflectUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * A {@link PropertyOperator} implementation based on JDK reflection.
 *
 * @author huangchengxing
 */
public class ReflectPropertyOperator extends CacheablePropertyOperator {

    /**
     * Create {@link MethodInvoker} according to the specified method
     *
     * @param targetType target type
     * @param propertyName property name
     * @param method getter method or setter method
     * @return {@link MethodInvoker}
     */
    @Override
    protected MethodInvoker createInvoker(Class<?> targetType, String propertyName, Method method) {
        return new ReflectMethodInvoker(method);
    }

    /**
     * {@link MethodInvoker} implementation based on JDK reflection
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    public static class ReflectMethodInvoker implements MethodInvoker {

        /**
         * method
         */
        @NonNull
        private final Method method;

        /**
         * Invoke method.
         *
         * @param target target
         * @param args args
         * @return result of invoke
         */
        @Override
        public Object invoke(@Nullable Object target, @Nullable Object... args) {
            return ReflectUtil.invoke(target, method, args);
        }
    }
}
