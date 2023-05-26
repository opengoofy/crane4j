package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Definition of container.
 *
 * @author huangchengxing
 * @see ContainerLifecycleProcessor
 */
public interface ContainerDefinition {

    /**
     * Create a simple container definition.
     *
     * @param namespace namespace
     * @param providerName provider name
     * @param containerFactory container factory
     * @return container definition
     */
    static SimpleContainerDefinition create(
            String namespace, @Nullable String providerName, @Nullable Supplier<Container<Object>> containerFactory) {
        return new SimpleContainerDefinition(namespace, providerName, containerFactory);
    }

    /**
     * Get namespace of container
     *
     * @return namespace
     */
    String getNamespace();

    /**
     * Get container provider name.
     *
     * @return provider name
     */
    String getProviderName();

    /**
     * Get container factory.
     *
     * @return container factory method
     */
    Supplier<Container<Object>> getContainerFactory();

    /**
     * Set container factory.
     *
     * @param factory factory
     */
    void setContainerFactory(Supplier<Container<Object>> factory);

    /**
     * Set init method, it takes effect simultaneously with {@link Container.Lifecycle#init()}.
     *
     * @param initMethod init method
     */
    void setInitMethod(@Nullable Consumer<Container<Object>> initMethod);

    /**
     * Get init method.
     *
     * @return init method
     */
    @Nullable
    Consumer<Container<Object>> getInitMethod();
    
    /**
     * Set destroy method, it takes effect simultaneously with {@link Container.Lifecycle#destroy()}.
     *
     * @param destroyMethod destroy method
     */
    void setDestroyMethod(@Nullable Consumer<Container<Object>> destroyMethod);

    /**
     * Get destroy method.
     *
     * @return destroy method
     */
    @Nullable
    Consumer<Container<Object>> getDestroyMethod();

    /**
     * Create {@link Container} by given {@code factory}.
     *
     * @return {@link Container} instance.
     */
    @SuppressWarnings("unchecked")
    default <K> Container<K> createContainer() {
        return (Container<K>) getContainerFactory().get();
    }

    /**
     * Container definition.
     *
     * @author huangchengxing
     * @see ContainerLifecycleProcessor
     */
    @Setter
    @Getter
    class SimpleContainerDefinition implements ContainerDefinition {
    
        /**
         * Namespace of container.
         */
        private final String namespace;
    
        /**
         * Name of container provider. If not create by provider, it will be {@code null}.
         */
        @Nullable
        private final String providerName;
    
        /**
         * Container factory.
         */
        @Nullable
        private Supplier<Container<Object>> containerFactory;
    
        /**
         * Init method.
         */
        @Nullable
        private Consumer<Container<Object>> initMethod;
    
        /**
         * Destroy method.
         */
        private Consumer<Container<Object>> destroyMethod;
    
        /**
         * Create an instance.
         *
         * @param namespace namespace
         * @param providerName provider name
         * @param containerFactory container factory
         */
        SimpleContainerDefinition(
                String namespace, @Nullable String providerName, @Nullable Supplier<Container<Object>> containerFactory) {
            this.namespace = namespace;
            this.providerName = providerName;
            this.containerFactory = containerFactory;
        }
    }
}
