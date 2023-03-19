package cn.crane4j.core.cache;

import com.google.common.cache.CacheBuilder;

/**
 * test for {@link GuavaCacheManager}
 *
 * @author huangchengxing
 */
public class GuavaCacheManagerTest extends BaseCacheManagerTest {

    @Override
    protected void initManager() {
        manager = new GuavaCacheManager(() -> CacheBuilder.newBuilder()
            .maximumSize(100)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .build());
        cache = manager.getCache("test");
    }
}
