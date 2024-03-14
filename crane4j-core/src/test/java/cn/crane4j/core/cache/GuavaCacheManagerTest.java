package cn.crane4j.core.cache;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * test for {@link GuavaCacheManager}
 *
 * @author huangchengxing
 */
public class GuavaCacheManagerTest extends BaseCacheManagerTest {

    @Override
    protected void initManager() {
        cacheManager = new GuavaCacheManager();
        ((GuavaCacheManager)cacheManager)
            .setCacheFactory(GuavaCacheManager.DefaultCacheFactory.INSTANCE);
        cache = cacheManager.createCache("test", -1L, null);
        Assert.assertEquals(CacheManager.DEFAULT_GUAVA_CACHE_MANAGER_NAME, cacheManager.getName());
    }

    @Test
    public void testExpire() {
        CacheObject<Object> cacheObject = cacheManager.createCache("test", 200L, TimeUnit.MILLISECONDS);
        Assert.assertFalse(cacheObject.isInvalid());
        cache.put("test", "test");
        LockSupport.parkNanos(Thread.currentThread(), 400L * 1000 * 1000);
        Assert.assertNull(cacheObject.get("test"));
    }
}
