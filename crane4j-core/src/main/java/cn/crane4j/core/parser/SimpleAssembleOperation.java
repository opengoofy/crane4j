package cn.crane4j.core.parser;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import lombok.Getter;

import java.util.Set;

/**
 * Basic implementation of {@link AssembleOperation}.
 *
 * @author huangchengxing
 */
@Getter
public class SimpleAssembleOperation extends SimpleKeyTriggerOperation implements AssembleOperation {

    private final Set<PropertyMapping> propertyMappings;
    private final Container<?> container;
    private final AssembleOperationHandler assembleOperationHandler;

    public SimpleAssembleOperation(
        String key, int sort,
        Set<PropertyMapping> propertyMappings, Container<?> container,
        AssembleOperationHandler assembleOperationHandler) {
        super(key, sort);
        this.propertyMappings = propertyMappings;
        this.container = container;
        this.assembleOperationHandler = assembleOperationHandler;
    }

    public SimpleAssembleOperation(
        String key,
        Set<PropertyMapping> propertyMappings, Container<?> container,
        AssembleOperationHandler assembleOperationHandler) {
        this(key, Integer.MAX_VALUE, propertyMappings, container, assembleOperationHandler);
    }
}
