package cn.crane4j.core.support.reflect;

import cn.crane4j.core.support.MethodInvoker;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A cacheable version of {@link ChainAccessiblePropertyOperator},
 * which caches the getter and setter of the nested property chain.<br/>
 * This class is used to improve the performance of nested property chain operations,
 * but it is not supported that the property value type will change after the first call.
 *
 * @author huangchengxing
 * @since 2.7.0
 */
public class CacheableChainAccessiblePropertyOperator extends ChainAccessiblePropertyOperator {

    /**
     * Create a new {@link CacheableChainAccessiblePropertyOperator} instance.
     *
     * @param delegate delegate
     */
    public CacheableChainAccessiblePropertyOperator(PropertyOperator delegate) {
        super(delegate);
    }

    /**
     * Create a new {@link CacheableChainAccessiblePropertyOperator} instance.
     *
     * @param delegate property operator
     * @param splitter splitter
     */
    public CacheableChainAccessiblePropertyOperator(PropertyOperator delegate, Function<String, String[]> splitter) {
        super(delegate, splitter);
    }

    /**
     * Create a chain getter.
     *
     * @param splitPropertyChain split property chain
     * @return chain getter
     */
    @Override
    protected MethodInvoker chainGetter(String[] splitPropertyChain) {
        return new ChainGetter(splitPropertyChain);
    }

    /**
     * Create a chain setter.
     *
     * @param splitPropertyChain split property chain
     * @return chain setter
     */
    @Override
    protected MethodInvoker chainSetter(String[] splitPropertyChain) {
        return new ChainSetter(splitPropertyChain);
    }

    @RequiredArgsConstructor
    private class ChainGetter implements MethodInvoker {

        private final String[] splitPropertyChain;
        private volatile boolean initialized = false;
        private MethodInvoker[] getterCaches;

        @Override
        public Object invoke(Object target, Object... args) {
            if (!this.initialized) {
                synchronized (this) {
                    if (!this.initialized) {
                        return invokeAndInit(target);
                    }
                }
            }
            return invokeIfInitialized(target);
        }

        private synchronized Object invokeAndInit(Object target) {
            this.getterCaches = new MethodInvoker[splitPropertyChain.length];
            for (int i = 0; i < splitPropertyChain.length; i++) {
                if (Objects.isNull(target)) {
                    return null;
                }
                MethodInvoker getter = delegate.findGetter(target.getClass(), splitPropertyChain[i]);
                if (Objects.isNull(getter)) {
                    return null;
                }
                getterCaches[i] = getter;
                target = getter.invoke(target);
            }
            this.initialized = true;
            return target;
        }

        @Nullable
        private Object invokeIfInitialized(Object target) {
            for (MethodInvoker getter : getterCaches) {
                if (Objects.isNull(target)) {
                    return null;
                }
                target = getter.invoke(target);
            }
            return target;
        }
    }

    @RequiredArgsConstructor
    private class ChainSetter implements MethodInvoker {

        private final String[] splitPropertyChain;
        private volatile boolean initialized = false;
        private MethodInvoker[] getterCaches;
        private MethodInvoker setterCache;

        @Override
        public Object invoke(Object target, Object... args) {
            if (!this.initialized) {
                synchronized (this) {
                    if (!this.initialized) {
                        return invokeAndInit(target, args);
                    }
                }
            }
            return invokeIfInitialized(target, args);
        }

        private Object invokeAndInit(Object target, Object... args) {
            int targetDeep = splitPropertyChain.length - 1;
            this.getterCaches = new MethodInvoker[targetDeep];
            String targetProp;
            int deep = 0;
            // found penultimate nested object
            while (deep < targetDeep) {
                if (Objects.isNull(target)) {
                    return null;
                }
                // go to next level
                targetProp = splitPropertyChain[deep];
                MethodInvoker getter = delegate.findGetter(target.getClass(), targetProp);
                if (Objects.isNull(getter)) {
                    return null;
                }
                this.getterCaches[deep] = getter;
                target = getter.invoke(target);
                deep++;
            }
            // reached the deepest point?
            if (deep == targetDeep && Objects.nonNull(target)) {
                this.setterCache = delegate.findSetter(target.getClass(), splitPropertyChain[targetDeep]);
                if (Objects.isNull(setterCache)) {
                    return null;
                }
                this.setterCache.invoke(target, args[0]);
                this.initialized = true;
            }
            return target;
        }

        @Nullable
        private Object invokeIfInitialized(Object target, Object[] args) {
            for (MethodInvoker getter : getterCaches) {
                if (Objects.isNull(target)) {
                    return null;
                }
                target = getter.invoke(target);
            }
            return Objects.nonNull(target) ? setterCache.invoke(target, args) : null;
        }
    }
}
