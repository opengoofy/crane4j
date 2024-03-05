package cn.crane4j.core.executor.key;

/**
 * <p>Key resolver register
 *
 * @author huangchengxing
 * @since 2.7.0
 */
public interface KeyResolverRegistry {

    /**
     * Register the resolver.
     *
     * @param name     name
     * @param resolverProvider resolver
     */
    void registerKeyResolverProvider(String name, KeyResolverProvider resolverProvider);

    /**
     * Get the resolver by the name.
     *
     * @param name name
     * @return resolver
     */
    KeyResolverProvider getKeyResolver(String name);
}
