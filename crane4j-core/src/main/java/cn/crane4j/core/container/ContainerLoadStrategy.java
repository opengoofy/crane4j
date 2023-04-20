package cn.crane4j.core.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Container load strategy.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public enum ContainerLoadStrategy {

    /**
     * Load and cache container singletons only when called.
     */
    LAZY("lazy", Lazy::new),

    /**
     * Immediately load and cache the container upon creation
     */
    HUNGRY("hungry", Hungry::new),

    /**
     * Every time a container is obtained, it is directly obtained from the container factory.
     */
    EVERY("every", Every::new);

    /**
     * container load strategy
     */
    private static final Map<String, ContainerLoadStrategy> CONTAINER_LOAD_STRATEGY_CACHE = Stream.of(ContainerLoadStrategy.values())
        .collect(Collectors.toMap(ContainerLoadStrategy::getName, Function.identity()));

    /**
     * name of strategy
     */
    @Getter
    private final String name;

    /**
     * container loader factory
     */
    private final UnaryOperator<Supplier<Container<?>>> containerLoaderFactory;

    /**
     * Get strategy by name
     *
     * @param name name
     * @return {@link ContainerLoadStrategy}
     */
    @Nullable
    public static ContainerLoadStrategy get(String name) {
        return CONTAINER_LOAD_STRATEGY_CACHE.get(name);
    }

    /**
     * Get container loader base on given {@code containerFactory}.
     *
     * @param containerFactory container factory
     * @return container loader
     */
    public Supplier<Container<?>> load(Supplier<Container<?>> containerFactory) {
        return containerLoaderFactory.apply(containerFactory);
    }

    @RequiredArgsConstructor
    private static class Lazy implements Supplier<Container<?>> {
        private volatile Container<?> value;
        private final Supplier<Container<?>> supplier;
        @Override
        public Container<?> get() {
            if (value == null) {
                synchronized (this) {
                    if (value == null) {
                        value = supplier.get();
                    }
                }
            }
            return value;
        }
    }

    private static class Hungry implements Supplier<Container<?>> {
        private final Container<?> container;
        public Hungry(Supplier<Container<?>> supplier) {
            this.container = supplier.get();
        }
        @Override
        public Container<?> get() {
            return container;
        }
    }

    @RequiredArgsConstructor
    private static class Every implements Supplier<Container<?>> {
        private final Supplier<Container<?>> supplier;
        @Override
        public Container<?> get() {
            return supplier.get();
        }
    }
}
