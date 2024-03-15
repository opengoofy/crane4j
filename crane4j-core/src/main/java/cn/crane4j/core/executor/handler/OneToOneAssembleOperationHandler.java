package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.executor.key.KeyResolver;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropDesc;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of {@link AssembleOperationHandler}
 * for the one-to-one mapping between the target object and the data source object.
 *
 * @author huangchengxing
 * @see PropertyOperator
 */
@RequiredArgsConstructor
public class OneToOneAssembleOperationHandler
    extends AbstractAssembleOperationHandler<AbstractAssembleOperationHandler.Target> {

    /**
     * propertyOperator
     */
    protected final PropertyOperator propertyOperator;

    /**
     * converter manager.
     */
    private final ConverterManager converterManager;

    /**
     * whether ignore null key.
     */
    @Setter
    private boolean ignoreNullKey = false;

    /**
     * Split the {@link AssembleExecution} into pending objects and wrap it as {@link Target}.
     *
     * @param executions executions
     * @return {@link Target}
     */
    @Override
    protected Collection<Target> collectToEntities(Collection<AssembleExecution> executions) {
        List<Target> targets = new ArrayList<>();
        for (AssembleExecution execution : executions) {
            AssembleOperation operation = execution.getOperation();
            KeyResolver keyResolver = getKeyResolver(execution);
            execution.getTargets().stream()
                .map(t -> createTarget(execution, t, keyResolver.resolve(t, operation)))
                .filter(t -> !ignoreNullKey || Objects.nonNull(t.getKey()))
                .forEach(targets::add);
        }
        return targets;
    }

    @NonNull
    private KeyResolver getKeyResolver(AssembleExecution execution) {
        KeyResolver keyResolver = execution.getOperation().getKeyResolver();
        if (Objects.nonNull(keyResolver)) {
            return keyResolver;
        }
        // TODO remove this branch in the future, the KeyResolver will be required.
        return getDefaultKeyPropertyResolver(execution);
    }

    private KeyResolver getDefaultKeyPropertyResolver(AssembleExecution execution) {
        String key = execution.getOperation().getKey();
        // if no key is specified, key value is the targets themselves.
        KeyResolver keyResolver = StringUtils.isEmpty(key) ?
            (t, op) -> t : (t, op) -> propertyOperator.readProperty(t.getClass(), t, key);
        // fix https://github.com/opengoofy/crane4j/issues/153
        Class<?> keyType = execution.getOperation().getKeyType();
        return Objects.isNull(keyType) ? keyResolver : (op, t) -> {
            Object k = keyResolver.resolve(op, t);
            return converterManager.convert(k, keyType);
        };
    }

    /**
     * Create a {@link Target} instance.
     *
     * @param execution execution
     * @param origin    origin
     * @param keyValue  key value
     * @return {@link Target}
     */
    protected Target createTarget(AssembleExecution execution, Object origin, Object keyValue) {
        return new Target(execution, origin, keyValue);
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
    protected Map<Object, Object> getSourcesFromContainer(Container<?> container, Collection<Target> targets) {
        Set<Object> keys = targets.stream()
            .map(Target::getKey)
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
    protected Object getTheAssociatedSource(Target target, Map<Object, Object> sources) {
        return sources.get(target.getKey());
    }

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    @Override
    protected void completeMapping(Object source, Target target) {
        AssembleExecution execution = target.getExecution();
        PropDesc sourceDesc = propertyOperator.getPropertyDescriptor(source.getClass());
        PropDesc targetDesc = propertyOperator.getPropertyDescriptor(target.getOrigin().getClass());

        // mapping properties
        PropertyMappingStrategy propertyMappingStrategy = execution.getOperation().getPropertyMappingStrategy();
        Set<PropertyMapping> mappings = execution.getOperation().getPropertyMappings();
        for (PropertyMapping mapping : mappings) {
            Object sourceValue = mapping.hasSource() ?
                sourceDesc.readProperty(source, mapping.getSource()) : source;
            Object originTarget = target.getOrigin();
            propertyMappingStrategy.doMapping(
                originTarget, source, sourceValue, mapping,
                sv -> targetDesc.writeProperty(originTarget, mapping.getReference(), sourceValue)
            );
        }
    }
}
