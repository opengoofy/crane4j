package cn.crane4j.core.parser.operation;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.key.KeyResolver;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
     * Get the type of key property.
     *
     * @return key type
     */
    @Nullable
    Class<?> getKeyType();

    /**
     * Set key property type.
     *
     * @param keyType key type
     */
    void setKeyType(Class<?> keyType);

    /**
     * Get key resolver.
     *
     * @return key resolver
     * @since 2.7.0
     */
    @Nullable
    KeyResolver getKeyResolver();

    /**
     * Set key resolver.
     *
     * @param keyResolver key resolver
     * @since 2.7.0
     */
    void setKeyResolver(@NonNull KeyResolver keyResolver);

    /**
     * Some description of the key which
     * helps {@link #getKeyResolver() resolver} to resolve the key.
     *
     * @return description
     * @since 2.7.0
     */
    @Nullable
    String getKeyDescription();

    /**
     * Set key description.
     *
     * @param keyDescription description
     * @since 2.7.0
     */
    void setKeyDescription(@NonNull String keyDescription);

    /**
     * Get the namespace of data source container.
     *
     * @return container
     */
    String getContainer();

    /**
     * Get operation handler.
     *
     * @return handler
     */
    AssembleOperationHandler getAssembleOperationHandler();

    /**
     * Get property mapping strategy.
     *
     * @return strategy
     * @since 2.1.0
     */
    PropertyMappingStrategy getPropertyMappingStrategy();

    /**
     * Set property mapping strategy.
     * @param strategy strategy name
     * @since 2.1.0
     */
    void setPropertyMappingStrategy(@NonNull PropertyMappingStrategy strategy);
}
