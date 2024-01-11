package cn.crane4j.core.cache;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * test for {@link CacheableContainerProcessor}
 *
 * @author huangchengxing
 */
public class CacheableContainerProcessorTest {

    private CacheableContainerProcessor processor;

    @Before
    public void init() {
        SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        processor = new CacheableContainerProcessor(configuration, SimpleAnnotationFinder.INSTANCE);
    }

    @Test
    public void testCacheDefinitionRetriever() {
        processor.setCacheDefinitionRetriever((d, c) -> d.getNamespace().equals("test") ?
            new CacheDefinition.Impl("test", null, -1L, TimeUnit.MILLISECONDS) : null
        );
        Container<Object> container = processor.whenCreated(ContainerDefinition.create("test", "test", Container::empty), Container.empty());
        Assert.assertTrue(container instanceof CacheableContainer);
        Assert.assertEquals("test", ((CacheableContainer<?>) container).getCacheDefinition().getName());
        // if namespace is not test, then container is not cacheable
        container = processor.whenCreated(ContainerDefinition.create("test2", "test2", Container::empty), Container.empty());
        Assert.assertFalse(container instanceof CacheableContainer);
    }

    @Test
    public void testAnnotation() {
        TestContainer testContainer = new TestContainer();
        Container<Object> container = processor.whenCreated(
            ContainerDefinition.create("test", "test", () -> testContainer), testContainer
        );
        Assert.assertTrue(container instanceof CacheableContainer);
        Assert.assertEquals("test", ((CacheableContainer<?>) container).getCacheDefinition().getName());

        CacheDefinition definition = ((CacheableContainer<?>) container).getCacheDefinition();
        Assert.assertEquals("test", definition.getName());
        Assert.assertEquals(1000L, definition.getExpireTime().longValue());
        Assert.assertEquals(TimeUnit.SECONDS, definition.getTimeUnit());
    }

    @ContainerCache(
        expirationTime = 1000L,
        timeUnit = TimeUnit.SECONDS
    )
    private static class TestContainer implements Container<Object> {
        @Getter
        private final String namespace = "test";
        @Override
        public Map<Object, ?> get(Collection<Object> keys) {
            return null;
        }
    }
}
