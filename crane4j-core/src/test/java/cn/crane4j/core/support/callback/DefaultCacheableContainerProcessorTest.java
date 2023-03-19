package cn.crane4j.core.support.callback;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.ConcurrentMapCacheManager;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * test for {@link DefaultCacheableContainerProcessor}
 *
 * @author huangchengxing
 */
public class DefaultCacheableContainerProcessorTest {

    private DefaultCacheableContainerProcessor defaultCacheableContainerProcessor;

    @Before
    public void init() {
        CacheManager manager = new ConcurrentMapCacheManager(ConcurrentHashMap::new);
        Map<String, String> conf = new HashMap<>();
        conf.put("test", "cache");
        defaultCacheableContainerProcessor = new DefaultCacheableContainerProcessor(manager, conf);
    }

    @Test
    public void beforeContainerRegister() {
        Container<?> containerBeforeProcess = LambdaContainer.forLambda("test", ids -> Collections.emptyMap());
        Container<?> containerAfterProcess = defaultCacheableContainerProcessor.beforeContainerRegister(this, containerBeforeProcess);
        Assert.assertNotSame(containerBeforeProcess, containerAfterProcess);
        Assert.assertTrue(containerAfterProcess instanceof CacheableContainer);
    }
}
