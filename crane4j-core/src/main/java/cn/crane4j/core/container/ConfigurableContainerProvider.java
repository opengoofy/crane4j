package cn.crane4j.core.container;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.callback.ContainerRegisterAware;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

/**
 * Configurable {@link ContainerProvider} implementation, supporting {@link ContainerRegisterAware} callbacks.
 *
 * @author huangchengxing
 * @see ContainerRegisterAware
 */
public interface ConfigurableContainerProvider extends ContainerProvider {

    /**
     * Add a {@link ContainerRegisterAware} callback.
     *
     * @param containerRegisterAware callback
     */
    void addContainerRegisterAware(ContainerRegisterAware containerRegisterAware);

    /**
     * Replace the registered container.
     * <ul>
     *     <li>if the container is not registered, it will be added;</li>
     *     <li>if {@code replacer} return {@code null}, the old container will be deleted;</li>
     * </ul>
     *
     * @param namespace namespace
     * @param replacer replacer
     * @return old container
     */
    @Nullable
    Container<?> replaceContainer(String namespace, UnaryOperator<Container<?>> replacer);

    /**
     * Register container.
     *
     * @param container container
     * @throws Crane4jException thrown when the namespace of the container has been registered
     * @see ContainerRegisterAware
     */
    void registerContainer(Container<?> container);

    /**
     * Whether the container has been registered.
     *
     * @param namespace namespace
     * @return boolean
     */
    boolean containsContainer(String namespace);
}
