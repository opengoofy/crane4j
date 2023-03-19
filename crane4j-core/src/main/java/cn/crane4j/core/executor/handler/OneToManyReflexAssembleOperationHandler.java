package cn.crane4j.core.executor.handler;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>An implementation of {@link AssembleOperationHandler}
 * for the one-to-many mapping between the target object and the data source object.
 * 
 * <p>The difference between {@link ManyToManyReflexAssembleOperationHandler} and {@link OneToManyReflexAssembleOperationHandler}
 * is that {@link OneToManyReflexAssembleOperationHandler} is used to handle the situation where
 * multiple values can be obtained through a key in the data source container,
 * while {@link ManyToManyReflexAssembleOperationHandler} is used to handle the situation where only
 * one value can be obtained through a key, but there are multiple keys at the same time.
 *
 * @author huangchengxing
 */
public class OneToManyReflexAssembleOperationHandler extends GenericReflexAssembleOperationHandler {

    /**
     * Create an {@link OneToManyReflexAssembleOperationHandler} instance.
     *
     * @param propertyOperator property operator
     */
    public OneToManyReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        super(propertyOperator);
    }

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    @Override
    protected void completeMapping(Object source, KeyEntity target) {
        Collection<?> sources = CollectionUtils.adaptObjectToCollection(source);
        Set<PropertyMapping> mappings = target.getExecution().getOperation().getPropertyMappings();
        for (PropertyMapping mapping : mappings) {
            // there are always multiple source values,
            // so we need to merge the source objects after operation
            Collection<?> sourceValues = mapping.hasSource() ?
                sources.stream().map(s -> propertyOperator.readProperty(s.getClass(), s, mapping.getSource()))
                    .collect(Collectors.toList()) : sources;
            propertyOperator.writeProperty(target.getExecution().getTargetType(), target.getOrigin(), mapping.getReference(), sourceValues);
        }
    }
}
