package cn.crane4j.core.container;

/**
 * A data source container provider.
 *
 * @author huangchengxing
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
