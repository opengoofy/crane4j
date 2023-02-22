package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.EmptyContainer;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A basic {@link AssembleOperationHandler} implementation based on {@link PropertyOperator}.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ReflectAssembleOperationHandler implements AssembleOperationHandler {

    /**
     * property operator
     */
    private final PropertyOperator propertyOperator;

    /**
     * Perform assembly operation.
     *
     * @param container container
     * @param executions operations to be performed
     */
    @SuppressWarnings("unchecked")
    @Override
    public void process(Container<?> container, Collection<AssembleExecution> executions) {
        // extract the key value of the target object and group it by target
        Multimap<Object, Entity> entities = mapToEntity(executions);
        // if no data source container is specified, the data source object is actually itself
        if (container instanceof EmptyContainer) {
            entities.forEach((k, e) -> mapProperties(e, e.getTarget()));
            return;
        }
        // get data source
        Map<Object, ?> sources = ((Container<Object>)container).get(entities.keys());
        if (CollUtil.isEmpty(sources)) {
            return;
        }
        // perform attribute mapping between data source object and target object
        entities.forEach((k, e) -> {
            Object source = sources.get(k);
            if (Objects.nonNull(source)) {
                mapProperties(e, source);
            }
        });
    }

    private Multimap<Object, Entity> mapToEntity(Collection<AssembleExecution> executions) {
        Multimap<Object, Entity> entities = ArrayListMultimap.create();
        for (AssembleExecution execution : executions) {
            Class<?> targetType = execution.getTargetType();
            String key = execution.getOperation().getKey();
            MethodInvoker getter = propertyOperator.findGetter(targetType, key);
            for (Object target : execution.getTargets()) {
                Object keyValue = getter.invoke(target);
                entities.put(keyValue, new Entity(execution, target));
            }
        }
        return entities;
    }

    /**
     * Map the specified fields in the data source to the specified fields in the target object.
     *
     * @param entity entity
     * @param source source
     */
    protected void mapProperties(Entity entity, Object source) {
        AssembleExecution execution = entity.getExecution();
        Class<?> targetType = execution.getTargetType();
        Set<PropertyMapping> mappings = execution.getOperation().getPropertyMappings();
        for (PropertyMapping mapping : mappings) {
            mapProperty(entity, source, targetType, mapping);
        }
    }

    private void mapProperty(Entity entity, Object source, Class<?> targetType, PropertyMapping mapping) {
        // TODO optimize the efficiency of reading and writing
        Object sourceValue = mapping.hasSource() ?
            propertyOperator.readProperty(source.getClass(), source, mapping.getSource()) : source;
        if (Objects.nonNull(sourceValue)) {
            propertyOperator.writeProperty(targetType, entity.getTarget(), mapping.getReference(), sourceValue);
        }
    }

    @Getter
    @RequiredArgsConstructor
    protected static class Entity {
        private final AssembleExecution execution;
        private final Object target;
    }
}
