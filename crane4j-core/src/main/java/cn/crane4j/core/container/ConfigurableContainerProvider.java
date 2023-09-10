package cn.crane4j.core.container;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Configurable container provider.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
public interface ConfigurableContainerProvider extends ContainerProvider {

    /**
     * Register container.
     *
     * @param container container
     * @return registration info
     */
    Object registerContainer(@NonNull Container<?> container);
}
