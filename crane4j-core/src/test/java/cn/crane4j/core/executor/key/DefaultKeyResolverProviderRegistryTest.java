package cn.crane4j.core.executor.key;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link DefaultKeyResolverProviderRegistry}
 *
 * @author huangchengxing
 */
public class DefaultKeyResolverProviderRegistryTest {

    @Test
    public void test() {
        DefaultKeyResolverProviderRegistry registry = new DefaultKeyResolverProviderRegistry();
        KeyResolver resolver = (t, op) -> null;
        registry.registerKeyResolverProvider("test", resolver);
        KeyResolverProvider provider = registry.getKeyResolver("test");
        Assert.assertSame(resolver, provider);
        Assert.assertSame(resolver, provider.getResolver(null));
    }
}
