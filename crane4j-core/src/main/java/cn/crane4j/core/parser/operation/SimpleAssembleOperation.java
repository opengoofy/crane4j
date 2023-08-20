package cn.crane4j.core.parser.operation;

import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.handler.strategy.OverwriteNotNullMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Basic implementation of {@link AssembleOperation}.
 *
 * @author huangchengxing
 */
@Getter
public class SimpleAssembleOperation extends SimpleKeyTriggerOperation implements AssembleOperation {

    private final Set<PropertyMapping> propertyMappings;
    private final String container;
    private final AssembleOperationHandler assembleOperationHandler;
    @Setter
    private PropertyMappingStrategy propertyMappingStrategy = OverwriteNotNullMappingStrategy.INSTANCE;

    public SimpleAssembleOperation(
        String key, int sort,
        Set<PropertyMapping> propertyMappings, String container,
        AssembleOperationHandler assembleOperationHandler) {
        super(key, sort);
        this.propertyMappings = propertyMappings;
        this.container = container;
        this.assembleOperationHandler = assembleOperationHandler;
    }

    public SimpleAssembleOperation(
        String key,
        Set<PropertyMapping> propertyMappings, String container,
        AssembleOperationHandler assembleOperationHandler) {
        this(key, Integer.MAX_VALUE, propertyMappings, container, assembleOperationHandler);
    }
}
