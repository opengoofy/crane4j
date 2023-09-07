package cn.crane4j.core.parser.handler.strategy;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A basic implementation of {@link PropertyMappingStrategyManager}.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
public class SimplePropertyMappingStrategyManager implements PropertyMappingStrategyManager {

    /**
     * Property mapping strategies.
     */
    private final Map<String, PropertyMappingStrategy> propertyMappingStrategies = new HashMap<>();

    /**
     * Register property mapping strategy.
     *
     * @param strategy strategy
     * @see PropertyMappingStrategy
     * @since 2.1.0
     */
    public void addPropertyMappingStrategy(@NonNull PropertyMappingStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy must not null");
        propertyMappingStrategies.put(strategy.getName(), strategy);
    }

    /**
     * Get property mapping strategy by name.
     *
     * @param name name
     * @return property mapping strategy
     */
    @Override
    public @Nullable PropertyMappingStrategy getPropertyMappingStrategy(String name) {
        return propertyMappingStrategies.get(name);
    }

    /**
     * Remove property mapping strategy.
     *
     * @param name name
     * @return property mapping strategy
     */
    @Override
    public PropertyMappingStrategy removePropertyMappingStrategy(String name) {
        return propertyMappingStrategies.remove(name);
    }

    /**
     * Get all property mapping strategies.
     *
     * @return collection of property mapping strategies
     */
    @Override
    public Collection<PropertyMappingStrategy> getAllPropertyMappingStrategies() {
        return propertyMappingStrategies.values();
    }
}
