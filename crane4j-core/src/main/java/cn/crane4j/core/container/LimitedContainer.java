package cn.crane4j.core.container;

import java.util.Map;

/**
 * A {@link Container} that has a limit on the number of data source objects it can contain.
 *
 * @author huangchengxing
 * @see ImmutableMapContainer
 */
public interface LimitedContainer<K> extends Container<K> {

    /**
     * Get all data source objects in the container.
     *
     * @return all elements
     */
    Map<K, ?> getAll();
}
