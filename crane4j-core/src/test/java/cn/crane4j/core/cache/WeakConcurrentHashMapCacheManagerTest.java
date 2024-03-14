package cn.crane4j.core.cache;

import org.junit.Assert;

import java.util.concurrent.TimeUnit;

/**
 * test for {@link MapCacheManager}
 *
 * @author huangchengxing
 */
public class WeakConcurrentHashMapCacheManagerTest extends BaseCacheManagerTest {

    @Override
    protected void initManager() {
        cacheManager = MapCacheManager.newWeakConcurrentMapCacheManager();
        cache = cacheManager.createCache("test", -1L, TimeUnit.MILLISECONDS);
        Assert.assertEquals(CacheManager.DEFAULT_MAP_CACHE_MANAGER_NAME, cacheManager.getName());
    }
}
