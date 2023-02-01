package cn.createsequence.crane4j.core.container;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 基于输入key值集合返回按key分组的数据源对象的lambda表达式实现的数据源容器
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LambdaContainer<K> implements Container<K> {

    @Getter
    private final String namespace;
    private final Function<Collection<K>, Map<K, ?>> dataSource;

    /**
     * 基于一个输入key集合，返回按key分组的数据源的表达式构建数据源容器
     *
     * @param namespace 命名空间
     * @param lambda 表达式
     * @param <K> key类型
     * @return 数据源容器
     */
    public static <K> Container<K> forLambda(String namespace, Function<Collection<K>, Map<K, ?>> lambda) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(lambda);
        return new LambdaContainer<>(namespace, lambda);
    }

    /**
     * 输入一批key值，返回按key值分组的数据源对象
     *
     * @param keys keys
     * @return 按key值分组的数据源对象
     */
    @Override
    public Map<K, ?> get(Collection<K> keys) {
        return dataSource.apply(keys);
    }
}
