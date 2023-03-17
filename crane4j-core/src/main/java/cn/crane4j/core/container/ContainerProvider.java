package cn.crane4j.core.container;

import cn.crane4j.core.support.callback.ContainerRegisterAware;

/**
 * A data source container provider.
 *
 * @author huangchengxing
 * @see ContainerRegisterAware
 */
public interface ContainerProvider {

    /**
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     */
    Container<?> getContainer(String namespace);
}
