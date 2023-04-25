package cn.crane4j.core.support.container;

import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * Support class for {@link MethodInvokerContainer} creation.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class MethodInvokerContainerCreator {

    protected final PropertyOperator propertyOperator;

    /**
     * Create a {@link MethodInvokerContainer} from the given method.
     *
     * @param target      method's calling object
     * @param method      method
     * @param mappingType mapping type
     * @param namespace   namespace, if null, use method name as namespace
     * @param resultType  result type, if mapping type is {@link MappingType#MAPPED}, this parameter is ignored
     * @param resultKey   result key, if mapping type is {@link MappingType#MAPPED}, this parameter is ignored
     * @return {@link MethodInvokerContainer}
     */
    public MethodInvokerContainer createContainer(
        Object target, Method method, MappingType mappingType,
        @Nullable String namespace, Class<?> resultType, String resultKey) {
        log.debug("create method container from [{}]", method);
        // get key extractor of result object if necessary
        MethodInvokerContainer.KeyExtractor keyExtractor = getKeyExtractor(mappingType, resultType, resultKey);
        // is proxy object and not declaring by proxy class?
        MethodInvoker methodInvoker = getMethodInvoker(target, method);
        namespace = getNamespace(method, namespace);
        return createMethodInvokerContainer(target, mappingType, namespace, keyExtractor, methodInvoker);
    }

    /**
     * Create a {@link MethodInvokerContainer} from the given method invoker.
     *
     * @param target      method's calling object
     * @param methodInvoker method invoker
     * @param mappingType mapping type
     * @param namespace   namespace, if null, use method name as namespace
     * @param resultType  result type, if mapping type is {@link MappingType#MAPPED}, this parameter is ignored
     * @param resultKey   result key, if mapping type is {@link MappingType#MAPPED}, this parameter is ignored
     * @return {@link MethodInvokerContainer}
     */
    public MethodInvokerContainer createContainer(
        Object target, MethodInvoker methodInvoker, MappingType mappingType,
        String namespace, Class<?> resultType, String resultKey) {
        log.debug("create method container from [{}]", methodInvoker);
        // get key extractor of result object if necessary
        MethodInvokerContainer.KeyExtractor keyExtractor = getKeyExtractor(mappingType, resultType, resultKey);
        // is proxy object and not declaring by proxy class?
        return createMethodInvokerContainer(target, mappingType, namespace, keyExtractor, methodInvoker);
    }

    /**
     * Create a {@link MethodInvokerContainer} from the given method.
     *
     * @param target target
     * @param mappingType mapping type
     * @param namespace namespace
     * @param keyExtractor key extractor, if mapping type is {@link MappingType#MAPPED}, this parameter is ignored
     * @param methodInvoker method invoker
     * @return {@link MethodInvokerContainer} instance
     */
    @Nonnull
    protected MethodInvokerContainer createMethodInvokerContainer(
        Object target, MappingType mappingType, String namespace,
        MethodInvokerContainer.KeyExtractor keyExtractor, MethodInvoker methodInvoker) {
        return new MethodInvokerContainer(namespace, methodInvoker, target, keyExtractor, mappingType);
    }

    /**
     * Get namespace of method container.
     *
     * @param target target
     * @param method method
     * @return namespace
     * @implNote if target is <b>proxy object</b>, invoke method on proxy object,
     * otherwise invoke method on target object
     * @see JdkProxyMethodInvoker
     */
    @Nonnull
    protected MethodInvoker getMethodInvoker(Object target, Method method) {
        MethodInvoker methodInvoker;
        if (Proxy.isProxyClass(target.getClass()) && !Proxy.isProxyClass(method.getDeclaringClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(target);
            methodInvoker = new JdkProxyMethodInvoker(handler, method);
        } else {
            methodInvoker = (t, args) -> ReflectUtil.invoke(t, method, args);
        }
        return methodInvoker;
    }

    /**
     * Get key extractor of result object if necessary.
     *
     * @param mappingType mapping type
     * @param resultType  result type
     * @param resultKey   result key
     * @return key extractor
     */
    @Nullable
    protected MethodInvokerContainer.KeyExtractor getKeyExtractor(
        MappingType mappingType, Class<?> resultType, String resultKey) {
        MethodInvokerContainer.KeyExtractor keyExtractor = null;
        if (mappingType != MappingType.MAPPED) {
            MethodInvoker keyGetter = findKeyGetter(resultType, resultKey);
            keyExtractor = keyGetter::invoke;
        }
        return keyExtractor;
    }

    /**
     * Get namespace from method.
     *
     * @param method     method
     * @param namespace  namespace
     * @return namespace
     */
    protected static String getNamespace(Method method, String namespace) {
        return CharSequenceUtil.emptyToDefault(namespace, method.getName());
    }

    /**
     * Find key getter method of result object.
     *
     * @param resultType result type
     * @param resultKey  result key
     * @return key getter method
     */
    protected MethodInvoker findKeyGetter(Class<?> resultType, String resultKey) {
        MethodInvoker keyGetter = propertyOperator.findGetter(resultType, resultKey);
        Objects.requireNonNull(keyGetter, CharSequenceUtil.format(
            "cannot find getter method [{}] on [{}]", resultKey, resultType
        ));
        return keyGetter;
    }

    /**
     * A {@link MethodInvoker} implementation for JDK proxy object, which invoke method on proxy object.
     */
    @RequiredArgsConstructor
    protected static class JdkProxyMethodInvoker implements MethodInvoker {
        private final InvocationHandler handler;
        private final Method method;
        @SneakyThrows
        @Override
        public Object invoke(Object target, Object... args) {
            return handler.invoke(target, method, args);
        }
    }
}
