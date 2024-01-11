package cn.crane4j.core.cache;

import cn.crane4j.core.container.Container;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * test for {@link CacheableContainer}
 *
 * @author huangchengxing
 */
public class CacheableContainerTest {

    private Container<String> container;
    private CacheManager cacheManager;
    private CacheableContainer<String> cacheableContainer;

    @Before
    public void init() {
        container = new TestContainer();
        cacheManager = MapCacheManager.newConcurrentHashMapCacheManager();
        CacheDefinition cacheDefinition = new CacheDefinition.Impl(
            container.getNamespace(), cacheManager.getClass().getName(), -1L, TimeUnit.MILLISECONDS
        );
        cacheableContainer = new CacheableContainer<>(container, cacheDefinition, cacheManager);
        cacheableContainer.init();
        cacheableContainer.destroy();
    }

    @Test
    public void getContainer() {
        Assert.assertSame(container, cacheableContainer.getContainer());
    }

    @Test
    public void getCache() {
        Assert.assertSame(cacheManager, cacheableContainer.getCacheManager());
    }

    @Test
    public void getNamespace() {
        Assert.assertEquals(container.getNamespace(), cacheableContainer.getNamespace());
    }

    @Test
    public void getCacheDefinition() {
        CacheDefinition definition = cacheableContainer.getCacheDefinition();
        Assert.assertEquals(container.getNamespace(), definition.getName());
        Assert.assertEquals(cacheManager.getClass().getName(), definition.getCacheManager());
        Assert.assertEquals(-1L, definition.getExpireTime().longValue());
        Assert.assertEquals(TimeUnit.MILLISECONDS, definition.getTimeUnit());
    }

    @Test
    public void get() {
        Map<String, ?> data = cacheableContainer.get(Collections.singleton("a"));
        Object cacheA = data.get("a");
        Map<String, ?> cachedData = cacheableContainer.get(Collections.singleton("a"));
        Assert.assertSame(cacheA, cachedData.get("a"));
        Map<String, ?> cachedDataFromCacheObject = cacheableContainer.getCurrentCache()
            .getAll(Collections.singleton("a"));
        Assert.assertSame(cacheA, cachedDataFromCacheObject.get("a"));

        cacheManager.removeCache(container.getNamespace());
        Map<String, ?> newData = cacheableContainer.get(Collections.singleton("a"));
        Assert.assertNotSame(cacheA, newData.get("a"));
    }

    @Getter
    private static class TestContainer implements Container<String>, Container.Lifecycle {
        private final String namespace = "test";
        @Override
        public Map<String, ?> get(Collection<String> keys) {
            return keys.stream()
                .collect(HashMap::new, (map, key) -> map.put(key, new Object()), HashMap::putAll);
        }
    }
}
