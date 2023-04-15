package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link PropertyOperator} implementation based on {@link com.esotericsoftware.reflectasm}.
 *
 * @author huangchengxing
 */
public class AsmReflectPropertyOperator extends CacheablePropertyOperator {

    /**
     * method access caches.
     */
    private final Map<Class<?>, MethodAccess> methodAccessCaches = new ConcurrentHashMap<>();

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
}
