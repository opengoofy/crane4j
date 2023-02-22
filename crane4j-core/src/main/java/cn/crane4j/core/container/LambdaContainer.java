package cn.crane4j.core.container;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>A data source container implemented by lambda expression
 * that returns data source objects grouped by key
 * based on the set of input key values.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LambdaContainer<K> implements Container<K> {

    @Getter
    private final String namespace;
    private final Function<Collection<K>, Map<K, ?>> dataSource;

    /**
     * Build a data source container based on an input key set
     * and an expression that returns data sources grouped by key.
     *
     * @param namespace namespace
     * @param lambda lambda expression
     * @param <K> key type
     * @return container
     */
    public static <K> LambdaContainer<K> forLambda(String namespace, Function<Collection<K>, Map<K, ?>> lambda) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(lambda);
        return new LambdaContainer<>(namespace, lambda);
    }

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @Override
    public Map<K, ?> get(Collection<K> keys) {
        return dataSource.apply(keys);
    }
}
