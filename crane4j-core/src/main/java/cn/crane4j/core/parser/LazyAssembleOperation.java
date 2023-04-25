package cn.crane4j.core.parser;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import lombok.Getter;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A AssembleOperation that container is initialized lazily.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
public class LazyAssembleOperation extends SimpleKeyTriggerOperation implements AssembleOperation {

    @Getter
    private final Set<PropertyMapping> propertyMappings;
    private final Supplier<Container<?>> containerSupplier;
    @Getter
    private final AssembleOperationHandler assembleOperationHandler;

    @Override
    public Container<?> getContainer() {
        return containerSupplier.get();
    }

    public LazyAssembleOperation(
        String key, int sort,
        Set<PropertyMapping> propertyMappings, Supplier<Container<?>> containerSupplier,
        AssembleOperationHandler assembleOperationHandler) {
        super(key, sort);
        this.propertyMappings = propertyMappings;
        this.containerSupplier = containerSupplier;
        this.assembleOperationHandler = assembleOperationHandler;
    }
}
