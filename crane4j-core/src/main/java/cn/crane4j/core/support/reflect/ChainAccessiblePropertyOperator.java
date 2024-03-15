package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * The wrapper class of {@link PropertyOperator} that
 * adds support for nested bean chain operations to the original operator.
 *
 * @author huangchengxing
 * @see CacheableChainAccessiblePropertyOperator
 * @since 1.1.0
 */
@RequiredArgsConstructor
public class ChainAccessiblePropertyOperator implements PropertyOperator {

    /**
     * original operator
     */
    protected final PropertyOperator delegate;

    /**
     * splitter
     */
    private final Function<String, String[]> splitter;

    /**
     * Create an {@link ChainAccessiblePropertyOperator} instance,
     * and use the default splitter to separate input chain operators based on the {@code "."} character.
     *
     * @param delegate property operator
     * @see DefaultSplitter
     */
    public ChainAccessiblePropertyOperator(PropertyOperator delegate) {
        this(delegate, new DefaultSplitter("."));
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
        PropDesc delegateDesc = delegate.getPropertyDescriptor(targetType);
        return new ChainAccessPropDesc(targetType, delegateDesc);
    }

    /**
     * Create a chain setter.
     *
     * @param splitPropertyChain split property chain
     * @return chain setter
     */
    protected MethodInvoker chainSetter(String[] splitPropertyChain) {
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
                target = delegate.readProperty(target.getClass(), target, targetProp);
                deep++;
            }
            // reached the deepest point?
            if (deep == targetDeep && Objects.nonNull(target)) {
                delegate.writeProperty(target.getClass(), target, splitPropertyChain[targetDeep], args[0]);
            }
            return null;
        };
    }

    /**
     * Create a chain getter.
     *
     * @param splitPropertyChain split property chain
     * @return chain getter
     */
    protected MethodInvoker chainGetter(String[] splitPropertyChain) {
        return (target, args) -> {
            for (String prop : splitPropertyChain) {
                if (Objects.isNull(target)) {
                    return null;
                }
                target = delegate.readProperty(target.getClass(), target, prop);
            }
            return target;
        };
    }

    /**
     * The property descriptor that supports chain property access.
     *
     * @author huangchengxing
     * @since 2.7.0
     */
    protected class ChainAccessPropDesc extends AbstractPropDesc {

        private final PropDesc delegate;

        public ChainAccessPropDesc(Class<?> beanType, PropDesc delegate) {
            super(beanType);
            this.delegate = delegate;
        }

        /**
         * Get the getter method.
         *
         * @param propertyName property name
         * @return property getter
         */
        @Override
        public @Nullable MethodInvoker getGetter(String propertyName) {
            String[] properties = splitter.apply(propertyName);
            if (properties.length <= 1) {
                return delegate.getGetter(propertyName);
            }
            // only chain property access will be processed
            return super.getGetter(propertyName);
        }

        /**
         * Get the setter method.
         *
         * @param propertyName property name
         * @return property setter
         */
        @Override
        public @Nullable MethodInvoker getSetter(String propertyName) {
            String[] properties = splitter.apply(propertyName);
            if (properties.length <= 1) {
                return delegate.getSetter(propertyName);
            }
            // only chain property access will be processed
            return super.getSetter(propertyName);
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
            String[] properties = splitter.apply(propertyName);
            return chainGetter(properties);
        }

        /**
         * Get setter method.
         *
         * @param propertyName property name
         * @return setter method
         */
        @Nullable
        @Override
        protected MethodInvoker findSetter(String propertyName) {
            String[] properties = splitter.apply(propertyName);
            return chainSetter(properties);
        }
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
