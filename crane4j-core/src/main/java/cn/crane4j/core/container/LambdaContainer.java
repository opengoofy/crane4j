package cn.crane4j.core.container;

import cn.crane4j.core.support.DataProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A data source container implemented by lambda expression
 * that returns data source objects grouped by key
 * based on the set of input key values.
 *
 * @author huangchengxing
 * @param <K> key type
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LambdaContainer<K> implements Container<K> {

    @Getter
    private final String namespace;
    private final DataProvider<K, ?> dataSource;

    /**
     * Build a data source container based on an input key set
     * and an expression that returns data sources grouped by key.
     *
     * @param namespace namespace
     * @param lambda lambda expression
     * @param <K> key type
     * @return container
     */
    public static <K> LambdaContainer<K> forLambda(String namespace, DataProvider<K, ?> lambda) {
        Objects.requireNonNull(namespace, "container namespace must not null");
        Objects.requireNonNull(lambda, "lambda must not null");
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
