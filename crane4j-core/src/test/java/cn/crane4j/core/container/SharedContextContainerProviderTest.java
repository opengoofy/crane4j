package cn.crane4j.core.container;

import cn.crane4j.core.support.DataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * test for {@link SharedContextContainerProvider}
 *
 * @author huangchengxing
 */
public class SharedContextContainerProviderTest {

    private SharedContextContainerProvider sharedContextContainerProvider;

    @Before
    public void init() {
        sharedContextContainerProvider = new SharedContextContainerProvider();
    }

    @Test
    public void test() {
        Container<?> container = sharedContextContainerProvider.getContainer("test");
        Assert.assertTrue(container.get(null).isEmpty());

        DataProvider<String, Object> provider = DataProvider.fixed(new HashMap<>());
        sharedContextContainerProvider.setDataProvider("test", provider);
        Assert.assertSame(provider, sharedContextContainerProvider.getDataProvider("test"));

        Assert.assertSame(provider.apply(null), container.get(null));
        Assert.assertSame(provider, sharedContextContainerProvider.removeDataProvider("test"));
        Assert.assertTrue(container.get(null).isEmpty());

        sharedContextContainerProvider.setDataProvider("test", provider);
        sharedContextContainerProvider.clear();
        Assert.assertTrue(container.get(null).isEmpty());
    }
}
