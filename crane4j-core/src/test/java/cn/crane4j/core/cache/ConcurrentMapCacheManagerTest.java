package cn.crane4j.core.cache;

import cn.crane4j.core.util.CollectionUtils;

/**
 * test for {@link ConcurrentMapCacheManager}
 *
 * @author huangchengxing
 */
public class ConcurrentMapCacheManagerTest extends BaseCacheManagerTest {

    @Override
    protected void initManager() {
        manager = new ConcurrentMapCacheManager(CollectionUtils::newWeakConcurrentMap);
        cache = manager.getCache("test");
    }
}
