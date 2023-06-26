package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * The wrapper class of {@link PropertyOperator} that
 * adds support for nested bean chain operations to the original operator.
 *
 * @author huangchengxing
 * @since 1.1.0
 */
@RequiredArgsConstructor
public class ChainAccessiblePropertyOperator implements PropertyOperator {

    /**
     * original operator
     */
    protected final PropertyOperator propertyOperator;

    /**
     * splitter
     */
    private final Function<String, String[]> splitter;

    /**
     * Create an {@link ChainAccessiblePropertyOperator} comparator,
     * and use the default splitter to separate input chain operators based on the {@code "."} character.
     *
     * @param propertyOperator property operator
     * @see DefaultSplitter
     */
    public ChainAccessiblePropertyOperator(PropertyOperator propertyOperator) {
        this(propertyOperator, new DefaultSplitter("."));
    }

    /**
     * Get the specified property value.
     *
     * @param targetType   target type
     * @param target       target
     * @param propertyName property name
     * @return property value
     */
    @Nullable
    @Override
    public Object readProperty(Class<?> targetType, Object target, String propertyName) {
        MethodInvoker invoker = findGetter(targetType, propertyName);
        return Objects.isNull(invoker) ? null : invoker.invoke(target);
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
        String[] properties = splitter.apply(propertyName);
        if (properties.length <= 1) {
            return propertyOperator.findGetter(targetType, propertyName);
        }
        return chainGetter(properties);
    }

    /**
     * Set the specified property value.
     *
     * @param targetType   target type
     * @param target       target
     * @param propertyName property name
     * @param value        property value
     */
    @Override
    public void writeProperty(Class<?> targetType, Object target, String propertyName, Object value) {
        MethodInvoker invoker = findSetter(targetType, propertyName);
        if (Objects.nonNull(invoker)) {
            invoker.invoke(target, value);
        }
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
        String[] properties = splitter.apply(propertyName);
        if (properties.length <= 1) {
            return propertyOperator.findSetter(targetType, propertyName);
        }
        return chainSetter(properties);
    }

    private MethodInvoker chainGetter(String[] splitPropertyChain) {
        return (target, args) -> {
            for (String prop : splitPropertyChain) {
                if (Objects.isNull(target)) {
                    return null;
                }
                target = propertyOperator.readProperty(target.getClass(), target, prop);
            }
            return target;
        };
    }

    private MethodInvoker chainSetter(String[] splitPropertyChain) {
        return (target, args) -> {
            int targetDeep = splitPropertyChain.length - 1;
            String targetProp;
            int deep = 0;
            // found penultimate nested object
            while (deep < targetDeep) {
                if (Objects.isNull(target)) {
                    return null;
                }
                targetProp = splitPropertyChain[deep];
                // go to next level
                target = propertyOperator.readProperty(target.getClass(), target, targetProp);
                deep++;
            }
            // reached the deepest point?
            if (deep == targetDeep && Objects.nonNull(target)) {
                propertyOperator.writeProperty(target.getClass(), target, splitPropertyChain[targetDeep], args[0]);
            }
            return null;
        };
    }

    /**
     * <p>The default splitter implementation that
     * uses {@link String#split} to split the input string
     * into multiple attributes by {@code separator}.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    public static class DefaultSplitter implements Function<String, String[]> {
        private final String separator;
        private final Map<String, String[]> caches = CollectionUtils.newWeakConcurrentMap();
        @Override
        public String[] apply(String propertyName) {
            return CollectionUtils.computeIfAbsent(
                caches, propertyName, p -> propertyName.split("\\" + separator)
            );
        }
    }
}
