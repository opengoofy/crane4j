package cn.createsequence.crane4j.core.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

/**
 * test for {@link ConcurrentMapCache}
 *
 * @author huangchengxing
 */
public class ConcurrentMapCacheTest {

    private ConcurrentMapCache<String> cache;

    @Before
    public void init() {
        cache = new ConcurrentMapCache<>(new ConcurrentHashMap<>());
    }

    @Test
    public void test() {
        Object value = new Object();
        Assert.assertNull(cache.put("test", value));
        Assert.assertSame(value, cache.get("test"));

        cache.putIfAbsent("test", new Object());
        Assert.assertSame(value, cache.get("test"));
        Object value2 = new Object();
        cache.putIfAbsent("test2", value2);
        Assert.assertSame(value2, cache.get("test2"));
    }

}
