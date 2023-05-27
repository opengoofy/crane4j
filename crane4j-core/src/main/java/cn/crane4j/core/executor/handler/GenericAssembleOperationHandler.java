package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.support.reflect.PropertyOperator;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generic implementation of {@link AssembleOperationHandler} based on reflection.
 *
 * @author huangchengxing
 * @see PropertyOperator
 */
@RequiredArgsConstructor
public class GenericAssembleOperationHandler extends AbstractAssembleOperationHandler<AssembleOperationTarget> {

    /**
     * propertyOperator
     */
    protected final PropertyOperator propertyOperator;

    /**
     * Split the {@link AssembleExecution} into pending objects and wrap it as {@link Target}.
     *
     * @param executions executions
     * @return {@link Target}
     */
    @Override
    protected Collection<AssembleOperationTarget> collectToEntities(Collection<AssembleExecution> executions) {
        List<AssembleOperationTarget> targets = new ArrayList<>();
        for (AssembleExecution execution : executions) {
            String key = execution.getOperation().getKey();
            execution.getTargets().stream()
                .map(t -> createTarget(execution, t, propertyOperator.readProperty(t.getClass(), t, key)))
                .forEach(targets::add);
        }
        return targets;
    }

    /**
     * Create a {@link AssembleOperationTarget} instance.
     *
     * @param execution execution
     * @param origin    origin
     * @param keyValue  key value
     * @return {@link AssembleOperationTarget}
     */
    protected AssembleOperationTarget createTarget(AssembleExecution execution, Object origin, Object keyValue) {
        return new AssembleOperationTarget(execution, origin, keyValue);
    }

    /**
     * Obtain the corresponding data source object from the data source container based on the entity's key value.
     *
     * @param container container
     * @param targets   targets
     * @return source objects
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<Object, Object> getSourcesFromContainer(Container<?> container, Collection<AssembleOperationTarget> targets) {
        Set<Object> keys = targets.stream()
            .map(AssembleOperationTarget::getKey)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return (Map<Object, Object>)((Container<Object>)container).get(keys);
    }

    /**
     * Get the data source object associated with the target object.
     *
     * @param target  target
     * @param sources sources
     * @return data source object associated with the target object
     */
    @Override
    protected Object getTheAssociatedSource(AssembleOperationTarget target, Map<Object, Object> sources) {
        return sources.get(target.getKey());
    }

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    @Override
    protected void completeMapping(Object source, AssembleOperationTarget target) {
        AssembleExecution execution = target.getExecution();
        Set<PropertyMapping> mappings = execution.getOperation().getPropertyMappings();
        for (PropertyMapping mapping : mappings) {
            mappingProperty(target, source, mapping);
        }
    }

    private void mappingProperty(AssembleOperationTarget entity, Object source,PropertyMapping mapping) {
        Object sourceValue = mapping.hasSource() ?
            propertyOperator.readProperty(source.getClass(), source, mapping.getSource()) : source;
        if (Objects.nonNull(sourceValue)) {
            Object target = entity.getOrigin();
            propertyOperator.writeProperty(target.getClass(), target, mapping.getReference(), sourceValue);
        }
    }
}
