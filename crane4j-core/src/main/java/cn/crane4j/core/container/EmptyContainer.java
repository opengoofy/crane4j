package cn.crane4j.core.container;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * <p>An empty data source container for placeholders,
 * it does not provide any data and data registration function itself.<br />
 * When an assembly operation specifies to use the data source container,
 * it actually means that the operation object itself will be used as the data source object.
 *
 * @author huangchengxing
 */
public class EmptyContainer implements Container<Object> {

    public static final EmptyContainer INSTANCE = new EmptyContainer();
    public static final String NAMESPACE = "";

    /**
     * Gets the namespace of the data source container,
     * The value always defaults to an empty string
     *
     * @return namespace
     */
    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    /**
     * <p>Enter a batch of key values to return data source objects grouped by key values.
     * always return an empty collection.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @Override
    public Map<Object, ?> get(Collection<Object> keys) {
        return Collections.emptyMap();
    }
}
