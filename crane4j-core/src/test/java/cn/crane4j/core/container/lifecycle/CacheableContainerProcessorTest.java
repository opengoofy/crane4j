package cn.crane4j.core.container.lifecycle;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

/**
 * test for {@link CacheableContainerProcessor}
 *
 * @author huangchengxing
 */
public class CacheableContainerProcessorTest {

    private CacheableContainerProcessor cacheableContainerProcessor;
    private CacheManager cacheManager;

    @Before
    public void init() {
        cacheManager = new ConcurrentMapCacheManager(ConcurrentHashMap::new);
        cacheableContainerProcessor = new CacheableContainerProcessor(cacheManager);
    }

    @Test
    public void whenCreated() {
        cacheableContainerProcessor.setCacheNameSelector((def, c) -> def.getNamespace().equals("test") ? "test" : null);
        Container<Object> container = cacheableContainerProcessor.whenCreated(ContainerDefinition.create("test", "test", Container::empty), Container.empty());
        Assert.assertTrue(container instanceof CacheableContainer);
        Assert.assertEquals("test", ((CacheableContainer<?>) container).getCacheName());
        // if namespace is not test, then container is not cacheable
        container = cacheableContainerProcessor.whenCreated(ContainerDefinition.create("test2", "test2", Container::empty), Container.empty());
        Assert.assertFalse(container instanceof CacheableContainer);
    }
}
