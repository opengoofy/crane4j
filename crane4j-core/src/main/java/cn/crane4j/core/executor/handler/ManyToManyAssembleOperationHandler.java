package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

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
 * <p>The difference between {@link ManyToManyAssembleOperationHandler} and {@link OneToManyAssembleOperationHandler}
 * is that {@link OneToManyAssembleOperationHandler} is used to handle the situation where
 * multiple values can be obtained through a key in the data source container,
 * while {@link ManyToManyAssembleOperationHandler} is used to handle the situation where only
 * one value can be obtained through a key, but there are multiple keys at the same time.
 *
 * @author huangchengxing
 * @see DefaultSplitter
 */
@Setter
public class ManyToManyAssembleOperationHandler extends OneToManyAssembleOperationHandler {

    /**
     * splitter used to split the value of key attribute into multiple key values.
     *
     * @see ManyToManyAssembleOperationHandler.DefaultSplitter
     */
    @NonNull
    private KeySplitter keySplitter;

    /**
     * Create an {@link ManyToManyAssembleOperationHandler} instance.
     *
     * @param propertyOperator propertyOperator
     * @param converterManager converter manager
     * @param keySplitter splitter used to split the value of key attribute into multiple key values.
     */
    public ManyToManyAssembleOperationHandler(
        PropertyOperator propertyOperator, ConverterManager converterManager,
        @NonNull KeySplitter keySplitter) {
        super(propertyOperator, converterManager);
        this.keySplitter = keySplitter;
    }

    /**
     * Create a {@link ManyToManyAssembleOperationHandler} instance
     * and use the default {@link DefaultSplitter} split key value
     *
     * @param propertyOperator property operator
     * @param converterManager converter manager
     */
    public ManyToManyAssembleOperationHandler(PropertyOperator propertyOperator, ConverterManager converterManager) {
        this(propertyOperator, converterManager, new DefaultSplitter(","));
    }

    /**
     * Create a {@link Target} instance.
     *
     * @param execution execution
     * @param origin    origin
     * @param keyValue  key value
     * @return {@link Target}
     */
    @Override
    protected Target createTarget(AssembleExecution execution, Object origin, Object keyValue) {
        // TODO remove this override method in the future, the KeyResolver already split the key value
        return new Target(execution, origin, keySplitter.apply(keyValue));
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
    protected Object getTheAssociatedSource(Target target, Map<Object, Object> sources) {
        return ((Collection<?>)target.getKey()).stream()
            .map(sources::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Split the value of key attribute into multiple key values.
     *
     * @since 2.5.0
     */
    public interface KeySplitter extends Function<Object, Collection<Object>> {
    }

    /**
     * The default key value splitter supports splitting {@link Collection},
     * arrays and strings with specified delimiters.
     */
    @RequiredArgsConstructor
    public static class DefaultSplitter implements KeySplitter {
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
