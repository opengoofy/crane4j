package cn.crane4j.core.cache;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

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
    }

    @Test
    public void testExpire() throws InterruptedException {
        CacheObject<Object> cacheObject = cacheManager.createCache("test", 200L, TimeUnit.MILLISECONDS);
        Assert.assertFalse(cacheObject.isInvalid());
        cache.put("test", "test");
        Thread.sleep(400L);
        Assert.assertNull(cacheObject.get("test"));
    }
}
