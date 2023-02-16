package cn.createsequence.crane4j.core.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

/**
 * test for {@link SimpleCacheManager}
 *
 * @author huangchengxing
 */
public class SimpleCacheManagerTest {

    private SimpleCacheManager manager;

    @Before
    public void init() {
        manager = new SimpleCacheManager(
            new ConcurrentHashMap<>(8),
            cacheName -> new ConcurrentMapCache<>(new ConcurrentHashMap<>(16))
        );
    }

    @Test
    public void test() {
        Cache<String> cache = manager.getCache("test");
        Assert.assertNotNull(cache);
        Assert.assertSame(cache, manager.getCache("test"));
    }
}
