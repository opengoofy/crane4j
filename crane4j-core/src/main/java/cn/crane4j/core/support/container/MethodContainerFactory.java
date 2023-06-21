package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Sorted;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Factory for creating data source containers based on methods.
 *
 * @author huangchengxing
 * @see ContainerMethodAnnotationProcessor
 * @see DefaultMethodContainerFactory
 * @see CacheableMethodContainerFactory
 */
public interface MethodContainerFactory extends Sorted {

    /**
     * Whether the method is supported.
     *
     * @param source method's calling object
     * @param method method
     * @param annotations annotations
     * @return true if supported, false otherwise
     */
    boolean support(Object source, Method method, Collection<ContainerMethod> annotations);

    /**
     * Adapt methods to data source containers.
     *
     * @param source method's calling object
     * @param method method
     * @param annotations annotations
     * @return data source containers
     */
    List<Container<Object>> get(Object source, Method method, Collection<ContainerMethod> annotations);


}
