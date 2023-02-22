package cn.crane4j.core.container;

import cn.crane4j.core.support.Sorted;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Factory for creating data source containers based on methods.
 *
 * @author huangchengxing
 * @see DefaultMethodContainerFactory
 * @see CacheableMethodContainerFactory
 */
public interface MethodContainerFactory extends Sorted {

    /**
     * Whether the method is supported.
     *
     * @param source method's calling object
     * @param method method
     * @return true if supported, false otherwise
     */
    boolean support(Object source, Method method);

    /**
     * Adapt methods to data source containers.
     *
     * @param source method's calling object
     * @param method method
     * @return data source containers
     */
    List<Container<Object>> get(Object source, Method method);
}
