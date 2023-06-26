package cn.crane4j.core.support;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * test for {@link DataProvider}
 *
 * @author huangchengxing
 */
public class DataProviderTest {

    @Test
    public void empty() {
        DataProvider<String, String> provider = DataProvider.empty();
        Assert.assertTrue(provider.apply(null).isEmpty());
    }

    @Test
    public void fixed() {
        DataProvider<String, String> provider = DataProvider.fixed(null);
        Assert.assertTrue(provider.apply(null).isEmpty());
        DataProvider<String, String> provider2 = DataProvider.fixed(Collections.singletonMap("a", "b"));
        Assert.assertEquals("b", provider2.apply(Collections.singleton("a")).get("a"));
    }
}
