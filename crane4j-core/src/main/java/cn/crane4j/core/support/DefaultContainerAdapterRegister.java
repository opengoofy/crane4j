package cn.crane4j.core.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation for {@link DefaultContainerAdapterRegister}.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
public class DefaultContainerAdapterRegister implements ContainerAdapterRegister {

    /**
     * default global instance.
     */
    public static final DefaultContainerAdapterRegister INSTANCE = new DefaultContainerAdapterRegister();

    /**
     * registered adapters
     */
    protected final Map<Class<?>, Adapter> registeredAdapters = new LinkedHashMap<>();

    /**
     * Create instance with default adapters.
     */
    public DefaultContainerAdapterRegister() {
        initDefaultAdapters();
    }

    /**
     * Init default adapters.
     */
    @SuppressWarnings("unchecked")
    protected void initDefaultAdapters() {
        registerAdapter(Map.class, (n, t) -> Containers.forMap(n, (Map<Object, ?>) t));
        registerAdapter(Container.class, (n, t) -> (Container<Object>)t);
        registerAdapter(DataProvider.class, (n, t) -> Containers.forLambda(n, (DataProvider<Object, Object>) t));
    }

    /**
     * Get target type.
     *
     * @param targetType target type
     * @return {@link Adapter} instance.
     */
    @Nullable
    @Override
    public Adapter getAdapter(Class<?> targetType) {
        return findAdaptor(targetType);
    }

    /**
     * Register adapter.
     *
     * @param targetType target type
     * @param adapter    adapter
     */
    @Override
    public void registerAdapter(Class<?> targetType, Adapter adapter) {
        registeredAdapters.put(targetType, adapter);
    }

    @Nullable
    private Adapter findAdaptor(Class<?> targetType) {
        Adapter adapter = registeredAdapters.get(targetType);
        if (Objects.nonNull(adapter)) {
            return adapter;
        }
        return registeredAdapters.entrySet().stream()
            .filter(e -> e.getKey().isAssignableFrom(targetType))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);
    }
}
