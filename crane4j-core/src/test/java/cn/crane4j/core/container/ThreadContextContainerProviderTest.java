package cn.crane4j.core.container;

import cn.crane4j.core.support.DataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * test for {@link ThreadContextContainerProvider}.
 *
 * @author huangchengxing
 */
public class ThreadContextContainerProviderTest {

    private ThreadContextContainerProvider threadContextContainerProvider;

    @Before
    public void init() {
        threadContextContainerProvider = new ThreadContextContainerProvider();
    }

    @Test
    public void test() {
        Container<?> container = threadContextContainerProvider.getContainer("test");
        Assert.assertTrue(container.get(null).isEmpty());

        DataProvider<String, Object> provider = DataProvider.fixed(new HashMap<>());
        threadContextContainerProvider.setDataProvider("test", provider);
        Assert.assertSame(provider, threadContextContainerProvider.getDataProvider("test"));

        Assert.assertSame(provider.apply(null), container.get(null));
        Assert.assertSame(provider, threadContextContainerProvider.removeDataProvider("test"));
        Assert.assertTrue(container.get(null).isEmpty());

        threadContextContainerProvider.setDataProvider("test", provider);
        threadContextContainerProvider.clear();
        Assert.assertTrue(container.get(null).isEmpty());
    }
}
