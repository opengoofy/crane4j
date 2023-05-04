package cn.crane4j.core.support.converter;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.ArrayUtils;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * A method invoker that can be called and convert parameters.
 *
 * @author huangchengxing
 * @see ConverterManager
 * @since 1.3.0
 */
@RequiredArgsConstructor
public class ParameterConvertibleMethodInvoker implements MethodInvoker {

    private static final Object[] EMPTY_PARAMS = new Object[0];
    private final MethodInvoker methodInvoker;
    private final ConverterManager converterManager;
    private final Class<?>[] parameterTypes;

    /**
     * Create {@link MethodInvoker} according to the specified parameter types.
     *
     * @param methodInvoker method invoker
     * @param converterManager    converterManager
     * @param parameterTypes parameter types
     * @return {@link MethodInvoker}, if the number of parameters is 0, return the original {@link MethodInvoker}
     */
    public static MethodInvoker create(
        MethodInvoker methodInvoker, ConverterManager converterManager, Class<?>[] parameterTypes) {
        Objects.requireNonNull(methodInvoker);
        Objects.requireNonNull(converterManager);
        return ArrayUtils.length(parameterTypes) == 0 ?
            methodInvoker : new ParameterConvertibleMethodInvoker(methodInvoker, converterManager, parameterTypes);
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
        Object[] actualArgs = resolveInvocationArguments(args);
        return methodInvoker.invoke(target, actualArgs);
    }

    private Object[] resolveInvocationArguments(Object... args) {
        int parameterCount = parameterTypes.length;
        if (parameterCount == 0) {
            return EMPTY_PARAMS;
        }
        // if args is null, return empty array
        if (ArrayUtils.isEmpty(args)) {
            return new Object[parameterCount];
        }
        // if the number of parameters is not equal, convert the parameters
        Object[] actualArgs = new Object[parameterCount];
        if (parameterCount >= args.length) {
            for (int i = 0; i < args.length; i++) {
                actualArgs[i] = converterManager.convert(args[i], parameterTypes[i]);
            }
        } else {
            for (int i = 0; i < parameterCount; i++) {
                actualArgs[i] = converterManager.convert(args[i], parameterTypes[i]);
            }
        }
        return actualArgs;
    }
}
