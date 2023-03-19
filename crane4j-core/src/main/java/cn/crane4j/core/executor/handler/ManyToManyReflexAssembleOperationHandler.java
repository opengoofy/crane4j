package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.support.reflect.PropertyOperator;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>An implementation of {@link AssembleOperationHandler}
 * for the one-to-one mapping between the target object and the data source object.
 *
 * <p>The difference between {@link ManyToManyReflexAssembleOperationHandler} and {@link OneToManyReflexAssembleOperationHandler}
 * is that {@link OneToManyReflexAssembleOperationHandler} is used to handle the situation where
 * multiple values can be obtained through a key in the data source container,
 * while {@link ManyToManyReflexAssembleOperationHandler} is used to handle the situation where only
 * one value can be obtained through a key, but there are multiple keys at the same time.
 *
 * @author huangchengxing
 * @see DefaultSplitter
 */
public class ManyToManyReflexAssembleOperationHandler extends OneToManyReflexAssembleOperationHandler {

    /**
     * splitter used to split the value of key attribute into multiple key values.
     *
     * @see ManyToManyReflexAssembleOperationHandler.DefaultSplitter
     */
    private final Function<Object, Collection<Object>> keySplitter;

    /**
     * Create an {@link ManyToManyReflexAssembleOperationHandler} instance.
     *
     * @param propertyOperator propertyOperator
     */
    public ManyToManyReflexAssembleOperationHandler(PropertyOperator propertyOperator, Function<Object, Collection<Object>> keySplitter) {
        super(propertyOperator);
        this.keySplitter = keySplitter;
    }

    /**
     * Create a {@link ManyToManyReflexAssembleOperationHandler} instance
     * and use the default {@link DefaultSplitter} split key value
     *
     * @param propertyOperator property operator
     */
    public ManyToManyReflexAssembleOperationHandler(PropertyOperator propertyOperator) {
        this(propertyOperator, new DefaultSplitter(","));
    }

    /**
     * Create a {@link KeyEntity} instance.
     *
     * @param execution execution
     * @param origin    origin
     * @param keyValue  key value
     * @return {@link KeyEntity}
     */
    @Override
    protected KeyEntity createTarget(AssembleExecution execution, Object origin, Object keyValue) {
        return new KeyEntity(execution, origin, keySplitter.apply(keyValue));
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
            .map(k -> (Collection<?>)k)
            .flatMap(Collection::stream)
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
        return ((Collection<?>)target.getKey()).stream()
            .map(sources::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
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
