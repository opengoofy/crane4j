package cn.crane4j.core.executor.key;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Default key resolver registry
 *
 * @author huangchengxing
 * @since 2.7.0
 */
public class DefaultKeyResolverProviderRegistry implements KeyResolverRegistry {

    private final Map<String, KeyResolverProvider> providers = new HashMap<>();

    @Override
    public void registerKeyResolverProvider(String name, KeyResolverProvider resolverProvider) {
        providers.put(name, resolverProvider);
    }

    @Override
    public KeyResolverProvider getKeyResolver(String name) {
        return providers.get(name);
    }
}
