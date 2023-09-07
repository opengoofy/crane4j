package cn.crane4j.core.parser.handler.strategy;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * Manager for property mapping strategy.
 *
 * @author huangchengxing
 * @see PropertyMappingStrategy
 * @see SimplePropertyMappingStrategyManager
 * @since 2.2.0
 */
public interface PropertyMappingStrategyManager {

    /**
     * Register property mapping strategy.
     *
     * @param propertyMappingStrategy property mapping strategy
     */
    void addPropertyMappingStrategy(@NonNull PropertyMappingStrategy propertyMappingStrategy);

    /**
     * Get property mapping strategy by name.
     *
     * @param name name
     * @return property mapping strategy
     */
    @Nullable
    PropertyMappingStrategy getPropertyMappingStrategy(String name);

    /**
     * Remove property mapping strategy.
     *
     * @param name name
     * @return property mapping strategy
     */
    PropertyMappingStrategy removePropertyMappingStrategy(String name);

    /**
     * Get all property mapping strategies.
     *
     * @return collection of property mapping strategies
     */
    Collection<PropertyMappingStrategy> getAllPropertyMappingStrategies();
}
