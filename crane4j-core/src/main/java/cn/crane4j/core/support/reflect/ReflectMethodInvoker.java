package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.ReflectUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * A {@link MethodInvoker} implementation based on JDK reflection.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
@RequiredArgsConstructor
public abstract class ReflectMethodInvoker implements MethodInvoker {

    protected final Object target;
    protected final Method method;
    protected final boolean alignArguments;

    /**
     * Create a {@link ReflectMethodInvoker} from the given method.
     *
     * @param target         target, if null, use method's declaring class as target
     * @param method         method
     * @param alignArguments align arguments
     * @return {@link ReflectMethodInvoker}
     */
    public static ReflectMethodInvoker create(Object target, Method method, boolean alignArguments) {
        if (Objects.nonNull(target)
            && Proxy.isProxyClass(target.getClass())
            && !Proxy.isProxyClass(method.getDeclaringClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(target);
            return new ProxyMethodInvoker(handler, method, alignArguments);
        }
        return new InvocationMethodInvoker(target, method, alignArguments);
    }

    /**
     * Invoke method.
     *
     * @param target target
     * @param args   args
     * @return result of invoke
     */
    @Override
    public Object invoke(Object target, Object... args) {
        Object[] actualArgs = alignArguments ? args : ReflectUtils.resolveMethodInvocationArguments(method, args);
        return invokeMethod(target, actualArgs);
    }

    /**
     * Invoke method.
     *
     * @param target target
     * @param args args
     * @return result of invoke
     */
    protected abstract Object invokeMethod(Object target, Object... args);
    

    /**
     * Create a {@link MethodInvoker} from the given method.
     *
     * @author huangchengxing
     */
    public static class InvocationMethodInvoker extends ReflectMethodInvoker {

        /**
         * Constructor with target and method.
         *
         * @param target target
         * @param method method
         * @param invokeRow invoke row
         */
        public InvocationMethodInvoker(Object target, Method method, boolean invokeRow) {
            super(target, method, invokeRow);
        }

        /**
         * Invoke method.
         *
         * @param target target
         * @param args   args
         * @return result of invoke
         */
        @Override
        protected Object invokeMethod(Object target, Object... args) {
            return ReflectUtils.invokeRaw(target, method, args);
        }
    }

    /**
     * A {@link MethodInvoker} implementation for JDK proxy object, which invoke method on proxy object.
     *
     * @author huangchengxing
     */
    public static class ProxyMethodInvoker extends ReflectMethodInvoker {

        /**
         * Constructor with target and method.
         *
         * @param target target
         * @param method method
         * @param invokeRow invoke row
         */
        public ProxyMethodInvoker(Object target, Method method, boolean invokeRow) {
            super(target, method, invokeRow);
        }

        /**
         * Invoke method.
         *
         * @param target target
         * @param args   args
         * @return result of invoke
         */
        @SneakyThrows
        @Override
        protected Object invokeMethod(Object target, Object... args) {
            return ((InvocationHandler)super.target).invoke(target, method, args);
        }
    }
}
