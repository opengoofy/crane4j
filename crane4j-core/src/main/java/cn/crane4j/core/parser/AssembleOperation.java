package cn.crane4j.core.parser;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;

import java.util.Set;

/**
 * <p>The assembly operation triggered by the specified key.<br />
 * An example is usually used to describe one of the following processes:
 * <ol>
 *     <li>extract a key field value of the target object;</li>
 *     <li>convert the key value to the corresponding data source object through the specified data source container;</li>
 *     <li>map the specific attribute values of the data source object to the attributes of the target object;</li>
 * </ol>
 *
 * <p>The necessary components for completing the above operations can be obtained through the instance:
 * <ol>
 *     <li>{@link #getKey()}: which field of the target object is the key field;</li>
 *     <li>{@link #getContainer()}: which container to get the corresponding data source object through the key field value;</li>
 *     <li>
 *         {@link #getPropertyMappings()}: after getting the data source object, which attributes
 *         should be stuffed into which attributes of the target object;
 *     </li>
 *     <li>{@link #getAssembleOperationHandler()}: how to plug these attribute values;</li>
 * </ol>
 *
 * @author huangchengxing
 * @see AssembleOperationHandler
 * @see Container
 * @see PropertyMapping
 * @see SimpleAssembleOperation
 */
public interface AssembleOperation extends KeyTriggerOperation {

    /**
     * Get property mapping.
     *
     * @return mapping
     */
    Set<PropertyMapping> getPropertyMappings();

    /**
     * Get data source container.
     *
     * @return container
     */
    Container<?> getContainer();

    /**
     * Get operation handler.
     *
     * @return handler
     */
    AssembleOperationHandler getAssembleOperationHandler();
}
