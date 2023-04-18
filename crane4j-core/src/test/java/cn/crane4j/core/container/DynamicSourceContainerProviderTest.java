package cn.crane4j.core.container;

import cn.crane4j.core.support.DataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * test for {@link DynamicSourceContainerProvider}.
 *
 * @author huangchengxing
 */
public class DynamicSourceContainerProviderTest {

    private DynamicSourceContainerProvider dynamicSourceContainerProvider;

    @Before
    public void init() {
        dynamicSourceContainerProvider = new DynamicSourceContainerProvider();
    }

    @Test
    public void test() {
        Container<?> container = dynamicSourceContainerProvider.getContainer("test");
        Assert.assertTrue(container.get(null).isEmpty());

        DataProvider<String, Object> provider = DataProvider.fixed(new HashMap<>());
        dynamicSourceContainerProvider.setDataProvider("test", provider);
        Assert.assertSame(provider, dynamicSourceContainerProvider.getDataProvider("test"));

        Assert.assertSame(provider.apply(null), container.get(null));
        Assert.assertSame(provider, dynamicSourceContainerProvider.removeDataProvider("test"));
        Assert.assertTrue(container.get(null).isEmpty());

        dynamicSourceContainerProvider.setDataProvider("test", provider);
        dynamicSourceContainerProvider.clear();
        Assert.assertTrue(container.get(null).isEmpty());
    }
}
