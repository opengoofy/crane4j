package cn.crane4j.core.container;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.callback.ContainerRegisterAware;

/**
 * A data source container provider.
 *
 * @author huangchengxing
 * @see ContainerRegisterAware
 * @since 1.3.0
 */
public interface ContainerProvider {

    /**
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     * @throws Crane4jException thrown when the container is not registered
     */
    Container<?> getContainer(String namespace);
}
