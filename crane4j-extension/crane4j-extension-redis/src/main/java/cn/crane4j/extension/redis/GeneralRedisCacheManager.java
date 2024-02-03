package cn.crane4j.extension.redis;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * General redis cache manager.
 *
 * @author huangchengxing
 */
public class GeneralRedisCacheManager<K, V> extends AbstractRedisCacheManager<K, V> {

    private final BiFunction<String, K, K> keyResolver;
    private final Function<Object, V> valueResolver;

    /**
     * Create a new {@link GeneralRedisCacheManager} instance.
     *
     * @param redisTemplate redis template
     * @param keyResolver  key resolver
     * @param valueResolver value resolver
     */
    public GeneralRedisCacheManager(
        @NonNull RedisTemplate<K, V> redisTemplate,
        @NonNull BiFunction<String, K, K> keyResolver, @NonNull Function<Object, V> valueResolver) {
        super(redisTemplate);
        this.keyResolver = keyResolver;
        this.valueResolver = valueResolver;
    }

    /**
     * Get the cache key which is used to store cache value in redis.
     *
     * @param cacheName cache name
     * @param key       cache key
     * @return cache key
     */
    @Override
    protected K resolveCacheKey(String cacheName, K key) {
        return keyResolver.apply(cacheName, key);
    }

    /**
     * Resolve cache value.
     *
     * @param value cache value
     * @return cache value
     */
    @Override
    protected V resolveCacheValue(Object value) {
        return valueResolver.apply(value);
    }
}
