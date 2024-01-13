package cn.crane4j.extension.redis;

import cn.crane4j.core.cache.CacheObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * test for {@link GeneralRedisCacheManager} with real redis
 *
 * @author huangchengxing
 */
@Ignore  // TODO remove this annotation after resolved the container job problem of github action
public class GeneralRedisCacheManagerTest {

    private static final String PREFIX = "prefix";
    private static final String CACHE_NAME = "test";
    private static final long EXPIRE_TIME = 3000L;
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

    private RedisTemplate<String, Object> redisTemplate;
    private GeneralRedisCacheManager<String, Object> cacheManager;
    private CacheObject<String> cache;

    @Before
    public void init() {
        redisTemplate = new RedisTemplate<>();

        // default connect to localhost:6379
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setDatabase(1);
        configuration.setPort(6379);
        configuration.setHostName("localhost");
        RedisConnectionFactory factory = new JedisConnectionFactory(configuration);
        redisTemplate.setConnectionFactory(factory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        cacheManager = new GeneralRedisCacheManager<>(redisTemplate, (cacheName, key) -> resolveCacheKey(key), Function.identity());
        cache = cacheManager.createCache("test", EXPIRE_TIME, TIME_UNIT);
    }

    @Test
    public void testPut() {
        cache.put("key", "value");

        Assert.assertEquals("value", cache.get("key"));
        Assert.assertEquals("value", redisTemplate.opsForValue().get(resolveCacheKey("key")));
        redisTemplate.delete(resolveCacheKey("key"));
    }

    @Test
    public void testPutAll() {
        Map<String, Object> values = new LinkedHashMap<>(2);
        values.put("key1", "value1");
        values.put("key2", "value2");
        cache.putAll(values);

        Assert.assertEquals("value1", redisTemplate.opsForValue().get(resolveCacheKey("key1")));
        redisTemplate.delete(resolveCacheKey("key1"));
        Assert.assertEquals("value2", redisTemplate.opsForValue().get(resolveCacheKey("key2")));
        redisTemplate.delete(resolveCacheKey("key2"));
    }

    @Test
    public void testGet() {
        redisTemplate.opsForValue().set(resolveCacheKey("key"), "value");
        Assert.assertEquals("value", cache.get("key"));
        redisTemplate.delete(resolveCacheKey("key"));
    }

    @Test
    public void testGetAll() {
        redisTemplate.opsForValue().set(resolveCacheKey("key1"), "value1");
        redisTemplate.opsForValue().set(resolveCacheKey("key2"), "value2");
        Map<String, Object> map = cache.getAll(Arrays.asList("key1", "none", "key2"));
        Assert.assertEquals("value1", map.get("key1"));
        Assert.assertEquals("value2", map.get("key2"));
        Assert.assertNull(map.get("none"));
        redisTemplate.delete(resolveCacheKey("key1"));
        redisTemplate.delete(resolveCacheKey("key2"));
    }

    @Test
    public void putIfAbsent() {
        redisTemplate.opsForValue().set(resolveCacheKey("key1"), "value1");
        cache.putIfAbsent("key1", "value1");
        Assert.assertEquals("value1", redisTemplate.opsForValue().get(resolveCacheKey("key1")));
        redisTemplate.delete(resolveCacheKey("key1"));

        cache.putIfAbsent("key2", "value2");
        Assert.assertEquals("value2", redisTemplate.opsForValue().get(resolveCacheKey("key2")));
        redisTemplate.delete(resolveCacheKey("key2"));
    }

    @Test
    public void remove() {
        redisTemplate.opsForValue().set(resolveCacheKey("key"), "value");
        cache.remove("key");
        Assert.assertNull(redisTemplate.opsForValue().get(resolveCacheKey("key")));
    }

    @Test
    public void removeAll() {
        redisTemplate.opsForValue().set(resolveCacheKey("key1"), "value1");
        redisTemplate.opsForValue().set(resolveCacheKey("key2"), "value2");
        cache.removeAll(Arrays.asList("key1", "key2"));
        Assert.assertNull(redisTemplate.opsForValue().get(resolveCacheKey("key1")));
        Assert.assertNull(redisTemplate.opsForValue().get(resolveCacheKey("key2")));
    }

    @Test
    public void clear() {
        redisTemplate.opsForValue().set(resolveCacheKey("key"), "value");
        cache.clear();
        Assert.assertEquals("value", redisTemplate.opsForValue().get(resolveCacheKey("key")));
    }

    private String resolveCacheKey(String key) {
        return PREFIX + ":" + CACHE_NAME + ":" + key;
    }
}
