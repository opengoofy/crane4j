package cn.crane4j.core.container;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.hutool.core.map.WeakConcurrentMap;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        cacheManager = new ConcurrentMapCacheManager(WeakConcurrentMap::new);
        cacheableContainer = new CacheableContainer<>(container, cacheManager, "test");
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
    public void getCacheName() {
        Assert.assertSame("test", cacheableContainer.getCacheName());
    }

    @Test
    public void getNamespace() {
        Assert.assertEquals(container.getNamespace(), cacheableContainer.getNamespace());
    }

    @Test
    public void get() {
        Map<String, ?> data = cacheableContainer.get(Collections.singleton("a"));
        Object cacheA = data.get("a");
        Map<String, ?> cachedData = cacheableContainer.get(Collections.singleton("a"));
        Assert.assertSame(cacheA, cachedData.get("a"));
    }

    @Getter
    private static class TestContainer implements Container<String>, Container.Lifecycle {
        private final String namespace = "test";
        @Override
        public Map<String, ?> get(Collection<String> keys) {
            return keys.stream().collect(HashMap::new, (map, key) -> map.put(key, key), HashMap::putAll);
        }
    }
}
