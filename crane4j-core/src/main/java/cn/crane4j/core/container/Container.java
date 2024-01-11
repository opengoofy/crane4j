package cn.crane4j.core.container;

import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;

import java.util.Collection;
import java.util.Map;

/**
 * <p>The source container used to store the provided data objects.<br />
 * The data source that can be used to complete the assembly operation.
 * Any Map set object method that can accept the key value set
 * and return grouping by key value can be used as a data source.
 *
 * <p><strong>Lifecycle</strong>
 * <p>All container instances support registering callback functions
 * during specific stages of their lifecycle, including:
 * <ul>
 *     <li>
 *         <em>When registered</em>: when the container is registered
 *         as a {@link ContainerDefinition} instance to the {@link ContainerManager};
 *     </li>
 *     <li>
 *         <em>When created</em>: when the container transitions from a {@link ContainerDefinition} to an actual instance;
 *     </li>
 *     <li>
 *         <em>When destroyed</em>: when the container instance is destroyed,
 *         this behavior may not affect the corresponding {@link ContainerDefinition};
 *     </li>
 * </ul>
 * By extending the {@link ContainerLifecycleProcessor}(for all container)
 * or implementing {@link Lifecycle}(for specified container class),
 * users can perceive these specific stages and perform modifications and replacement operations on the container.
 *
 * <p><strong>Manger</strong>
 * <p>Containers are typically not used directly in specific operations,
 * but rather managed by a designated {@link ContainerManager} that handles the creation,
 * usage, and destruction process.<br />
 * This allows the associated {@link ContainerLifecycleProcessor} to fully handle the container's lifecycle.
 * <p>When delegating the container to the manager,
 * you have the option to directly register an instance or a factory method used to create the instance with the manager.
 * Alternatively, you can register a {@link ContainerProvider},
 * allowing the container to automatically complete the registration logic when necessary,
 * similar to the internal BeanFactory in Spring, where beans are created using FactoryBean.
 *
 * @author huangchengxing
 * @param <K> key type
 * @see ContainerManager
 * @see ContainerLifecycleProcessor
 * @see LambdaContainer
 * @see MethodInvokerContainer
 * @see EmptyContainer
 * @see cn.crane4j.core.cache.CacheableContainer
 */
public interface Container<K> {

    /**
     * <p>Namespace of empty container.
     *
     * @see #empty()
     * @see EmptyContainer
     */
    String EMPTY_CONTAINER_NAMESPACE = "";

    /**
     * <p>Get an empty data source container.<br />
     * When an assembly operation specifies to use the data source container,
     * the operation object itself will be used as the data source object.
     *
     * @return container
     * @see EmptyContainer
     */
    @SuppressWarnings("unchecked")
    static <K> Container<K> empty() {
        return (Container<K>)EmptyContainer.INSTANCE;
    }

    /**
     * Gets the namespace of the data source container,
     * which should be globally unique.
     *
     * @return namespace
     */
    String getNamespace();

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    Map<K, ?> get(Collection<K> keys);

    /**
     * Simple lifecycle callback of container
     */
    interface Lifecycle {

        /**
         * Callback when container created.
         */
        default void init() {
            // do nothing
        }

        /**
         * Callback when container destroyed.
         */
        default void destroy() {
            // do nothing
        }
    }
}
