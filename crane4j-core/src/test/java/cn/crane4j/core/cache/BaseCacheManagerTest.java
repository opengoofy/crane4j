package cn.crane4j.core.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangchengxing
 */
public abstract class BaseCacheManagerTest {

    protected CacheManager manager;
    protected Cache<String> cache;

    @Before
    public final void init() {
        initManager();
    }

    protected abstract void initManager();

    @Test
    public void testManager() {
        Cache<String> cache = manager.getCache("test");
        Assert.assertNotNull(cache);
        Assert.assertFalse(cache.isExpired());
        Assert.assertSame(cache, manager.getCache("test"));
        manager.removeCache("none cache");
        manager.removeCache("test");
        Assert.assertTrue(cache.isExpired());
        Assert.assertNotSame(cache, manager.getCache("test"));
    }

    @Test
    public void testCache() {
        Object value = new Object();
        cache.put("test", value);
        Assert.assertSame(value, cache.get("test"));

        cache.putIfAbsent("test", new Object());
        Assert.assertSame(value, cache.get("test"));
        Object value2 = new Object();
        cache.putIfAbsent("test2", value2);
        Assert.assertSame(value2, cache.get("test2"));

        Assert.assertTrue(cache.getAll(Collections.emptyList()).isEmpty());
        Map<String, Object> cacheValues = cache.getAll(Arrays.asList("test", "test2"));
        Assert.assertEquals(value, cacheValues.get("test"));
        Assert.assertEquals(value2, cacheValues.get("test2"));

        Map<String, Object> map = new HashMap<>();
        map.put("test3", value);
        map.put("test4", value2);
        cache.putAll(map);
        Assert.assertEquals(value, cache.get("test3"));
        Assert.assertEquals(value2, cache.get("test4"));
    }
}
