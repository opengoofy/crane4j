package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.executor.AssembleOperationHandler;
import lombok.Getter;

import java.util.Set;

/**
 * {@link AssembleOperation}的基本实现
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
