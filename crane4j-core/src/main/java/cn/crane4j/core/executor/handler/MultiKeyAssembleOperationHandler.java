package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AssembleOperationHandler} supporting multiple key value fields.
 *
 * @author huangchengxing
 * @see DefaultSplitter
 */
@RequiredArgsConstructor
public class MultiKeyAssembleOperationHandler implements AssembleOperationHandler {

    /**
     * splitter used to split the value of key attribute into multiple key values.
     *
     * @see DefaultSplitter
     */
    private final Function<Object, Collection<Object>> keySplitter;

    /**
     * property operator.
     */
    private final PropertyOperator propertyOperator;

    /**
     * Create a {@link MultiKeyAssembleOperationHandler} instance
     * and use the default {@link DefaultSplitter} split key value
     *
     * @param propertyOperator property operator
     */
    public MultiKeyAssembleOperationHandler(PropertyOperator propertyOperator) {
        this(new DefaultSplitter(","), propertyOperator);
    }

    /**
     * Perform assembly operation.
     *
     * @param container container
     * @param executions operations to be performed
     */
    @Override
    public void process(Container<?> container, Collection<AssembleExecution> executions) {
        // extract and split key for target object
        List<Entity> entities = parseExecutionsToEntities(executions);
        Map<Object, ?> sources = resolveKeys(container, entities);
        // perform attribute mapping between data source object and target object
        for (Entity entity : entities) {
            AssembleOperation operation = entity.getExecution().getOperation();
            List<Object> source = entity.getKeys().stream()
                .map(sources::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            process(entity.getExecution().getTargetType(), operation, source, entity.getTarget());
        }
    }

    private void process(
        Class<?> targetType, AssembleOperation operation, List<Object> source, Object target) {
        Set<PropertyMapping> mappings = operation.getPropertyMappings();
        for (PropertyMapping mapping : mappings) {
            // there are always multiple source values,
            // so we need to merge the source objects after operation
            List<Object> sourceValues = mapping.hasSource() ?
                source.stream().map(s -> propertyOperator.readProperty(s.getClass(), s, mapping.getSource()))
                    .collect(Collectors.toList()) : source;
            propertyOperator.writeProperty(targetType, target, mapping.getReference(), sourceValues);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, ?> resolveKeys(Container<?> container, List<Entity> entities) {
        Set<Object> keys = entities.stream()
            .map(Entity::getKeys)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
        return ((Container<Object>)container).get(keys);
    }

    private List<Entity> parseExecutionsToEntities(Collection<AssembleExecution> executions) {
        List<Entity> entities = new ArrayList<>();
        for (AssembleExecution execution : executions) {
            Class<?> targetType = execution.getTargetType();
            String key = execution.getOperation().getKey();
            MethodInvoker getter = propertyOperator.findGetter(targetType, key);
            for (Object target : execution.getTargets()) {
                Object keyValue = getter.invoke(target);
                // split keys
                Collection<Object> multiKeys = keySplitter.apply(keyValue);
                if (!multiKeys.isEmpty()) {
                    entities.add(new Entity(execution, target, multiKeys));
                }
            }
        }
        return entities;
    }

    @Getter
    @RequiredArgsConstructor
    protected static class Entity {
        private final AssembleExecution execution;
        private final Object target;
        private final Collection<Object> keys;
    }

    /**
     * The default key value splitter supports splitting {@link Collection},
     * arrays and strings with specified delimiters.
     */
    @RequiredArgsConstructor
    public static class DefaultSplitter implements Function<Object, Collection<Object>> {
        private final String strSeparator;
        @SuppressWarnings("unchecked")
        @Override
        public Collection<Object> apply(Object keys) {
            if (Objects.isNull(keys)) {
                return Collections.emptyList();
            }
            if (keys instanceof String) {
                String str = (String)keys;
                return Arrays.stream(str.split(strSeparator))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            }
            if (keys instanceof Collection) {
                return (Collection<Object>)keys;
            }
            if (keys.getClass().isArray()) {
                return Arrays.asList((Object[])keys);
            }
            return Collections.emptyList();
        }
    }
}
