package cn.createsequence.crane4j.core.container;

import cn.createsequence.crane4j.core.cache.ConcurrentMapCache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * test for {@link CacheableContainer}
 *
 * @author huangchengxing
 */
public class CacheableContainerTest {

    private Container<String> container;
    private CacheableContainer<String> cacheableContainer;

    @Before
    public void init() {
        container = LambdaContainer.forLambda("container", keys -> {
            Map<String, Object> map = new HashMap<>();
            keys.forEach(key -> map.put(key, new Object()));
            return map;
        });
        cacheableContainer = new CacheableContainer<>(
            container, new ConcurrentMapCache<>(new ConcurrentHashMap<>(2))
        );
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
}
