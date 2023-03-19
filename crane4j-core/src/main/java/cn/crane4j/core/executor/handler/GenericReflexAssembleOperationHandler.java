package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.text.CharSequenceUtil;
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
public class GenericReflexAssembleOperationHandler extends AbstractAssembleOperationHandler<Object, KeyEntity> {

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
    protected Collection<KeyEntity> collectToEntities(Collection<AssembleExecution> executions) {
        List<KeyEntity> targets = new ArrayList<>();
        for (AssembleExecution execution : executions) {
            Class<?> targetType = execution.getTargetType();
            String key = execution.getOperation().getKey();
            MethodInvoker getter = propertyOperator.findGetter(targetType, key);
            Objects.requireNonNull(getter, () -> CharSequenceUtil.format(
                "cannot find getter [{}] for [{}]", key, targetType
            ));
            execution.getTargets().stream()
                .map(t -> createTarget(execution, t, getter.invoke(t)))
                .forEach(targets::add);
        }
        return targets;
    }

    /**
     * Create a {@link KeyEntity} instance.
     *
     * @param execution execution
     * @param origin    origin
     * @param keyValue  key value
     * @return {@link KeyEntity}
     */
    protected KeyEntity createTarget(AssembleExecution execution, Object origin, Object keyValue) {
        return new KeyEntity(execution, origin, keyValue);
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
    protected Map<Object, Object> getSourcesFromContainer(Container<?> container, Collection<KeyEntity> targets) {
        Set<Object> keys = targets.stream()
            .map(KeyEntity::getKey)
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
    protected Object getTheAssociatedSource(KeyEntity target, Map<Object, Object> sources) {
        return sources.get(target.getKey());
    }

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    @Override
    protected void completeMapping(Object source, KeyEntity target) {
        AssembleExecution execution = target.getExecution();
        Class<?> targetType = execution.getTargetType();
        Set<PropertyMapping> mappings = execution.getOperation().getPropertyMappings();
        for (PropertyMapping mapping : mappings) {
            mappingProperty(target, source, targetType, mapping);
        }
    }

    private void mappingProperty(KeyEntity entity, Object source, Class<?> targetType, PropertyMapping mapping) {
        Object sourceValue = mapping.hasSource() ?
            propertyOperator.readProperty(source.getClass(), source, mapping.getSource()) : source;
        if (Objects.nonNull(sourceValue)) {
            propertyOperator.writeProperty(targetType, entity.getOrigin(), mapping.getReference(), sourceValue);
        }
    }
}
