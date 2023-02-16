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
import java.util.stream.Collectors;

/**
 * 支持多key值字段的装配处理器
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class MultiKeyAssembleOperationHandler implements AssembleOperationHandler {

    private final String separator;
    private final PropertyOperator propertyOperator;

    /**
     * 执行装配操作
     *
     * @param container  数据源容器
     * @param executions 待执行的装配操作
     */
    @Override
    public void process(Container<?> container, Collection<AssembleExecution> executions) {
        List<Entity> entities = parseExecutionsToEntities(executions);
        Map<Object, ?> sources = resolveKeys(container, entities);
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
                Collection<Object> multiKeys = splitMultiKeys(keyValue);
                if (!multiKeys.isEmpty()) {
                    entities.add(new Entity(execution, target, multiKeys));
                }
            }
        }
        return entities;
    }

    /**
     * key字段值
     *
     * @param keys 字段值
     * @return key字段集合
     */
    @SuppressWarnings("unchecked")
    protected Collection<Object> splitMultiKeys(Object keys) {
        if (Objects.isNull(keys)) {
            return Collections.emptyList();
        }
        if (keys instanceof String) {
            String str = (String)keys;
            return Arrays.stream(str.split(separator))
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

    @Getter
    @RequiredArgsConstructor
    protected static class Entity {
        private final AssembleExecution execution;
        private final Object target;
        private final Collection<Object> keys;
    }
}
