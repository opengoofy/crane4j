package cn.crane4j.core.cache;

import org.junit.Assert;

import java.util.concurrent.TimeUnit;

/**
 * test for {@link MapCacheManager}
 *
 * @author huangchengxing
 */
public class ConcurrentHashMapCacheManagerTest extends BaseCacheManagerTest {

    @Override
    protected void initManager() {
        cacheManager = MapCacheManager.newConcurrentHashMapCacheManager();
        cache = cacheManager.createCache("test", -1L, TimeUnit.MILLISECONDS);
        Assert.assertEquals(cacheManager.getClass().getSimpleName(), cacheManager.getName());
    }
}
