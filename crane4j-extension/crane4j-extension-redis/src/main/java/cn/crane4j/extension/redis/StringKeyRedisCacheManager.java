package cn.crane4j.extension.redis;

import cn.crane4j.core.cache.CacheObject;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

/**
 * The cache manager based on spring {@link RedisTemplate},
 * default cache key is {@code globalPrefix + ":" + cacheName + ":" + key}.
 *
 * @author huangchengxing
 */
@Slf4j
public class StringKeyRedisCacheManager extends AbstractRedisCacheManager<String, Object> {

    /**
     * Global prefix for all cache keys.
     */
    @NonNull
    @Setter
    private String globalPrefix = "crane4j:cache:";

    /**
     * <p>Whether enable actually clear cache from redis
     * when {@link #clearAll()}、{@link #removeCache(String)} or {@link CacheObject#clear()} is called.
     * 
     * <p>It's a dangerous operation, please use it carefully.
     */
    @Setter
    private boolean enableClearCache = false;

    public StringKeyRedisCacheManager(@NonNull RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected void clearCache(String cacheName) {
        if (enableClearCache) {
            String command = globalPrefix + ":" + cacheName + ":*";
            Set<String> keys = redisTemplate.keys(command);
            if (CollectionUtils.isNotEmpty(keys)) {
                log.warn("Clear [{}] keys from cache [{}] by command [{}]", keys.size(), cacheName, command);
                redisTemplate.delete(keys);
            }
            return;
        }
        super.clearCache(cacheName);
    }

    /**
     * Get the cache key which is used to store cache value in redis.
     *
     * @param cacheName cache name
     * @param key       cache key
     * @return cache key
     */
    @Override
    protected String resolveCacheKey(String cacheName, String key) {
        return globalPrefix + ":" + cacheName + ":" + key;
    }

    /**
     * Resolve cache value.
     *
     * @param value cache value
     * @return cache value
     */
    @Override
    protected Object resolveCacheValue(Object value) {
        return value;
    }
}
