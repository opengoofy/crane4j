package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

/**
 * Property value mapping strategy.
 *
 * @author huangchengxing
 * @since 2.1.0
 */
public interface PropertyMappingStrategy {

    /**
     * Get strategy name.
     *
     * @return name
     */
    String getName();
    
    /**
     * Map {@code sourceValue} to reference fields in target.
     *
     * @param target target object
     * @param source source object
     * @param sourceValue source value
     * @param propertyMapping property mapping
     * @param mapping mapping action
     */
    void doMapping(
        Object target, Object source, @Nullable Object sourceValue, 
        PropertyMapping propertyMapping, Consumer<Object> mapping);
}
