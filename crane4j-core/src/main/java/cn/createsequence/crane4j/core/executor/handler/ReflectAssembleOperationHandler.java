package cn.createsequence.crane4j.core.executor.handler;

import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.container.EmptyContainer;
import cn.createsequence.crane4j.core.executor.AssembleExecution;
import cn.createsequence.crane4j.core.parser.PropertyMapping;
import cn.createsequence.crane4j.core.support.reflect.PropertyOperator;
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
 * 基于{@link PropertyOperator}实现的装配操作处理器
 *
 * @author huangchengxing
 */
// TODO 重写Mapping方法，使其支持各种表达式
@RequiredArgsConstructor
public class ReflectAssembleOperationHandler implements AssembleOperationHandler {

    /**
     * 反射工具类
     */
    private final PropertyOperator propertyOperator;

    /**
     * 执行操作
     *
     * @param container  数据源容器
     * @param executions 待执行的装配操作
     */
    @SuppressWarnings("unchecked")
    @Override
    public void process(Container<?> container, Collection<AssembleExecution> executions) {
        // 提取目标对象的key值，并按target分组
        Multimap<Object, Entity> entities = mapToEntity(executions);

        // 如果没有指定数据源容器，则实际上数据源对象就是它自己
        if (container instanceof EmptyContainer) {
            entities.forEach((k, e) -> mapProperties(e, e.getTarget()));
            return;
        }

        // 获取数据源
        Map<Object, ?> sources = ((Container<Object>)container).get(entities.keys());
        if (CollUtil.isEmpty(sources)) {
            return;
        }
        // 进行数据源对象与目标对象间的属性映射
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
            for (Object target : execution.getTargets()) {
                Object keyValue = propertyOperator.readProperty(targetType, target, key);
                entities.put(keyValue, new Entity(execution, target));
            }
        }
        return entities;
    }

    /**
     * 将数据源中的指定字段映射到目标对象的指定字段中
     *
     * @param entity 目标对象
     * @param source 数据源对象
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
        // TODO 优化读写效率，避免每次读写都需要根据类型进行一次查找
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
